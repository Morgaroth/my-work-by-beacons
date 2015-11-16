package io.github.morgaroth.android.mywork.fragments

import java.text.SimpleDateFormat
import java.util.{TimeZone, Date, Calendar}

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.TextView
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.fragments.WorkDetailsFragment.{Adapter, Callbacks}
import io.github.morgaroth.android.mywork.storage.Work
import io.github.morgaroth.android.utilities.With
import io.github.morgaroth.android.utilities.fragments.{FragmentCompanion, SmartFragment, ViewManaging}

/**
 * Created by mateusz on 15.11.15.
 */

object WorkDetailsFragment extends FragmentCompanion[HelloFragment] with ViewManaging {
  def newInstance(w: Work) = new WorkDetailsFragment(w)

  trait Callbacks {}

  class VH(v: View) extends RecyclerView.ViewHolder(v) {
    val date: TextView = v.findText(R.id.date)
    val rawDate: TextView = v.findText(R.id.raw)
  }

  class Adapter(var data: List[Long] = List.empty) extends RecyclerView.Adapter[VH] {
    def setData(newData: List[Long]) = {
      data = newData.sorted
      notifyDataSetChanged()
      newData
    }

    val cal = Calendar.getInstance()
    val formatter = new SimpleDateFormat("hh:mm dd/MM/yyyy a")
    formatter.setTimeZone(TimeZone.getTimeZone("Poland"))

    def getDate(time: Long) = formatter.format(new Date(time))

    override def getItemCount: Int = data.size

    override def onBindViewHolder(holder: VH, position: Int): Unit = {
      val d = data(position)
      holder.date.setText(getDate(d))
      holder.rawDate.setText(d.toString)
    }

    override def onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
      new VH(LayoutInflater.from(parent.getContext).inflate(R.layout.in_work_elem, parent, false))
  }

}

class WorkDetailsFragment(w: Work) extends SmartFragment {

  val adapter = new Adapter(w.InWorks())

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    super.onCreateView(inflater, container, savedInstanceState)

    With(inflater.inflate(R.layout.work_details_layout, container, false)) { l =>
      val rv = l.findViewById(R.id.my_recycler_view).asInstanceOf[RecyclerView]
      rv.setHasFixedSize(true)
      rv.setLayoutManager(new LinearLayoutManager(getActivity))
      rv.setAdapter(adapter)
      l.findText(R.id.header).setText(w.name)
    }
  }

  override def onAttachFunction(ctx: Context): Unit = ctx.asInstanceOf[Callbacks]
}
