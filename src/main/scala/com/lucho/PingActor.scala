package com.lucho

import akka.actor.Actor
import spray.http.{ContentType, HttpBody, HttpResponse}
import spray.util.SprayActorLogging
import com.lucho.models.{PingBSON, Ping}
import com.lucho.models.PingBSON._
import reactivemongo.api._
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.bson.BSONDocument
import scala.concurrent.{Future, ExecutionContext}
import reactivemongo.core.commands.LastError
import scala.util.{Success, Failure}

sealed case class Save(ping: Ping)

sealed case class All()

class PingActor(db: =>DB)(implicit ec: ExecutionContext) extends Actor with SprayActorLogging {

  def receive = {
    case Save(ping) => {
      log.info("Save received with Ping: {}", ping)

      val collection: BSONCollection = db("ping")
      val future: Future[LastError] = collection.insert(ping)
      val zender = sender
      future.onComplete {
        case Success(le: LastError) => {
          log.info("Save success")
          val body = "{'success': " + !le.inError + ", 'message': '" + (if (le.inError) le.message else "") + "'}"
          zender ! HttpResponse(entity = HttpBody(ContentType.`application/json`, body))
        }
        case Failure(throwable) => {
          log.error(throwable, "Save failure")
          val body = "{'success': false, 'message': '" + throwable.getMessage + "'}"
          zender ! HttpResponse(entity = HttpBody(ContentType.`application/json`, body))
        }
      }
    }

    case All => {
      log.info("All received")

      val collection: BSONCollection = db("ping")

      val cursor: Cursor[Ping] = collection.find(BSONDocument()).cursor[Ping]
      val future: Future[List[Ping]] = cursor.toList()

      val zender = sender

      future.onComplete {
        case Success(list: List[Ping]) => {
          log.info("All success. Count: {}", list.size)
          zender ! list
        }
        case Failure(throwable) => {
          log.error(throwable, "All failure")
        }
      }

      //cursor.enumerate()
      //sender ! HttpResponse(entity = HttpBody(ContentType.`application/json`, body))
    }

  }

}
