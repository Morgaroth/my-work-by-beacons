package io.github.morgaroth.android.mywork.fragments

import java.util.UUID

import android.content.{ComponentName, Context, Intent, ServiceConnection}
import android.os.{Bundle, IBinder}
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{BaseAdapter, TextView}
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.fragments.BeaconsFragment.{Adapter, Callbacks}
import io.github.morgaroth.android.mywork.logic.BeaconInTheAir
import io.github.morgaroth.android.mywork.services.BeaconMonitorService
import io.github.morgaroth.android.mywork.services.BeaconMonitorService.{BeaconMonitorBinder, BeaconsListener}
import io.github.morgaroth.android.utilities.{With, fragments}
import io.github.morgaroth.android.utilities.fragments.{AttachedActivity, SmartFragment}

import scala.collection.mutable

object BeaconsFragment extends fragments.FragmentCompanion[BeaconsFragment] with fragments.ViewManaging {
  def newInstance = new BeaconsFragment

  trait Callbacks


  //  class
  class ViewHolder(view: View) extends RecyclerView.ViewHolder(view) {
    val name = view.findText(R.id.text1)
    val dsc = view.findText(R.id.text2)
    val id = view.findText(R.id.beaconId)
  }

  class Adapter extends RecyclerView.Adapter[ViewHolder] {
    var data: mutable.MutableList[BeaconInTheAir] = mutable.MutableList.empty
    var ids: Set[String] = Set.empty

    def setData(newData: List[BeaconInTheAir]) = {
      val filtered: List[BeaconInTheAir] = newData.filterNot(ids contains _.beacon.getUniqueId)
      data ++= filtered
      ids ++= filtered.map(_.beacon.getUniqueId)
      notifyDataSetChanged()
      newData
    }

    override def getItemCount: Int = data.size

    override def onBindViewHolder(holder: ViewHolder, position: Int): Unit = {
      holder.name.setText(data(position).beacon.getName)
      holder.dsc.setText(data(position).beacon.toString)
      holder.id.setText(data(position).beacon.getUniqueId)
    }

    override def onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
      new ViewHolder(LayoutInflater.from(parent.getContext).inflate(R.layout.beacon_elem, parent, false))
  }

}


class BeaconsFragment extends SmartFragment with AttachedActivity[Callbacks] {

  var connectedService: BeaconMonitorBinder = _

  val adapter = new Adapter

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
          adapter.setData(bcns)
        }

        override def monitoringStarted: Unit = {
          log.info("started beacons monitoring")
        }

        override def stopMonitoring: Unit = {
          log.info("stopped beacons monitoring")
        }
      })
    }
  }

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    super.onCreateView(inflater, container, savedInstanceState)

    val intent = new Intent(getActivity, classOf[BeaconMonitorService])
    val r = getActivity.startService(intent)
    log.info(s"starting service end with $r")
    val e = getActivity.bindService(intent, connection, 0)
    log.info(s"binding service end with $e")

    With(inflater.inflate(R.layout.fragment_beacons, container, false)) { l =>
      val rv = l.findViewById(R.id.my_recycler_view).asInstanceOf[RecyclerView]
      rv.setHasFixedSize(true)
      rv.setAdapter(adapter)
      rv.setLayoutManager(new LinearLayoutManager(getActivity))
    }
  }


  override def onDestroyView(): Unit = {
    getActivity.unbindService(connection)
    super.onDestroyView()
  }

  override def attachActivity(ctx: Context): Callbacks = ctx.asInstanceOf[BeaconsFragment.Callbacks]
}
