package com.jalarbee.kaalis

import java.util.UUID

import spray.json._
import scala.concurrent.ExecutionContextExecutor
import akka.stream.{Materializer, ActorMaterializer}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.Http
import akka.actor.{ActorSystem, Props}
import com.typesafe.config.{ConfigFactory, Config}
import akka.event.{Logging, LoggingAdapter}

final case class Customer(firstName: String, lastName: String)
final case class Transfer(id: Long, price: BigDecimal)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val cusotmerFormat = jsonFormat2(Customer)
  implicit val transferFormat = jsonFormat2(Transfer)
}

trait TransferService extends Directives with JsonSupport {

  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def config: Config
  val logger: LoggingAdapter

  val routes =
      get {
        path("ping") {
          complete(Transfer(1L, BigDecimal(12.45)))
        } ~
        path("pong") {
          complete(Customer("Hi", "there"))
        }
      }
}

object Boot extends App with TransferService {

  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))
}
