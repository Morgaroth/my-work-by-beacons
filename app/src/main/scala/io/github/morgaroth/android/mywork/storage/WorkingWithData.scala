package io.github.morgaroth.android.mywork.storage

import scala.language.postfixOps
import scala.util.Try

/**
 * Created by mateusz on 15.11.15.
 */
trait WorkingWithData {

  var knownBeacons = Map.empty[String, (Beacon, Work)]

  var works = List.empty[Work]

  def updateBeaconsAndWorksData(): Unit = {
    works = loadWorks
    knownBeacons = loadBeacons(works)
  }

  def loadBeacons(works: List[Work]): Map[String, (Beacon, Work)] = {
    works.flatMap(w => w.Determinants.apply.map(b => b.beaconId ->(b, w))) toMap
  }

  def loadWorks: List[Work] = {
    val works1: Try[List[Work]] = ParseManager.works
    works1.failed.map {
      t => println(s"loading works end with $t")
    }
    works1.getOrElse(List.empty)
  }
}
