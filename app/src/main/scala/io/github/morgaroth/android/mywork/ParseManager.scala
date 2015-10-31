//package io.github.morgaroth.android.mywork
//
//import com.parse.ParseException
//import com.parse.ParseQuery
//import com.parse.ParseUser
//import java.util.ArrayList
//import java.util.Date
//import java.util.Iterator
//import scala.collection.JavaConversions._
//import scala.util.Try
//
//class ParseManager {
//  def beacons: Try[List[Beacon]] = {
//    val query = ParseQuery.getQuery(classOf[Beacon].getSimpleName)
//    Try {
//      query.include("raum")
//      query.include("raum.occupations")
//      query.include("raum.occupations.user")
//      query.find.toList
//    }
//  }
//
//  def addOccupationToRaum(raum: Nothing) {
//    addOccupationToRaum(raum, null)
//  }
//
//  def addOccupationToRaum(raum: Nothing, endDate: Date) {
//    addOccupationToRaum(raum, endDate, false)
//  }
//
//  def addOccupationToRaum(raum: Nothing, endDate: Date, inBackground: Boolean) {
//    var occupations: List[Occupation] = raum.getOccupations
//    if (occupations == null) {
//      occupations = new ArrayList[Occupation]
//    }
//    val iterator: Iterator[Occupation] = occupations.iterator
//    while (iterator.hasNext) {
//      val occupation: Occupation = iterator.next
//      val parseUser: ParseUser = occupation.getUser
//      if (ParseUser.getCurrentUser.getObjectId == parseUser.getObjectId) {
//        iterator.remove
//      }
//    }
//    val occupation: Occupation = new Occupation
//    occupation.setUser(ParseUser.getCurrentUser)
//    if (endDate != null) {
//      occupation.setEndDate(endDate)
//    }
//    occupations.add(occupation)
//    try {
//      if (inBackground) {
//        raum.saveInBackground
//      }
//      else {
//        raum.save
//      }
//    }
//    catch {
//      case e: ParseException => {
//        e.printStackTrace
//      }
//    }
//  }
//
//  def clearOccupationsFromRaum(raum: Nothing, inBackground: Boolean) {
//    var occupations: List[Occupation] = raum.getOccupations
//    if (occupations == null) {
//      occupations = new ArrayList[Occupation]
//    }
//    val iterator: Iterator[Occupation] = occupations.iterator
//    while (iterator.hasNext) {
//      val occupation: Occupation = iterator.next
//      val parseUser: ParseUser = occupation.getUser
//      if (ParseUser.getCurrentUser.getObjectId == parseUser.getObjectId) {
//        iterator.remove
//      }
//    }
//    try {
//      if (inBackground) {
//        raum.saveInBackground
//      }
//      else {
//        raum.save
//      }
//    }
//    catch {
//      case e: ParseException => {
//        e.printStackTrace
//      }
//    }
//  }
//}
