package io.github.morgaroth.android.utilities

import android.app.{Activity, Fragment}

/**
 * Created by mateusz on 01.11.15.
 */

case class FragmentContainer(id: Int)

trait SmartFragmentActivity {
  this: Activity =>
  private val ACTUAL_FRAGMENT_TAG: String = "actual_fragment_" + getClass.getSimpleName

  var currentFragment: Fragment = _

  def replaceFragment[T <: Fragment](fragment: T)(implicit container: FragmentContainer) = {
    currentFragment = fragment
    getFragmentManager.beginTransaction.replace(container.id, fragment, ACTUAL_FRAGMENT_TAG).commit
    currentFragment
  }

  def loadInitialFragment[T <: Fragment](ifNone: => T)(implicit container: FragmentContainer) = {
    currentFragment = Option(getFragmentManager.findFragmentByTag(ACTUAL_FRAGMENT_TAG)).getOrElse {
      val fragment = ifNone
      getFragmentManager.beginTransaction.add(container.id, fragment, ACTUAL_FRAGMENT_TAG).commit
      fragment
    }
    currentFragment
  }
}
