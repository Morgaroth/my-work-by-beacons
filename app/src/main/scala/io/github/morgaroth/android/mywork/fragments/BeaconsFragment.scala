package io.github.morgaroth.android.mywork.fragments

import android.content.{ComponentName, Context, Intent, ServiceConnection}
import android.os.{Bundle, IBinder}
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.TextView
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.fragments.BeaconsFragment.Callbacks
import io.github.morgaroth.android.mywork.logic.BeaconInTheAir
import io.github.morgaroth.android.mywork.services.BeaconMonitorService
import io.github.morgaroth.android.mywork.services.BeaconMonitorService.{BeaconMonitorBinder, BeaconsListener}
import io.github.morgaroth.android.utilities.With
import io.github.morgaroth.android.utilities.fragments._

object BeaconsFragment extends FragmentCompanion[BeaconsFragment] {
  def newInstance = new BeaconsFragment

  trait Callbacks

}


class BeaconsFragment extends SmartFragment with AttachedActivity[Callbacks] {

  var connectedService: BeaconMonitorBinder = _

  val connection = new ServiceConnection {
    override def onServiceDisconnected(name: ComponentName): Unit = {
      log.info(s"service disconnected $name")
    }

    override def onServiceConnected(name: ComponentName, service: IBinder): Unit = {
      log.info(s"connected to service $name, service $service")
      connectedService = service.asInstanceOf[BeaconMonitorBinder]
      connectedService.service.addDataListener(new BeaconsListener {
        override def onBeacons(bcns: List[BeaconInTheAir]): Unit = {
          log.info(s"beacons on the air $bcns")
          t.setText(s"found ${bcns.size} beacons")
        }
      })
    }
  }

  var t: TextView = _

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    super.onCreateView(inflater, container, savedInstanceState)

    val intent = new Intent(getActivity, classOf[BeaconMonitorService])
//    val r = getActivity.startService(intent)
//    log.info(s"starting service end with $r")
    val e = getActivity.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    log.info(s"binding service end with $e")

    With(inflater.inflate(R.layout.fragment_beacons, container, false)) { l =>
      t = l.findText(R.id.container)
    }
  }


  override def onDestroyView(): Unit = {
    //    connectedService.foreach(_ => getActivity.unbindService(connection))
    super.onDestroyView()
  }

  override def attachActivity(ctx: Context): Callbacks = ctx.asInstanceOf[BeaconsFragment.Callbacks]
}
