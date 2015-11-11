package io.github.morgaroth.android.mywork.fragments

import android.content.Context
import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.fragments.HelloFragment.Callbacks
import io.github.morgaroth.android.utilities.fragments.{FragmentCompanion, AttachedActivity, SmartFragment}
import io.github.morgaroth.android.utilities.With


object HelloFragment extends FragmentCompanion[HelloFragment] {
  def newInstance = new HelloFragment

  trait Callbacks {
    def wantBeacons(): Unit

    def wantWorks(): Unit
  }

}

class HelloFragment extends SmartFragment with AttachedActivity[Callbacks] {
  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    super.onCreateView(inflater, container, savedInstanceState)
    With(inflater.inflate(R.layout.fragment_hello, container, false)) { l =>
      l.findBtn(R.id.go_beacons).setOnClickListener(() => attached.foreach(_.wantBeacons()))
      l.findBtn(R.id.go_works).setOnClickListener(() => attached.foreach(_.wantWorks()))
    }
  }

  override def attachActivity(ctx: Context): Callbacks = ctx.asInstanceOf[HelloFragment.Callbacks]
}
