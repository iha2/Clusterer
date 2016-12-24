import akka.actor.Actor
import scala.collection._
import akka.actor.Props


package Cluster {

  case class ComputeVariables(var closest: Double, lowestPair: (Int, Int),
                              clusterToCompareTo: (Int, Cluster), clusters: Vector[Cluster],
                              distances: mutable.Map[(String, String), Double])
  case class ClosestAndLowestPair(var closest: Double, var lowestPair: (Int, Int))
  case class ComputeResults(distances: Map[(String, String), Double], clAndLw: ClosestAndLowestPair)
  case class hClusterData(distances: Map[(String, String), Double], currentClustID: Int, clusters: Vector[Cluster])

  class HClusterActor extends Actor {

    implicit val system = context.system
    var computingClusterCounter = -1
    var clusters = Vector[Cluster]()
    var distances = Map[(String, String), Double]()
    var currentClustID:Int = -1
    var ForAllClAndLw: ClosestAndLowestPair = null

    def receive = {
      case clusterData: hClusterData => {
        clusters = clusterData.clusters
        distances = clusterData.distances
        currentClustID = clusterData.currentClustID
        ForAllClAndLw = new ClosestAndLowestPair(DistanceMetrics.PearsonCorrelationScore(clusters(0).vec, clusters(1).vec), (0,1))
        computingClusterCounter = clusters.length-2
        clusters.zipWithIndex.foreach { case (x, i) =>
          if ( i < clusters.length-1) {
            val computeDistances  = system.actorOf(Props(classOf[ComputeDistancesActor], ForAllClAndLw, (i, x), clusters.drop(i+1), distances))
            computeDistances ! "start"
          }
        }
      }

      case results: ComputeResults => {
        if (computingClusterCounter != 0) {
          val distanceAsSeq = distances.toSeq ++ results.distances.toSeq
          distances = Map(distanceAsSeq.groupBy(_._1).mapValues(_.head._2).toSeq: _*)

          //check if the returned closest set of clusters is closer together than the previously known closest set of clusters
          if (results.clAndLw.closest < ForAllClAndLw.closest) {
            ForAllClAndLw.closest = results.clAndLw.closest
            ForAllClAndLw.lowestPair = results.clAndLw.lowestPair
          }
          computingClusterCounter -= 1
        } else {
          println("done data " + ForAllClAndLw)
          // create new vector from average of both vectors
          val mergedVector = clusters(ForAllClAndLw.lowestPair._1).vec.merge(clusters(ForAllClAndLw.lowestPair._2).vec)
          val newCluster = new Cluster(clusters(ForAllClAndLw.lowestPair._1), clusters(ForAllClAndLw.lowestPair._2), mergedVector, currentClustID.toString, results.clAndLw.closest)
          currentClustID -= 1
          clusters = clusters filterNot { x => x == clusters(ForAllClAndLw.lowestPair._1) || x == clusters(ForAllClAndLw.lowestPair._2)}
          clusters = clusters :+ newCluster
          // check if the final cluster has been made. If not continue merging clusters
          if (clusters.length != 1) {
            computingClusterCounter = clusters.length
            self ! hClusterData(distances, currentClustID, clusters)
          } else {
            println("done!!")
          }
        }

      }
    }
  }

  class ComputeDistancesActor(clAndLw: ClosestAndLowestPair,
                              clusterToCompareTo: (Int, Cluster), clusters: Vector[Cluster],
                              var distances: Map[(String, String), Double]) extends Actor {

    def receive = {
      case "start" => {
        var close = clAndLw.closest
        var lp = clAndLw.lowestPair
        clusters.zipWithIndex.foreach { case (y,i) =>

          if (!(distances contains (clusterToCompareTo._2.id, y.id))) {
           distances = distances + ((clusterToCompareTo._2.id, y.id) -> DistanceMetrics.PearsonCorrelationScore(clusterToCompareTo._2.vec, y.vec))
          }
          val distance = distances((clusterToCompareTo._2.id, y.id))

          if (distance < clAndLw.closest) {
            clAndLw.closest = distance
            clAndLw.lowestPair = (clusterToCompareTo._1, i)
          }
        }
        sender ! ComputeResults(distances, clAndLw.copy())
      }
    }
  }
}
