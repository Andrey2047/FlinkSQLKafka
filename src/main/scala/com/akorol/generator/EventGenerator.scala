package com.akorol.generator

import java.util.UUID

import org.apache.kafka.clients.producer.ProducerRecord
import zio.clock.Clock
import zio.duration._
import zio.json._
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.kafka.serde.Serde
import zio.random._
import zio.stream.ZStream
import zio.{ExitCode, Schedule, Task, URIO, ZIO, ZLayer}

import scala.math.abs

object EventGenerator extends zio.App{

  val producerSettings: ProducerSettings = ProducerSettings(List("localhost:9092"))
  val ProducerL = ZLayer.fromManaged(Producer.make(producerSettings))

  implicit val decoder1: JsonEncoder[StartOrder] = DeriveJsonEncoder.gen[StartOrder]
  implicit val decoder2: JsonEncoder[CompleteOrder] = DeriveJsonEncoder.gen[CompleteOrder]

  trait OrderEvent

  case class StartOrder(orderId: String, clientId: String, startTime: Long) extends OrderEvent

  case class CompleteOrder(orderId: String, clientId: String, finishTime: Long, status: String) extends OrderEvent

  def generateEvents(clientId: String): Task[Option[(Seq[OrderEvent], String)]] = {
    for {
      orderId <- ZIO.succeed(UUID.randomUUID().toString)
      orderStartTime <- ZIO.succeed(System.currentTimeMillis())
      orderEndTime <- nextInt.map(x => orderStartTime + abs(x)/100000).provideLayer(Random.live)
    } yield Some((Seq(StartOrder(orderId, "clintId", orderStartTime), CompleteOrder(orderId, "clintId", orderEndTime, "Completed")), clientId))
  }

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {

    val spaced = Schedule.spaced(1000.milliseconds)

    ZStream.unfoldM("")(generateEvents).schedule(spaced)
      .map(events => {
        val producerRecord1: ProducerRecord[Int, String] =
          new ProducerRecord("orders", 1, events.head.asInstanceOf[StartOrder].toJson)

        val producerRecord2: ProducerRecord[Int, String] =
          new ProducerRecord("completed_orders", 1, events(1).asInstanceOf[CompleteOrder].toJson)

        (producerRecord1, producerRecord2)
      })
      .foreach(t => {
        Producer.produce(t._1, Serde.int, Serde.string) *> ZIO.sleep(1 second) *>
        Producer.produce(t._2, Serde.int, Serde.string)
    }).provideLayer(ProducerL ++ Clock.live).exitCode
  }
}
