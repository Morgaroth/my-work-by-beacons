package io.github.morgaroth.android.mywork

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

  def minor_=(minor: Int) ={
    put("minor", minor)
  }
}

//@ParseClassName("Work")
class Work extends ParseObject {
  def name: String = getString("name")

  def name_=(name: String) {
    put("name", name)
  }

  def determinants: List[Beacon] = getList("determinants").toList

  def determinants_=(determinants: List[Beacon]) {
    put("determinants", determinants)
  }
}
