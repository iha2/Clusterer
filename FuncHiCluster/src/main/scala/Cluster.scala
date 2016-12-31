
package cluster

case class Vec(cells: Vector[Double]) {
  def merge(that: Vec): Vec = {
    def pureMerger(current: Vector[Double], that: Vector[Double], result: Vector[Double] = Vector[Double]()): Vector[Double] = current match {
      case Vector() => result
      case x +: xs => {
        val mergeCell = (x + that.head) / 2
        pureMerger(xs, that.tail, (mergeCell +: result))
      }
    }
    new Vec(pureMerger(cells, that.cells))
  }
}
case class Cluster(vec: Vec, left: Cluster, right: Cluster, distance: Double, id: String)

object Distance {

  def pearsonCorrelationScore(vec1: Vec, vec2: Vec) =
    if (vec1.cells.length != vec2.cells.length) {
      throw new IllegalArgumentException("The vectors passed are not the same size.")
    } else {
      val sum1 = vec1.cells.sum
      val sum2 = vec2.cells.sum

      val sum1Sq = vec1.cells.map { x => x * x }.sum
      val sum2Sq = vec2.cells.map { x => x * x }.sum

      val productSum = (0 to vec1.cells.length-1).map { index =>
        vec1.cells(index) * vec2.cells(index)
      }.sum

      val num = productSum - (sum1 * sum2)/vec1.cells.length
      val denom = Math.sqrt((sum1Sq - (Math.pow(sum1, 2)/ vec2.cells.length)) * (sum2Sq - (Math.pow(sum2, 2) / vec1.cells.length)))
      if (denom == 0) 0
      1.0 - num/denom
    }
  }

