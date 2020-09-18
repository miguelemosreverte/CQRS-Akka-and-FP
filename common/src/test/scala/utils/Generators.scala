package utils

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}

object Generators {
  def actorSystem(actorSystemName: String, port: Int = 2559): ActorSystem = {
    val customConf =
      ConfigFactory.parseString(s"""
      akka.actor.provider = cluster
      akka.persistence.journal.inmem.test-serialization = on
      akka.actor.allow-java-serialization = true

      akka.cluster.seed-nodes = ["akka://$actorSystemName@0.0.0.0:$port"]
      akka.remote.artery.canonical.port = $port

      """)
    lazy val config: Config = Seq(
      customConf // Level 1 - will replace values set in StaticConfig
    ).reduce(_ withFallback _)

    ActorSystem(actorSystemName, config)
  }
}
