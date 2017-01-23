package cluster

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import util._

object Main extends App {

  val random = scala.util.Random

  def rangeOfDimensions(clusters: Vector[Cluster]) = {
    val initialCluster = clusters.head.vec.cells.zip(clusters.tail.head.vec.cells)
    clusters.tail.foldLeft(initialCluster) {
      case (minMaxValues, currentVector) =>
        minMaxValues.zip(currentVector.vec.cells)
                    .map(values => (math.min(values._1._1, values._2), math.max(values._1._2, values._2)))
    }
  }

  def createRandomClusters(maxMinRanges: Vector[(Double, Double)], k: Int): Vector[Cluster] =
    (0 to k).map { currentK => {
      val randomizedVector = maxMinRanges.map(x => x._1 + (random.nextDouble * (x._2 - x._1)))
      new Cluster(new Vec(randomizedVector), null, null, 0.0, currentK.toString())
    }
    }.toVector

  def closestMembersToCentroid(rows: Vector[Cluster], centroids: Map[Int, Cluster]): Vector[Future[(Int, Cluster)]] = rows.map { row => {
    Future[(Int, Cluster)] {
      val closetCentroid:(Int, Cluster) = centroids.reduce { (centroid1, centroid2) => {
          val distToCentroid = Distance.pearsonCorrelationScore(centroid2._2.vec, row.vec)
          if (distToCentroid < Distance.pearsonCorrelationScore(centroid1._2.vec, row.vec)) {
            centroid2
          } else {
            centroid1
          }
        }
      }
      (closetCentroid._1, row)
    }
  }
  }

  def collectCentroidNeighbours(rows: List[(Int, Cluster)]): Map[Int, List[Cluster]] = {
    rows.foldLeft(Map[Int, List[Cluster]]()) {
      case (clusterCollection, clusterSet) => {
        if (clusterCollection contains clusterSet._1) {
          val clusterList = clusterCollection(clusterSet._1)
          clusterCollection + (clusterSet._1 -> (clusterSet._2 +: clusterList))
        } else {
          clusterCollection + (clusterSet._1 -> List(clusterSet._2))
        }
      }
    }
  }

  def averageNeighbours(clusterNeighbours: Map[Int, List[Cluster]]): Map[Int, Cluster] =
    clusterNeighbours.map { x =>
      val cluster = x._2.reduceLeft { (sum, cluster) => new Cluster(cluster.vec.sum(sum.vec), null, null, 0.0, "0") }
      cluster.copy(new Vec(cluster.vec.cells.map(cell => cell / x._2.length)))
    }.zipWithIndex.foldLeft(Map[Int, Cluster]()) {
      case (x, y) => {
        x + (y._2 -> y._1.asInstanceOf[Cluster])
      }
    }

  def main(tries: Int): Map[Int, Cluster] = {

    import com.typesafe.config.ConfigFactory

    val directory = ConfigFactory.load().getString("application-settings.data-location.dataDirectory")
    val file = ConfigFactory.load().getString("application-settings.data-location.data-set-1")
    val importedData = new DataImporter(System.getProperty("user.home") + "/" + directory + file)

    val clusters = importedData.processFile.rows.zipWithIndex.map { case (x, i) =>
      new Cluster(new Vec(x.map(y => y.get)), null, null, 0.0, i.toString())
    }

  val ranges = rangeOfDimensions(clusters)
  val randomizedCentroids = createRandomClusters(ranges, 4).zipWithIndex.map( x => (x._2 -> x._1)).toMap[Int, Cluster]

    def loop(index: Int, lastMatches: Map[Int, Cluster]): Map[Int, Cluster] = index match {
      case 0 => lastMatches
      case _ => {
        val futureCollection = closestMembersToCentroid(clusters, randomizedCentroids)
        val potentialCentroidNeighbours = Future.sequence(futureCollection)
        val centroidNeighbours = Await.result(potentialCentroidNeighbours, 600.seconds)
        val groupedCentroidNeighbours = collectCentroidNeighbours(centroidNeighbours.toList)
        val centroidAverages = averageNeighbours(groupedCentroidNeighbours)

        println("try index " + index)
        if (lastMatches == centroidAverages) {
          lastMatches
        }
        loop(index - 1, centroidAverages)
      }

    }

    val result = loop(tries, Map[Int, Cluster]())
    println(result)
    result
  }
  main(100)
}