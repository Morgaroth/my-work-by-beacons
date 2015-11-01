package io.github.morgaroth.android.utilities

import android.app.Activity
import android.content.{Context, Intent}

import scala.language.reflectiveCalls
import scala.reflect._
/**
 * Created by mateusz on 31.10.15.
 */
trait SmartActivity extends ImplicitContext with logger {
  this: Activity =>

  def Intent[T:ClassTag](implicit ctx: Context) = new Intent(ctx, classTag[T].runtimeClass)

  val toast = Toasts
}
