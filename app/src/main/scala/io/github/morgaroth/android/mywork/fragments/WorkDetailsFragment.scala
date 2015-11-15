package io.github.morgaroth.android.mywork.fragments

import android.content.Context
import android.os.Bundle
import android.view.{View, ViewGroup, LayoutInflater}
import io.github.morgaroth.android.mywork.fragments.WorkDetailsFragment.Callbacks
import io.github.morgaroth.android.mywork.storage.Work
import io.github.morgaroth.android.utilities.fragments.{SmartFragment, ViewManaging, FragmentCompanion}

/**
 * Created by mateusz on 15.11.15.
 */

object WorkDetailsFragment extends FragmentCompanion[HelloFragment] with ViewManaging {
  def newInstance(w: Work) = new WorkDetailsFragment(w)

  trait Callbacks {
    def nothing(): Unit
  }

}

class WorkDetailsFragment(w: Work) extends SmartFragment {


  override def onCreateView(inflater: LayoutInflater, container: ViewGroup, savedInstanceState: Bundle): View = {
    super.onCreateView(inflater, container, savedInstanceState)

  }

  override def onAttachFunction(ctx: Context): Unit = ctx.asInstanceOf[Callbacks]
}
