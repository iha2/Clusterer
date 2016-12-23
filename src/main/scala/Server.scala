import akka.actor.{ActorSystem, Props}
import akka.pattern._
import akka.io.IO
import akka.util.Timeout
import spray.can.Http
import scala.concurrent.duration._
import Cluster._

package Server {

  import scala.collection.mutable

  object Server extends App with CassandraConfig {
    implicit lazy val system = ActorSystem("Clusterer")
    val routerActor = system.actorOf(Props(classOf[RouterActor]), "router")
    Main.run
    implicit val timeout = Timeout(5.seconds)
    IO(Http) ? Http.Bind(routerActor, interface = "localhost", port =  8080)
  }

  object Main {
    def run(implicit system: ActorSystem): Unit = {
      def config = system.settings.config

      val fileLocation = config.getConfig("application-settings.main.data-file").getString("fileLocation")
      val file  = config.getConfig("application-settings.main.data-file").getString("fileName")
      val fileData = DataImporter.importFile(fileLocation + file)
      val clusters = fileData.data.zipWithIndex.map { case (x, i) =>
        new Cluster(null, null, new Vec(x), i.toString(), 0.0)
      }
      val clAndLw = new ClosestAndLowestPair(DistanceMetrics.PearsonCorrelationScore(clusters(0).vec, clusters(1).vec), (0,1))
      val hCluster = system.actorOf(Props(classOf[HClusterActor], mutable.Map[(String,String), Double](), -1, clAndLw), "hCluster")
      hCluster ! clusters
    }
  }
}