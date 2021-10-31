package com.akorol.flink

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

object FlinkApp {

  import org.apache.flink.api.common.typeinfo.TypeInformation

  private val CreateOrderTableQuery = """CREATE TABLE Orders (
                                    |  -- declare the schema of the table
                                    |  `orderId` STRING,
                                    |  `clientId` STRING,
                                    |  `startTime` BIGINT,
                                    |  `processTime` TIMESTAMP(3) METADATA FROM 'timestamp'    -- use a metadata column to access Kafka's record timestamp
                                    |) WITH (
                                    |  -- declare the external system to connect to
                                    |  'connector' = 'kafka',
                                    |  'topic' = 'orders',
                                    |  'scan.startup.mode' = 'earliest-offset',
                                    |  'properties.bootstrap.servers' = 'localhost:9092',
                                    |  'format' = 'json'   -- declare a format for this system
                                    |)""".stripMargin

  private val CreateCompleteOrderTableQuery = """CREATE TABLE CompetedOrders (
                                        |  -- declare the schema of the table
                                        |  `orderId` STRING,
                                        |  `clientId` STRING,
                                        |  `finishTime` BIGINT,
                                        |  `processTime` NUMERIC(3) METADATA FROM 'timestamp',    -- use a metadata column to access Kafka's record timestamp
                                        |  `status` STRING
                                        |) WITH (
                                        |  -- declare the external system to connect to
                                        |  'connector' = 'kafka',
                                        |  'topic' = 'completed_orders',
                                        |  'scan.startup.mode' = 'earliest-offset',
                                        |  'properties.bootstrap.servers' = 'localhost:9092',
                                        |  'format' = 'json'   -- declare a format for this system
                                        |)""".stripMargin

  implicit val info: TypeInformation[String] = TypeInformation.of(classOf[String])

  def main(args: Array[String]): Unit = {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    val tableEnv = StreamTableEnvironment.create(env)

    tableEnv.executeSql(CreateOrderTableQuery)
    tableEnv.executeSql(CreateCompleteOrderTableQuery)

    val orderTimeTable = tableEnv.sqlQuery("SELECT Orders.orderId, " +
      "((CompetedOrders.finishTime - Orders.startTime)/1000) as orderTime" +
      " FROM Orders " +
      "INNER JOIN CompetedOrders " +
      "ON Orders.orderId = CompetedOrders.orderId")

    val resultStream = tableEnv.toDataStream(orderTimeTable)

    resultStream.addSink(r => {
      System.out.println(r.toString)
    })

   env.execute("Test job")
  }

}
