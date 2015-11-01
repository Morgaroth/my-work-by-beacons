package io.github.morgaroth.android.utilities.activities

import _root_.io.github.morgaroth.android.utilities.{ImplicitContext, Toasts, logger}
import android.app.Activity
import android.content.{Context, Intent}

import scala.language.reflectiveCalls
import scala.reflect._
/**
 * Created by mateusz on 31.10.15.
 */
trait smart extends ImplicitContext with logger {
  this: Activity =>


  override implicit def implicitlyVisibleThisAsContext: Context = this

  def Intent[T:ClassTag](implicit ctx: Context) = new Intent(ctx, classTag[T].runtimeClass)

  val toast = Toasts
}


