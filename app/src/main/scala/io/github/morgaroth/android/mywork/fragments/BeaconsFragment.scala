package io.github.morgaroth.android.mywork.fragments

import android.content.{ComponentName, ServiceConnection, Intent, Context}
import android.os.{IBinder, Bundle}
import android.view.{View, ViewGroup, LayoutInflater}
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.fragments.BeaconsFragment.Callbacks
import io.github.morgaroth.android.mywork.logic.BeaconInTheAir
import io.github.morgaroth.android.mywork.services.BeaconMonitorService
import io.github.morgaroth.android.mywork.services.BeaconMonitorService.{BeaconsListener, BeaconMonitorBinder}
import io.github.morgaroth.android.utilities.fragments
import io.github.morgaroth.android.utilities.fragments._

object BeaconsFragment extends fragments.FragmentCompanion[BeaconsFragment] {
  def newInstance = new BeaconsFragment

  trait Callbacks

}


class BeaconsFragment extends SmartFragment with AttachedActivity[Callbacks] {

  var connectedService: Option[BeaconMonitorBinder] = _

  val connection = new ServiceConnection {
    override def onServiceDisconnected(name: ComponentName): Unit = {
      log.info(s"service disconnected $name")
    }

    override def onServiceConnected(name: ComponentName, service: IBinder): Unit = {
      log.info(s"connected to service $name, service $service")
      connectedService = Some(service.asInstanceOf[BeaconMonitorBinder])
      connectedService.foreach(_.service.addDataListener(new BeaconsListener {
        override def onBeacons(bcns: List[BeaconInTheAir]): Unit = {
          log.info(s"beacons on the air $bcns")
        }
      }))
    }
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    super.onCreateView(inflater, container, savedInstanceState)

    val intent = new Intent(getActivity, classOf[BeaconMonitorService])
    getActivity.bindService(intent, connection, 0)

    inflater.inflate(R.layout.fragment_beacons, container, false)
  }


  override def onDestroyView(): Unit = {
    connectedService.foreach(_ => getActivity.unbindService(connection))
  }

  override def attachActivity(ctx: Context): Callbacks = ctx.asInstanceOf[BeaconsFragment.Callbacks]
}
