package util

import scala.io.Source
import java.io._

case class DataContainer(columnHeaders: Vector[String], rows: Vector[Vector[Option[Double]]])

class DataImporter(file: String) {

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

    val rows: Vector[Vector[Option[Double]]] = lines.tail.zipWithIndex.map { case (x, i) =>
      val cells = x.split("\\s *")
      if (cells.length != vectorLength) {
        println("There is missing data in row " + i + ". Skipping..")
        Vector[Option[Double]]()
      } else {

        val result = cells.zipWithIndex.map { case (x, j) =>
          validateCells(x, i, j)
        }

        if (result.takeWhile(x => x.isDefined).length != vectorLength) {
          Vector[Option[Double]]()
        } else {
          result.toVector
        }
      }
    }.filter(x => x.nonEmpty).toVector

    println("There were " + rows.length + " clean lines out of " + lines.tail.length + " rows of data.")
    new DataContainer(columnHeaders, rows)
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
      case e: NumberFormatException => throw new NumberFormatException("Unable to cast number at row " + i + " and column " + j)
    }
  }
}
