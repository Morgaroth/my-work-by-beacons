package io.github.morgaroth.android.mywork.fragments

import android.app.AlertDialog
import android.app.AlertDialog.Builder
import android.content.DialogInterface.OnClickListener
import android.content.{Context, DialogInterface}
import android.os.Bundle
import android.support.v7.widget.{LinearLayoutManager, RecyclerView}
import android.view.MenuItem.OnMenuItemClickListener
import android.view.View.OnLongClickListener
import android.view._
import android.widget.{EditText, TextView}
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.fragments.WorksFragment.{AdapterCallbacks, Adapter, Callbacks}
import io.github.morgaroth.android.mywork.storage.{ParseManager, Work}
import io.github.morgaroth.android.utilities.fragments._
import io.github.morgaroth.android.utilities.{Toasts, With}

object WorksFragment extends FragmentCompanion[HelloFragment] with ViewManaging {
  def newInstance = new WorksFragment

  trait Callbacks {
    def nothing(): Unit
  }

  trait AdapterCallbacks {
    def deleteObject(w: Work): Unit
  }

  class VH(v: View) extends RecyclerView.ViewHolder(v) {
    val header: TextView = v.findText(R.id.header)
    val subheader: TextView = v.findText(R.id.subheader)
    //    val more = v.findText(R.id.more)
  }

  class Adapter(var data: List[Work] = List.empty, listener: AdapterCallbacks) extends RecyclerView.Adapter[VH] {
    def setData(newData: List[Work]) = {
      data = newData
      notifyDataSetChanged()
      newData
    }

    override def getItemCount: Int = data.size

    override def onBindViewHolder(holder: VH, position: Int): Unit = {
      val d = data(position)
      holder.header.setText(d.name)
      holder.subheader.setText(s"determinants: ${d.Determinants.apply.size}")
      holder.itemView.setOnLongClickListener(new OnLongClickListener {
        override def onLongClick(v: View): Boolean = {
          println(s"Long clck on item with $data")
          new Builder(holder.itemView.getContext).setTitle("Really delete?").setPositiveButton("Yes", new OnClickListener {
            override def onClick(dialog: DialogInterface, which: Int): Unit = {
              listener.deleteObject(d)
            }
          }).show()
          true
        }
      })
    }

    override def onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
      new VH(LayoutInflater.from(parent.getContext).inflate(R.layout.work_elem, parent, false))
  }

}


class WorksFragment extends SmartFragment with AttachedActivity[Callbacks] with AdapterCallbacks {

  var works: List[Work] = Work.all.getOrElse(List.empty)
  var worksNames: Set[String] = works.map(_.name).toSet
  val adapter = new Adapter(works, this)

  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    super.onCreateView(inflater, container, savedInstanceState)
    setHasOptionsMenu(true)
    With(inflater.inflate(R.layout.fragment_recycler_view, container, false)) { l =>
      val rv = l.findViewById(R.id.my_recycler_view).asInstanceOf[RecyclerView]
      rv.setHasFixedSize(true)
      rv.setLayoutManager(new LinearLayoutManager(getActivity))
      rv.setAdapter(adapter)
    }
  }


  override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater): Unit = {
    val fav = menu.add("add")
    fav.setIcon(android.R.drawable.btn_plus)
    fav.setVisible(true)
    fav.setOnMenuItemClickListener(new OnMenuItemClickListener {
      override def onMenuItemClick(item: MenuItem): Boolean = {
        fireAddWork()
        true
      }
    })
    fav.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
    super.onCreateOptionsMenu(menu, inflater)
  }


  def invalidateWorksSet(): Unit = {
    works = Work.all.getOrElse(List.empty)
    worksNames = works.map(_.name).toSet
    adapter.setData(works)
  }

  def addWork(newWorkName: String): Unit = {
    if (worksNames contains newWorkName) {
      Toasts.short(s"$newWorkName exists!")
    } else {
      val w = new Work
      w.name = newWorkName
      w.save()
      invalidateWorksSet()
    }
  }

  def fireAddWork() {
    val e = new EditText(getActivity)
    new AlertDialog.Builder(getActivity).setTitle("Provide work name:").setView(e).setPositiveButton("Done", new DialogInterface.OnClickListener {
      override def onClick(dialog: DialogInterface, which: Int): Unit = {
        addWork(e.getText.toString)
      }
    }).setNegativeButton("Abort", null)
      .show()
  }

  override def attachActivity(ctx: Context): Callbacks = ctx.asInstanceOf[Callbacks]

  override def deleteObject(w: Work): Unit = {
    w.delete()
    invalidateWorksSet()
  }
}
