package io.github.morgaroth.android.mywork.fragments

import android.content.{Context, Intent}
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.fragments.BTFragment.Callbacks
import io.github.morgaroth.android.utilities.fragments.{FragmentCompanion, SmartFragment}
import io.github.morgaroth.android.utilities.{BluetoothUtils, With}

object BTFragment extends FragmentCompanion[BTFragment] {
  def newInstance = new BTFragment

  trait Callbacks {
    def BTEnabled(): Unit

    def BTNotEnabled(): Unit
  }

}


class BTFragment extends SmartFragment {

  var attached: Option[Callbacks] = _

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    //    setRetainInstance(true)
    log.info("created")
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    super.onCreateView(inflater, container, savedInstanceState)
    log.debug(s"creating fragment $this")
    if (BluetoothUtils.isBluetoothEnabled) {
      inflater.inflate(R.layout.fragment_bt_enabled, container, false)
    } else {
      With(inflater.inflate(R.layout.fragment_bt_disabled, container, false)) { l =>
        l.findBtn(R.id.enable_funcking_bt_button)
          .map(_.setOnClickListener(() => BluetoothUtils.enableBT(this)))
          .getOrElse(log.warn("not found button in bt_disabled layout"))
      }
    }
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {
    val a: PartialFunction[(Int, Int, Intent), Unit] = PartialFunction {
      case (req, res, d) => super.onActivityResult(requestCode, resultCode, d): Unit
    }
    val enabled: PartialFunction[(Int, Int, Intent), Unit] = BluetoothUtils.handleBTEnabled(
    { _ => log.info("bt enabled") }, { _ => log.info("BT not enabled") }
    )
    enabled.orElse(a)((requestCode, resultCode, data))
  }

  def onAttachFunction(context: Context): Unit = {
    try {
      attached = Some(context.asInstanceOf[BTFragment.Callbacks])
      log.info(s"to $this attached $attached")
    } catch {
      case t: Throwable =>
        log.error(s"attaching ${t.getMessage}")
    }
  }

  override def handleOnActivityResult: PartialFunction[(Int, Int, Intent), Unit] = {
    BluetoothUtils.handleBTEnabled(
    { _ =>
      log.info("bt enabled")
      attached.map(_.BTEnabled()).getOrElse(log.warn("BT not enabled, but fragment detached"))
    }, { _ =>
      log.info("BT not enabled")
      attached.map(_.BTNotEnabled()).getOrElse(log.warn("BT not enabled, but fragment detached"))
    }
    )
  }
}
