name := "Clusterer"

version := "1.0"

scalaVersion := "2.10.2"

resolvers += "spray repo" at "http://repo.spray.io"

resolvers += "spray nightlies" at "http://nightlies.spray.io"

libraryDependencies ++= {
  val akkaVersion = "2.3.6"
  val sprayVersion = "1.3.1"
  Seq(
    "com.typesafe.akka"      %% "akka-actor"            % akkaVersion,
    "com.typesafe.akka"      %% "akka-slf4j"            % akkaVersion,
    "io.spray"                % "spray-can"             % sprayVersion,
    "io.spray"                % "spray-client"          % sprayVersion,
    "io.spray"                % "spray-routing"         % sprayVersion,
    "com.datastax.cassandra"  % "cassandra-driver-core" % "2.1.1"  exclude("org.xerial.snappy", "snappy-java"),
    "org.xerial.snappy"       % "snappy-java"           % "1.1.1.3",
    "org.scala-lang"          % "scala-reflect"         % "2.10.4")
}

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Ywarn-dead-code",
  "-language:_",
  "-target:jvm-1.7",
  "-encoding", "UTF-8"
)
