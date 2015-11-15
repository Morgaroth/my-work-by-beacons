package io.github.morgaroth.android.mywork.storage

import scala.language.postfixOps

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
    ParseManager.works.getOrElse(List.empty)
  }
}
