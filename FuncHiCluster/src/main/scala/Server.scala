package server
import spray.can.Http
import akka.io.IO
import akka.pattern._
import akka.util.Timeout

import scala.concurrent.duration._
import akka.actor.{ActorSystem, Props}
import cluster._
import util._


object Server extends App {
  implicit lazy val system = ActorSystem("FunctionalActorSystem")
  val routerActor = system.actorOf(Props(classOf[RouterActor]), "Router")

  Main.run

  implicit val timeout = Timeout(5.seconds)
  IO(Http) ? Http.Bind(routerActor, interface= "localhost", port=8080)
}

object Main {
  def run(implicit system: ActorSystem): Unit = {
    def config = system.settings.config
    val directory = config.getConfig("application-settings.data-location").getString("dataDirectory")
    val file = config.getConfig("application-settings.data-location").getString("data-set-1")
    val importedData = new DataImporter(System.getProperty("user.home") + "/" + directory + file)

    val clusters = importedData.processFile.rows.zipWithIndex.map { case (x, i) =>
      new Cluster(new Vec(x.map( y => y.get)), null, null, 0.0, i.toString())
    }
    val HierarchicalClusteringAlgorithm = new PureHiCluster(clusters, Map[(String, String), Double]())
    println("started...")
    println( clusters.length + " clusters in total.")
    HierarchicalClusteringAlgorithm.start(-1)
  }
}