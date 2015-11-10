package io.github.morgaroth.android.mywork.fragments

import android.app.AlertDialog
import android.content.{ComponentName, Context, Intent, ServiceConnection}
import android.os.{Bundle, IBinder}
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view.View.OnClickListener
import android.view.{LayoutInflater, View, ViewGroup}
import com.kontakt.sdk.android.device.BeaconDevice
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.fragments.BeaconsFragment.{OnItemClickListener, Adapter, Callbacks}
import io.github.morgaroth.android.mywork.logic.BeaconInTheAir
import io.github.morgaroth.android.mywork.services.BeaconMonitorService
import io.github.morgaroth.android.mywork.services.BeaconMonitorService.{BeaconMonitorBinder, BeaconsListener}
import io.github.morgaroth.android.utilities.fragments.{AttachedActivity, SmartFragment}
import io.github.morgaroth.android.utilities.{With, fragments}

import scala.collection.mutable

object BeaconsFragment extends fragments.FragmentCompanion[BeaconsFragment] with fragments.ViewManaging {
  def newInstance = new BeaconsFragment

  trait Callbacks

  trait OnItemClickListener[T] {
    def onItemClick(item: T, v: View)
  }

  //  class
  class ViewHolder(view: View) extends RecyclerView.ViewHolder(view) {
    val header = view.findText(R.id.header)
    val subheader = view.findText(R.id.subheader)
    val more = view.findText(R.id.more)
  }

  class Adapter(rv: RecyclerView, onClickListener: OnItemClickListener[BeaconInTheAir]) extends RecyclerView.Adapter[ViewHolder] {
    var data: List[BeaconInTheAir] = List.empty

    def setData(newData: List[BeaconInTheAir]) = {
      data = newData.sortBy(_.beacon.getAccuracy)
      notifyDataSetChanged()
      newData
    }

    override def getItemCount: Int = data.size

    override def onBindViewHolder(holder: ViewHolder, position: Int): Unit = {
      val d: BeaconInTheAir = data(position)
      val b: BeaconDevice = d.beacon
      holder.header.setText(s"${b.getName} ${b.getUniqueId}")
      holder.subheader.setText(s"${b.getAccuracy.toString} ${b.getProximity}")
      holder.more.setText(d.region.toString)
      holder.itemView.setOnClickListener(new OnClickListener {
        override def onClick(v: View): Unit = {
          val ch = rv.getChildLayoutPosition(v)
          onClickListener.onItemClick(data(ch), v)
        }
      })
    }

    override def onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
      new ViewHolder(LayoutInflater.from(parent.getContext).inflate(R.layout.beacon_elem, parent, false))
  }

}


class BeaconsFragment extends SmartFragment with AttachedActivity[Callbacks] with OnItemClickListener[BeaconInTheAir] {

  var connectedService: BeaconMonitorBinder = _

  var adapter: Adapter = _

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
      adapter = new Adapter(rv, this)
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

  override def onItemClick(item: BeaconInTheAir, v: View): Unit = {
    log.info(s"clicked beacon ${item.beacon.getUniqueId}")
    val a = new AlertDialog.Builder(getActivity)
    a.setTitle("Beacon").setMessage(s"Beacon (${item.beacon.getUniqueId}) is\n${item.beacon.getProximity}")
    a.setPositiveButton("OK", null)
    a.show()
  }
}
