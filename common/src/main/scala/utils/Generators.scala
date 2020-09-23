package utils

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import serialization.EventSerializer

object Generators {
  def config(customConf: Config = ConfigFactory.empty): Config =
    Seq(
      customConf,
      EventSerializer.automatedSerializationConfig,
      ConfigFactory.load()
    ).reduce(_ withFallback _)

  def actorSystem(port: Int = 2559, actorSystemName: String = "TestActorSystem"): ActorSystem = {
    val customConf =
      ConfigFactory.parseString(s"""
      akka.loglevel = ERROR
      #akka.persistence.typed.log-stashing = on
      akka.actor.provider = cluster
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.journal.inmem.test-serialization = on
      akka.actor.allow-java-serialization = false
      akka.cluster.jmx.multi-mbeans-in-same-jvm = on

      akka.cluster.seed-nodes = ["akka://$actorSystemName@127.0.1.1:$port"]
      akka.remote.artery.canonical.port = $port

      """)
    ActorSystem(actorSystemName, config(customConf))
  }
}
