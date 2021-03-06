name := "HiClusterer"

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
    "org.specs2"             %% "specs2-core"           % "3.8.5"     % "test")
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
