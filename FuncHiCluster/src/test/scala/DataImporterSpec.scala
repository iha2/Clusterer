import org.specs2._
import java.io._
import scala.io._
import util._
case class FileInfo(source: String, specifiedErrors: Int)

class DataImporterSpec extends Specification { def is = "An importer that takes data from the user to cluster.".title ^ s2"""

 The DataImporter should behave as such in these defined cases:
   Throw and FileNotFound Exception when there is no file found       ${notPresentFile}
   Check that missing information results in rows being filtered out  ${missingDataError}
   Check that the data in each cell is a valid number                 ${garbageDataError}
   return a DataCase object when it correctly parses the data         ${cleanData}
  """

  val nonExistentFile = new FileInfo(System.getProperty("user.dir") + "/src/test/testData/fakeFile.dat", 0)
  val testFile1 = new FileInfo(System.getProperty("user.dir") + "/src/test/testData/RowLengthTest.dat", 3)
  val testFile2 = new FileInfo(System.getProperty("user.dir") + "/src/test/testData/GarbageCellsTest.dat", 2)
  val testFile3 = new FileInfo(System.getProperty("user.dir") + "/src/test/testData/CleanDataTest.dat", 2)

  def notPresentFile = new DataImporter(nonExistentFile.source).processFile must throwA[FileNotFoundException]

  def missingDataError = {
    println(testFile1)
    val importedData = Source.fromFile(testFile1.source).getLines.toList
    val importedAndProcessedData = new DataImporter(testFile1.source).processFile

    importedData.tail.length must_!= importedAndProcessedData.rows.length
    importedAndProcessedData.rows.length must_== importedData.tail.length - testFile1.specifiedErrors // three errors in file in total
  }

  def garbageDataError = new DataImporter(testFile2.source).processFile must throwA[NumberFormatException]

  def cleanData = {
    val fileData = new DataImporter(testFile3.source)
    fileData.processFile.rows.length must_== Source.fromFile(testFile3.source).getLines.toList.tail.length
  }
}

