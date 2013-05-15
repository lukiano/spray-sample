package com.lucho

import spray.routing.SimpleRoutingApp
import spray.httpx.SprayJsonSupport
import akka.actor.Props
import reactivemongo.core.actors.{MonitorActor, MongoDBSystem}
import reactivemongo.api.{DefaultDB, MongoConnection}

object Main extends App with SimpleRoutingApp with SprayJsonSupport with Routes {

  val mongoSystemActor = system.actorOf(Props(new MongoDBSystem(List("localhost"), Seq.empty, 10 )))
  val monitorActor = system.actorOf(Props(new MonitorActor(mongoSystemActor)))
  val connection = new MongoConnection(system, mongoSystemActor, monitorActor)

  lazy val db: DefaultDB = connection("spraynetworkblame")

  def actor = system.actorOf(Props(new PingActor(db)))

  startServer(interface = "localhost", port = 8080) {
    routes
  }

}
