package cluster

package object cluster {
  type ClusterMap = Map[(String, String), Double]
}

import scala.concurrent.{Await, Future }
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

case class PairData(clusterSetId: (String, String), distance: Double)
case class ComputeSet(distances: cluster.ClusterMap, clusters: Vector[Cluster], toCompare: Cluster)

class PureHiCluster(clusters: Vector[Cluster], distances: Map[(String, String), Double]) {

  def generateClusterCalculations(clusters: Vector[Cluster], distances: Map[(String, String), Double], pairData: PairData) = {

    def loop(clusters: Vector[Cluster], futureResults: List[Future[(Map[(String, String), Double], PairData)]]): List[Future[(Map[(String, String), Double], PairData)]]  =
     clusters match {
       case Vector() => futureResults
       case x +: xs =>  loop(xs, Future[(Map[(String, String), Double], PairData)] { computeDistances(clusters.dropWhile { y => y.id != x.id}, distances, x, pairData) } +: futureResults)
     }
    loop(clusters, List())
  }

  def updateMostSimilar(acc: (Map[(String, String), Double], PairData), futureResult: (Map[(String, String), Double], PairData)) = {
    val distanceAsSeq = acc._1.toSeq ++ futureResult._1.toSeq
    val newUniqueDistances = Map(distanceAsSeq.groupBy(_._1).mapValues(_.head._2).toSeq: _*)
    if (futureResult._2.distance < acc._2.distance) { (newUniqueDistances, futureResult._2) } else { (newUniqueDistances, acc._2) }
  }

  def start(starterIndex: Int, clusters:Vector[Cluster] = clusters): Cluster = {
      val vec1 = clusters.head
      val vec2 = clusters.tail.head
      val futureCompletedDistance = List[Future[(Map[(String, String), Double], PairData)]]()
      val pairData = new PairData((vec1.id, vec2.id), Distance.pearsonCorrelationScore( vec1.vec, vec2.vec))
      val calculationFutures = generateClusterCalculations(clusters, distances, pairData)

      // Here I'm assuming all the futures will return successfully since they are simply calculations
      val futureResults = Future.sequence(calculationFutures)
      val x = Await.result(futureResults, 600.seconds)
      val result = x.foldLeft( (Map[(String, String), Double](), pairData) )((acc, y) => updateMostSimilar(acc, y))
      val nearestClusters = clusters.filter( x => x.id == result._2.clusterSetId._1 || x.id == result._2.clusterSetId._2 )

      if (nearestClusters.length != 2) {
        throw new Exception("the closest neighbour clusters were not found ")
      } else {
        val mergedVector = nearestClusters.head.vec.merge(nearestClusters.tail.head.vec)
        val newCluster = Cluster(mergedVector, nearestClusters.head, nearestClusters.tail.head, result._2.distance , starterIndex.toString())
        val updatingClusters = newCluster +: clusters.filter( x => !nearestClusters.map(x => x.id).contains(x.id))

        if (updatingClusters.length == 1) {
          println(updatingClusters.head)
          updatingClusters.head
        } else {
          println(updatingClusters.length + " remaining .....")
          start(starterIndex-1, updatingClusters)
        }
      }
  }

  def computeDistances(clusters: Vector[Cluster], distances: Map[(String, String), Double], cluster: Cluster, pd: PairData):  (Map[(String, String), Double], PairData) = {

    def getMostSimilarCluster(clusters: Vector[Cluster], comparedCluster: Cluster,
                              distances: Map[(String, String), Double],
                              mostRelatedPair: PairData): (Map[(String, String), Double], PairData) = clusters match {

      case Vector() => (distances, mostRelatedPair)
      case x +: xs => {

        // get the relational map of distances between clusters
        val updatedDistances: Map[(String, String), Double] = if ( !(distances contains (comparedCluster.id, x.id)) ) {
          distances + ((comparedCluster.id, x.id) -> Distance.pearsonCorrelationScore(comparedCluster.vec, x.vec)) }
        else { distances }

        val relevantDistance = updatedDistances((comparedCluster.id, x.id))

        // check if the current w clusters are the closest
        val updatedPairs = if (relevantDistance < mostRelatedPair.distance) {
          new PairData((comparedCluster.id, x.id), mostRelatedPair.distance)
        } else {
          mostRelatedPair
        }

        getMostSimilarCluster(xs, comparedCluster, updatedDistances, updatedPairs)
      }
    }
    getMostSimilarCluster(clusters, cluster, distances, pd)
  }
}


