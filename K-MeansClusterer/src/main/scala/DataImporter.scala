package util

import scala.io.Source
import java.io._

case class DataContainer(columnHeaders: Vector[String], rows: Vector[Vector[Option[Double]]], failPercentage: Double)

class DataImporter(file: String, acceptableFailurePercentage: Double = 0.10) {

  private def importFile(file: String): List[String] = {
    try {
      Source.fromFile(file).getLines().toList
    } catch {
      case e: FileNotFoundException => {
        println("The file entered does not exists")
        throw e
      }
    }
  }

  def processFile = {
    val lines = importFile(file)
    val columnHeaders = lines.head.split("\\s *").toVector
    val vectorLength = columnHeaders.length

    val rows = lines.tail.zipWithIndex.map { case (x, i) =>
      val cells = x.split("\\s *")
      cells match {
        case x if ( x.length != vectorLength ) => {
          println("There is missing data in row " + i + ". Skipping..")
          Vector[Option[Double]]()
        }
        case _ => {
          val result = cells.zipWithIndex.map { case (x, j) =>
            validateCells(x, i, j)
          }
          result.takeWhile(x => x.isDefined).length match {
            case y if (y != vectorLength) => Vector[Option[Double]]()
            case _ => result.toVector
          }
        }
      }
    }.foldLeft((Vector[Vector[Option[Double]]]()), Vector[Vector[Option[Double]]]()) { (m, n) => {
      n match {
        case z: Vector[Option[Double]] if z.isEmpty => {
          val newFailure = z +: m._1
          (newFailure, m._2)
        }
        case z if z.nonEmpty => {
          val newSuccess = z +: m._2
          (m._1, newSuccess)
        }
      }
    }
    }

    val failureRate = (rows._1.flatMap(x => x).length.toDouble / rows._2.flatMap(x => x).length.toDouble)

    failureRate match {
      case x if (x > acceptableFailurePercentage) => throw new Exception(s"The failure rate was $failureRate. It does not pass the success")
      case _ => {
        println(s"There were ${rows._2.length} clean lines out of ${lines.tail.length} rows of data. Error Rate: ${failureRate}")
        new DataContainer(columnHeaders, rows._2, acceptableFailurePercentage)
      }
    }
  }

  def validateCells(x: String, i: Int, j: Int): Option[Double] = {
    try {
      val cellValue = x.toDouble
      if (cellValue == Double.NegativeInfinity || cellValue == Double.PositiveInfinity) {
        println("cell value too large at row: " + i + " column: " + j)
        None
      }
      Some(cellValue)
    } catch {
      case e: NumberFormatException => {
        println("cell value too large at row: " + i + " column: " + j)
        None
      }
    }
  }
}
