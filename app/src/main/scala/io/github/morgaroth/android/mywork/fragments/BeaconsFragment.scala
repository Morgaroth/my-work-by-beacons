package io.github.morgaroth.android.mywork.fragments

import android.content.Context
import io.github.morgaroth.android.utilities.fragments.{FragmentCompanion, SmartFragment}

object BeaconsFragment extends FragmentCompanion[BeaconsFragment] {
  def newInstance = new BeaconsFragment

  trait Callbacks

}


class BeaconsFragment extends SmartFragment {
  var attached: Option[BeaconsFragment.Callbacks] = _



  override def onAttachFunction(ctx: Context): Unit = {
    try {
      attached = Some(ctx.asInstanceOf[BeaconsFragment.Callbacks])
      log.info(s"to $this attached $attached")
    } catch {
      case t: Throwable =>
        log.error(s"attaching ${t.getMessage}")
    }
  }

}
