package com.lucho

import spray.routing.Directives
import spray.http._
import akka.actor.{ActorSystem, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import com.lucho.models.personal
import com.lucho.models.{Position, Ping}
import com.lucho.models.PingProtocol._
import spray.httpx.SprayJsonSupport._
import org.slf4j.LoggerFactory
import scala.concurrent.Future
import reactivemongo.api.collections.default.BSONCollection
import reactivemongo.core.commands.LastError
import reactivemongo.api.{Cursor, DefaultDB}
import reactivemongo.bson.BSONDocument

import spray.httpx.marshalling._

import com.lucho.models.PingBSON._

import Stream._
import org.joda.time.DateTime
import play.api.libs.iteratee.{Enumerator, Iteratee}
import spray.http.HttpResponse
import com.lucho.models.Ping
import reactivemongo.api.DefaultDB
import reactivemongo.api.collections.default.BSONCollection
import spray.http.HttpHeaders.RawHeader

//F...ing implicits
import scala.concurrent.ExecutionContext.Implicits.global

//We HAVE to include ClassTag because of some broken stuff. See http://stackoverflow.com/questions/15584328/scala-future-mapto-fails-to-compile-because-of-missing-classtag
import reflect.ClassTag

trait Routes extends Directives {

  val log = LoggerFactory.getLogger(classOf[Routes])

  implicit val timeout = Timeout(90.seconds)

  def actor: ActorRef

  val db: DefaultDB

  val system: ActorSystem

  val version = Project.version

  def routes: spray.routing.Route = {
    path("ping") {
      get {
        produce(instanceOf[List[Ping]]) { //
          prod => complete {
            log.info("All received")
            val collection: BSONCollection = db("ping")
            val cursor: Cursor[Ping] = collection.find(BSONDocument()).cursor[Ping]
            cursor.toList()
          }
        }
      } ~
        post {
          entity(as[Ping]) {
            ping => complete {
              log.info("Save received with Ping: {}", ping)
              val collection: BSONCollection = db("ping")
              val future: Future[LastError] = collection.insert(ping)
              val future2: Future[HttpResponse] = future.map {
                le => {
                  log.info("Save success")
                  val body = "{'success': " + !le.inError + ", 'message': '" + (if (le.inError) le.message else "") + "'}"
                  HttpResponse(entity = HttpBody(ContentType.`application/json`, body))
                }
              }
              future2
            }
          }
        }
    } ~
    path("health-check") {
      get {
        complete {
          val body = "{'status':'ok', 'version': '" + version + "'}"
          val status = StatusCodes.OK
          val entity = HttpBody(ContentType.`application/json`, body)
          val headers = List(HttpHeaders.`Cache-Control`(CacheDirectives.`no-cache`),
            RawHeader("no-gzip", "health-check-disabled-gzip"),
            RawHeader("Expires", "0")
          )
          HttpResponse(status, entity, headers)
        }
      }
    } ~
    path ("version") {
      get {
        complete {
          val status = StatusCodes.OK
          val entity = HttpBody(ContentType.`application/json`, version)
          val headers = List(HttpHeaders.`Cache-Control`(CacheDirectives.`no-cache`),
            RawHeader("no-gzip", "health-check-disabled-gzip"),
            RawHeader("Expires", "0")
          )
          HttpResponse(status, entity, headers)
        }
      }
    }
  }


}

