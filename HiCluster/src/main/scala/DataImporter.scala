import scala.io.Source

package Cluster {

  case class DataSet[T](header: Vector[String], data: Vector[Vector[T]])
  case class InCompleteRowException(message: String) extends Exception
  object DataImporter {

    // Lots possible tests
    // corrupted data tests
      // Missing column
      // missing row column id
    // incorrect data tests
    // type matching tests
    // data loss tests
    // missing file tests
    // tests for negative and positive results
    // tests for column heading presence and correctness.
    def importFile(fileLocation: String): DataSet[Double] = {
      try {
        val data = Source.fromFile(fileLocation).getLines().toList
        if (data.isEmpty) {
          throw new IllegalStateException("File is empty")
        } else {
          val headers = data.head.split("\\s *").toVector
          println(headers)
          val rows = data.tail.zipWithIndex.map { case (x,i) =>
            val values = x.split("\\s *")

            if (values.length != headers.length)
              throw new InCompleteRowException("Row " + i + " has corrupted data. number of values are not equal to columns.")

            values.map { x =>
              try {
                x.toDouble
              } catch {
                case e: Exception => {
                  println("Type casting to double caused an error at row " + i)
                  throw e
                }
              }
            }.toVector
          }.toVector

          println(rows.length + " rows have been imported")
          new DataSet[Double](headers, rows)
        }
      } catch {
        case e: Exception => throw e
      }
    }
  }
}



