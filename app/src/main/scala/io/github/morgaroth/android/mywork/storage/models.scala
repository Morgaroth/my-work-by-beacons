package io.github.morgaroth.android.mywork.storage

import com.parse.ParseObject

import scala.collection.JavaConversions._

//@ParseClassName("Beacon")
class Beacon extends ParseObject {

  def uuid: String = getString("proximityUUID")

  def uuid_=(proximityUUID: String) {
    put("proximityUUID", proximityUUID)
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
}

class Work extends ParseObject {

  import Work._

  def name: String = getString("name")

  def name_=(name: String) {
    put("name", name)
  }

  object InWorks {
    def apply = Work.this.getList[Long](timestamps_field).toList

    def +=(timestamps: List[Long]) = Work.this.addAll(timestamps_field, timestamps)

    def +=(timestamp: Long) = this += List(timestamp)
  }

  object Determinants {
    def apply = Work.this.getList[Beacon](determinants_field).toList

    def +=(beacon: Beacon) = Work.this.add(determinants_field, beacon)
  }

}