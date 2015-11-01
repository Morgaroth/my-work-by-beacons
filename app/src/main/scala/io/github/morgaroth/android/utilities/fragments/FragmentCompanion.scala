package io.github.morgaroth.android.utilities.fragments

import android.app.Fragment
import android.os.Bundle

import scala.language.implicitConversions

/**
 * Created by mateusz on 01.11.15.
 */

class BundleBuilder {
  val b = new Bundle()

  def put(key: String, value: String): BundleBuilder = {
    b.putString(key, value)
    this
  }

  def put(key: String, value: Int): BundleBuilder = {
    b.putInt(key, value)
    this
  }

  def put(key: String, value: Seq[Int])(implicit du: DummyImplicit): BundleBuilder = {
    b.putIntArray(key, value.toArray)
    this
  }

  def put(key: String, value: Seq[String])(implicit du: DummyImplicit, du1: DummyImplicit): BundleBuilder = {
    b.putStringArray(key, value.toArray)
    this
  }
}

trait FragmentCompanion[T <: Fragment] {

  implicit def toBundle(b: BundleBuilder): Bundle = b.b.clone().asInstanceOf[Bundle]

  def newBundle = new BundleBuilder
}
