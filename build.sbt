name := "FlinkSQLKafka"

version := "0.1"

scalaVersion := "2.12.15"

libraryDependencies += "dev.zio" %% "zio" % "1.0.12"
libraryDependencies += "dev.zio" %% "zio-streams" % "1.0.12"
libraryDependencies += "dev.zio" %% "zio-json" % "0.1.5"
libraryDependencies += "dev.zio" %% "zio-kafka" % "0.16.0"

libraryDependencies += "org.apache.flink" % "flink-core" % "1.13.3"
libraryDependencies += "org.apache.flink" %% "flink-streaming-scala" % "1.13.3" % "provided"
libraryDependencies += "org.apache.flink" %% "flink-scala" % "1.13.3"
libraryDependencies += "org.apache.flink" %% "flink-connector-kafka" % "1.13.3"
libraryDependencies += "org.apache.flink" %% "flink-table-api-scala-bridge" % "1.13.3"
libraryDependencies += "org.apache.flink" %% "flink-table-api-java-bridge" % "1.13.3"
libraryDependencies += "org.apache.flink" % "flink-table" % "1.13.3" % "provided"
libraryDependencies += "org.apache.flink" %% "flink-table-api-scala" % "1.13.3"
libraryDependencies += "org.apache.flink" %% "flink-table-planner-blink" % "1.13.3"
libraryDependencies += "org.apache.flink" % "flink-json" % "1.13.3"
libraryDependencies += "org.apache.flink" %% "flink-clients" % "1.13.3"