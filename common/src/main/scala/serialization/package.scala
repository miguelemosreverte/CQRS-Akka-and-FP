import play.api.libs.json.{Format, JsValue, Json}

import scala.reflect.ClassTag

package object serialization {

  def encode[A](a: A)(implicit format: Format[A]): String =
    Json.prettyPrint(format.writes(a))

  def decode[A: ClassTag](a: String)(implicit format: Format[A]): Either[Throwable, A] = {
    def ctag = implicitly[reflect.ClassTag[A]]
    def AClass: Class[A] = ctag.runtimeClass.asInstanceOf[Class[A]]
    if (AClass.getName == "java.lang.String") {
      Right(a.asInstanceOf[A])
    } else {
      Json.parse(a).asOpt[A] match {
        case Some(a) => Right(a)
        case None =>
          Left(
            new SerializationError(
              s"""
              Failed to decode ${AClass.getName}
              because of:
              ${Json.parse(a).validate}
              message that failed was: 
              $a
              """
            )
          )
      }
    }
  }

  case class SerializationError(message: String) extends Exception {
    override def getMessage: String = message
  }
}
