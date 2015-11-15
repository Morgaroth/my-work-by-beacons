package io.github.morgaroth.android.utilities.activities

import _root_.io.github.morgaroth.android.utilities.{ImplicitContext, Toasts, logger}
import android.app.Activity
import android.content.{Context, Intent}
import android.view.MenuItem

import scala.language.reflectiveCalls
import scala.reflect._

/**
 * Created by mateusz on 31.10.15.
 */
trait smart extends Activity with ImplicitContext with logger {

  override implicit def implicitlyVisibleThisAsContext: Context = this

  def Intent[T: ClassTag](implicit ctx: Context) = new Intent(ctx, classTag[T].runtimeClass)

  val toast = Toasts

  def handleOptionsMenuItemSelected: PartialFunction[Int, Any] = {
    case any if false =>
  }

  abstract override def onOptionsItemSelected(item: MenuItem): Boolean = {
    val default: PartialFunction[Int, Boolean] = {
      case _ => super.onOptionsItemSelected(item)
    }
    (handleOptionsMenuItemSelected.andThen(_ => true) orElse default)(item.getItemId)
  }

}


