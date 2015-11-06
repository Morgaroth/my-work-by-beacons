package io.github.morgaroth.android.utilities.fragments

import android.annotation.TargetApi
import android.app.{Activity, Fragment}
import android.content.{Intent, Context}
import android.os.Build
import android.view.View
import android.view.View.OnClickListener
import android.widget.{TextView, Button}
import io.github.morgaroth.android.utilities.{BluetoothUtils, ImplicitContext, logger}

import scala.language.implicitConversions

class ViewFactory(v: View) {
  def findBtn(id: Int) = Option(v.findViewById(id).asInstanceOf[Button])

  def findText(id: Int) = v.findViewById(id).asInstanceOf[TextView]
}

trait SmartFragment extends Fragment with ImplicitContext with logger {
  override implicit def implicitlyVisibleThisAsContext: Context = getActivity

  implicit def wrapToViewFactory(v: View): ViewFactory = new ViewFactory(v)

  //  implicit def convertAnonymousFunctionToViewOnClickListener(fun: => Unit): OnClickListener = new OnClickListener {
  //    override def onClick(v: View): Unit = fun
  //  }
  implicit def convertAnonymousFunctionToViewOnClickListener(fun: () => Any): OnClickListener = new OnClickListener {
    override def onClick(v: View): Unit = {
      //      println("calling fun...")
      fun()
    }
  }

  implicit def convertAnonymousFunctionToViewOnClickListener(fun: (View) => Unit): OnClickListener = new OnClickListener {
    override def onClick(v: View): Unit = fun(v)
  }

  def onAttachFunction(ctx: Context)

  @TargetApi(23)
  abstract override def onAttach(context: Context) {
    super.onAttach(context)
    onAttachFunction(context)
  }

  /*
   * Deprecated on API 23
   * Use onAttachToContext instead
   */
  @SuppressWarnings(Array("deprecation"))
  abstract override def onAttach(activity: Activity) {
    super.onAttach(activity)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      onAttachFunction(activity)
    }
  }

  def handleOnActivityResult: PartialFunction[(Int, Int, Intent), Unit] = PartialFunction.empty

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {
    val standardHandleActivityResult: PartialFunction[(Int, Int, Intent), Unit] = PartialFunction {
      case (req, res, d) => super.onActivityResult(requestCode, resultCode, d): Unit
    }
    handleOnActivityResult.orElse(standardHandleActivityResult)((requestCode, resultCode, data))
  }
}
