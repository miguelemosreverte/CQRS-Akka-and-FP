package utils

import org.reflections.Reflections

import scala.collection.Set
import scala.jdk.CollectionConverters._
import scala.reflect._

object Inference {

  /*
   *
   * Given a Superclass, say, a Microservice
   *   and a Subclass, say, an ActorTransaction
   * What this function will return is a map like this:
   *
   * Map(
   *   SuperclassA -> Set[SubclassA, SubclassB],
   *   SuperclassB -> Set[SubclassC, SubclassD]
   * )
   *
   * It does do by assuming a standarized packaging pattern,
   * This relation is Inferrable by a standarized packaging pattern:
   *
   * com.project.superclassA.subclass_A
   * com.project.superclassA.subclass_B
   * com.project.superclassB.subclass_C
   * com.project.superclassB.subclass_D
   *
   * */
  def resolveClassHierarchy[Superclasses: ClassTag, Subclasses: ClassTag]: Map[Class[_], Set[Class[_]]] = {
    val superclasses =
      utils.Inference.getSubtypesOf[Superclasses]()

    val subclasses = utils.Inference
      .getSubtypesOf[Subclasses]()

    subclasses
      .map { subclass =>
        val superclass: Class[_] = superclasses
          .map { superclass: Class[_] =>
            (superclass.getCanonicalName.split('.').count { packageName =>
               subclass.getCanonicalName.contains(packageName)
             },
             superclass
            )
          }
          .maxBy { a =>
            a._1
          }
          ._2
        superclass -> subclass
      }
      .groupMap(_._1)(a => a._2)
  }

  // https://github.com/mockito/mockito-scala/issues/117#issuecomment-499654664
  def getSimpleName(name: String): String = {
    val withoutDollar = name.split("\\$").lastOption.getOrElse(name)
    val withoutDot = withoutDollar.split("\\.").lastOption.getOrElse(withoutDollar)
    withoutDot
  }

  def getSubtypesOf[C: ClassTag](
      packageNames: Set[String] = Set(
        "model",
        "consumers",
        "readside",
        "serialization",
        "cassandra",
        "marshalling",
        "country_gdp_ranking",
        "country_yearly_population_delta"
      )
  ): Set[Class[_]] = {
    def aux[C: ClassTag](packageName: String) = {
      val javaSet =
        new Reflections(packageName) // search for subclasses will be performed inside the 'model' package
          .getSubTypesOf(classTag[C].runtimeClass)
      javaSet.asScala.toSet
    }

    packageNames.flatMap { packageName =>
      aux[C](packageName)
    }
  }

  def instantiate[T](clazz: java.lang.Class[_])(args: AnyRef*): T = {
    val constructor = clazz.getConstructors()(0)
    constructor.newInstance(args: _*).asInstanceOf[T]
  }

  def methodOf[Trait: ClassTag, Expected](methodName: String): Set[Expected] = {
    val klasses = getSubtypesOf[Trait]()

    klasses.map { klass =>
      klass
        .getDeclaredMethod(methodName)
        .invoke(klass.getDeclaredConstructor().newInstance())
        .asInstanceOf[Expected]
    }
  }

  def getSubtypesNames[C: ClassTag]: Set[String] =
    getSubtypesOf[C]()
      .map(_.getName)
      .map {
        getSimpleName
      }
}
