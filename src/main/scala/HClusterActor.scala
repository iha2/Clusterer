import akka.actor.Actor
import scala.collection._
import akka.actor.Props


package Cluster {


  case class ComputeVariables(var closest: Double, lowestPair: (Int, Int),
                              clusterToCompareTo: (Int, Cluster), clusters: Vector[Cluster],
                              distances: mutable.Map[(String, String), Double])
  case class ClosestAndLowestPair(var closest: Double, var lowestPair: (Int, Int))
  case class ComputeResults(distances: mutable.Map[(String, String), Double], clAndLw: ClosestAndLowestPair)

  class HClusterActor(var distances: mutable.Map[(String, String), Double], var currentClustID: Int, var ForAllClAndLw: ClosestAndLowestPair) extends Actor {

    implicit val system = context.system
    var computingClusterCounter = -1
//    val distances: mutable.Map[(String, String), Double] = mutable.Map[(String, String), Double]()
    var clusters = Vector[Cluster]()
//    var currentClustID = -1
    def receive = {
      case clusters: Vector[Cluster] => {
        this.clusters = clusters
        // I was working on this part last time
        clusters.zipWithIndex.foreach { case (x, i) =>
          val computeDistances  = system.actorOf(Props(classOf[ComputeDistancesActor], ForAllClAndLw, (i, x), clusters, distances))
          computeDistances ! "start"
        }
      }

      case results: ComputeResults => {

        if (computingClusterCounter != 0) {

          // merge the map of the new set of distances with the old set to get map of all computed distances
          val distanceAsSeq = distances.toSeq ++ results.distances.toSeq
          distances = mutable.Map(distanceAsSeq.groupBy(_._1).mapValues(_.head._2).toSeq: _*)

          //check if the returned closest set of clusters is closer together than the previously known closest set of clusters
          if (results.clAndLw.closest < ForAllClAndLw.closest) {
            ForAllClAndLw.closest = results.clAndLw.closest
            ForAllClAndLw.lowestPair = results.clAndLw.lowestPair
          }
          computingClusterCounter -= 1
        } else {
          // create new vector from average of both vectors
          val mergedVector = (0 to clusters(0).vec.data.length).map { x =>
            (clusters(results.clAndLw.lowestPair._1).vec.data(x) + clusters(results.clAndLw.lowestPair._2).vec.data(x)) / 2
          }.toVector

          // create new vector object and cluster object
          val vec = new Vec(mergedVector)
          val newCluster = new Cluster(clusters(results.clAndLw.lowestPair._1), clusters(results.clAndLw.lowestPair._2), vec, currentClustID.toString, results.clAndLw.closest)
          currentClustID -= 1
          clusters = clusters.drop(results.clAndLw.lowestPair._1)
          clusters = clusters.drop(results.clAndLw.lowestPair._2)
          clusters = clusters :+ newCluster

          // check if the final cluster has been made. If not continue merging clusters
          if (clusters.length != 1) {
            println("loop complete")
            println("remaining clusters: " + clusters.length)
            self ! clusters
          } else {
            println("done!!")
          }
        }

      }
    }
  }

  class ComputeDistancesActor(clAndLw: ClosestAndLowestPair,
                              clusterToCompareTo: (Int, Cluster), clusters: Vector[Cluster],
                              var distances: mutable.Map[(String, String), Double]) extends Actor {

    def receive = {
      case "start" => {
        var close = clAndLw.closest
        var lp = clAndLw.lowestPair
        clusters.zipWithIndex.foreach { case (y,i) =>

          var updated = true
          if (!(distances contains (clusterToCompareTo._2.id, y.id))) {
           distances = distances += ((clusterToCompareTo._2.id, y.id) -> DistanceMetrics.PearsonCorrelationScore(clusterToCompareTo._2.vec, y.vec))
          }
          val distance = distances get (clusterToCompareTo._2.id, y.id) match {
            case Some(aDistance) => aDistance
            case None => if (updated) {
              println(updated)
              println((clusterToCompareTo._2.id, y.id) + " got updated")
            } else {
              println(updated)
              println((clusterToCompareTo._2.id, y.id) + " did not get updated")
            }
          }

          if (distance.asInstanceOf[Double] < clAndLw.closest) {
            close = distance.asInstanceOf[Double]
            lp = (clusterToCompareTo._1, i)
          }
          sender ! ComputeResults(distances, new ClosestAndLowestPair(close, lp))
        }
      }
    }
  }
}
