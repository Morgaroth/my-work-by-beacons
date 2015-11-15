package io.github.morgaroth.android.mywork.storage

import com.kontakt.sdk.android.device.BeaconDevice
import com.parse.{ParseClassName, ParseObject, ParseQuery}

import scala.collection.JavaConversions._
import scala.util.Try

object Beacon {
  def from(i: BeaconDevice) = {
    val b = new Beacon
    b.beaconId = i.getUniqueId
    b.minor = i.getMinor
    b.major = i.getMajor
    b
  }
}

@ParseClassName("Beacon")
class Beacon extends ParseObject {

//  fetchIfNeeded()

  def beaconId: String = getString("beaconId")

  def beaconId_=(beaconId: String) {
    put("beaconId", beaconId)
  }

  def major: Int = getInt("major")

  def major_=(major: Int) {
    put("major", major)
  }

  def minor: Int = getInt("minor")

  def minor_=(minor: Int) = {
    put("minor", minor)
  }
}

//@ParseClassName("Work")

object Work {
  val timestamps_field: String = "timestamps"
  val determinants_field: String = "determinants"

  def all: Try[List[Work]] = {
    val query = ParseQuery.getQuery(classOf[Work].getSimpleName)
    Try {
      //      query.include("raum")
      //      query.include("raum.occupations")
      //      query.include("raum.occupations.user")
      query.find.toList
    }
  }
}

@ParseClassName("Work")
class Work extends ParseObject {

  import Work._

  def name: String = getString("name")

  def name_=(name: String) {
    put("name", name)
  }

  object InWorks {
    def apply() = Option(Work.this.getList[Long](timestamps_field)).map(_.toList).getOrElse(List.empty)

    def +=(timestamps: List[Long]): Unit = Work.this.addAll(timestamps_field, timestamps)

    def +=(timestamp: Long): Unit = this += List(timestamp)
  }

  object Determinants {
    def apply: List[Beacon] = Option(Work.this.getList[Beacon](determinants_field)).map(_.toList).getOrElse(List.empty)

    def +=(beacon: Beacon) = Work.this.add(determinants_field, beacon)
  }

}