package io.github.morgaroth.android.mywork.fragments

import java.lang.reflect

import android.app.{Dialog, AlertDialog}
import android.app.AlertDialog.Builder
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.content._
import android.os.{Bundle, IBinder}
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view.View.OnClickListener
import android.view._
import android.widget.Filter.FilterResults
import android.widget.{Filter, Filterable}
import com.kontakt.sdk.android.device.BeaconDevice
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.fragments.BeaconsFragment.{OnItemClickListener, Adapter, Callbacks}
import io.github.morgaroth.android.mywork.logic.BeaconInTheAir
import io.github.morgaroth.android.mywork.services.BeaconMonitorService
import io.github.morgaroth.android.mywork.services.BeaconMonitorService.{BeaconMonitorBinder, BeaconsListener}
import io.github.morgaroth.android.mywork.storage.{Work, ParseManager, Beacon}
import io.github.morgaroth.android.utilities.fragments.{AttachedActivity, SmartFragment}
import io.github.morgaroth.android.utilities.{With, fragments}

import scala.collection.mutable
import scala.language.postfixOps

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

  class Adapter(rv: RecyclerView, onClickListener: OnItemClickListener[BeaconInTheAir]) extends RecyclerView.Adapter[ViewHolder] with Filterable {
    var data: List[BeaconInTheAir] = List.empty
    var rawData: List[BeaconInTheAir] = List.empty

    def setData(newData: List[BeaconInTheAir]) = {
      rawData = newData.sortBy(_.beacon.getAccuracy)
      data = rawData
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

    override def getFilter: Filter = {
      new Filter {
        override def publishResults(constraint: CharSequence, results: FilterResults): Unit = {
          data = results.values.asInstanceOf[List[BeaconInTheAir]]
          notifyDataSetChanged()
        }

        override def performFiltering(constraint: CharSequence): FilterResults = {
          val r = Option(constraint.toString).filter(_.nonEmpty).map {
            case "unknown" => rawData.filter(_.known.isEmpty)
            case "known" => rawData.filter(_.known.nonEmpty)
            case _ => rawData
          }.getOrElse(rawData)

          With(new FilterResults) { fr =>
            fr.count = r.size
            fr.values = r
          }
        }
      }
    }
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
      setHasOptionsMenu(true)
      connectedService = service.asInstanceOf[BeaconMonitorBinder]
      connectedService.service.addDataListener(new BeaconsListener {
        override def onBeacons(bcns: List[BeaconInTheAir]): Unit = {
          log.info(s"beacons on the air $bcns")
          adapter.setData(bcns.map { bia =>
            knownBeacons
              .get(bia.beacon.getUniqueId)
              .map(x => bia.copy(known = Some(x._2)))
              .getOrElse(bia)
          })
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


  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case R.id.action_search =>
        log.info("used search menu from fragment")
        val options: Array[CharSequence] = Array[CharSequence]("unknown", "known", "all")
        new Builder(getActivity)
          .setItems(options, new DialogInterface.OnClickListener {
            override def onClick(dialog: DialogInterface, which: Int): Unit = {
              log.info(s"clicked $which")
              adapter.getFilter.filter(options(which))
            }
          }).show()
        true
      case _ => false
    }
  }

  override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater): Unit = {
    super.onCreateOptionsMenu(menu, inflater)
  }

  var knownBeacons = Map.empty[String, (Beacon, Work)]

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    super.onCreateView(inflater, container, savedInstanceState)

    val intent = new Intent(getActivity, classOf[BeaconMonitorService])
    val r = getActivity.startService(intent)
    log.info(s"starting service end with $r")
    val e = getActivity.bindService(intent, connection, 0)
    log.info(s"binding service end with $e")

    knownBeacons = loadBeacons(loadWorks)

    With(inflater.inflate(R.layout.fragment_recycler_view, container, false)) { l =>
      val rv = l.findViewById(R.id.my_recycler_view).asInstanceOf[RecyclerView]
      adapter = new Adapter(rv, this)
      rv.setHasFixedSize(true)
      rv.setAdapter(adapter)
      rv.setLayoutManager(new LinearLayoutManager(getActivity))
    }
  }


  def loadBeacons(works: List[Work]): Map[String, (Beacon, Work)] = {
    works.flatMap(w => w.Determinants.apply.map(b => b.beaconId ->(b, w))) toMap
  }

  def loadWorks: List[Work] = {
    ParseManager.works.getOrElse(List.empty)
  }

  var works = List.empty[Work]

  def updateData(): Unit = {
    works = loadWorks
    knownBeacons = loadBeacons(works)
  }

  override def onDestroyView(): Unit = {
    getActivity.unbindService(connection)
    super.onDestroyView()
  }

  override def attachActivity(ctx: Context): Callbacks = ctx.asInstanceOf[BeaconsFragment.Callbacks]

  override def onItemClick(item: BeaconInTheAir, v: View): Unit = {
    log.info(s"clicked beacon ${item.beacon.getUniqueId}")
    updateData()
    val workNames = works.map(_.name).toArray[CharSequence]
    val a = new AlertDialog.Builder(getActivity)
    new Builder(getActivity)
      .setItems(workNames, new DialogInterface.OnClickListener {
        override def onClick(dialog: DialogInterface, which: Int): Unit = {
          log.info(s"clicked $which")
          val work: Work = works.find(_.name == workNames(which)).get
          work.Determinants += Beacon.from(item.beacon)
          work.save()
          updateData()
        }
      }).show()
  }
}
