package io.github.morgaroth.android.mywork.fragments

import android.content.{Context, Intent}
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.fragments.BTFragment.Callbacks
import io.github.morgaroth.android.utilities.fragments.{AttachedActivity, FragmentCompanion, SmartFragment}
import io.github.morgaroth.android.utilities.{BluetoothUtils, With}

object BTFragment extends FragmentCompanion[BTFragment] {
  def newInstance = new BTFragment

  trait Callbacks {
    def BTEnabled(): Unit

    def BTNotEnabled(): Unit
  }

}

class BTFragment extends SmartFragment with AttachedActivity[Callbacks] {

  override def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)
    //    setRetainInstance(true)
    log.info("created")
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    super.onCreateView(inflater, container, savedInstanceState)
    log.debug(s"creating fragment $this")
    if (BluetoothUtils.isBluetoothEnabled) {
      With(inflater.inflate(R.layout.fragment_bt_enabled, container, false)) { l =>
        l.findBtn(R.id.button).setOnClickListener(() => {
          attached.map(_.BTEnabled()).getOrElse(log.warn("BT enabled, but fragment detached"))
        })
      }
    } else {
      With(inflater.inflate(R.layout.fragment_bt_disabled, container, false)) { l =>
        l.findBtn(R.id.button).setOnClickListener(() => BluetoothUtils.enableBT(this))
      }
    }
  }

  override def attachActivity(ctx: Context): Callbacks = ctx.asInstanceOf[BTFragment.Callbacks]

  override def handleOnActivityResult: PartialFunction[(Int, Int, Intent), Unit] = {
    BluetoothUtils.handleBTEnabled(
    { _ =>
      log.info("bt enabled")
      attached.map(_.BTEnabled()).getOrElse(log.warn("BT enabled, but fragment detached"))
    }, { _ =>
      log.info("BT not enabled")
      attached.map(_.BTNotEnabled()).getOrElse(log.warn("BT not enabled, but fragment detached"))
    }
    )
  }
}
