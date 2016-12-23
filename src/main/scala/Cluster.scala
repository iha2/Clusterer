
package Cluster {

  case class Vec(data: Vector[Double])
  case class Cluster(left: Cluster = null, right: Cluster = null, vec: Vec, id: String = null, distance: Double = 0.0)

  object DistanceMetrics {

    def PearsonCorrelationScore(vector1: Vec, vector2: Vec) = {
      val sum1 = vector1.data.sum
      val sum2 = vector2.data.sum

      val sum1Sq = vector1.data.map { x => x * x }.sum
      val sum2Sq = vector2.data.map { x => x * x }.sum

      val productSum = (0 to vector1.data.length-1).map { index =>
        vector1.data(index) * vector2.data(index)
      }.sum

      val num = productSum - (sum1 * sum2)/vector1.data.length
      val denom = Math.sqrt((sum1Sq - (Math.pow(sum1, 2)/ vector2.data.length)) * (sum2Sq - (Math.pow(sum2, 2) / vector1.data.length)))
      if (denom == 0) 0
      1.0 - num/denom
    }
  }
}