package io.github.morgaroth.android.mywork.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.{FloatingActionButton, Snackbar}
import android.view.{Menu, MenuItem, View}
import android.widget.Toolbar
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.fragments._
import io.github.morgaroth.android.mywork.storage.Work
import io.github.morgaroth.android.utilities.activities.smart
import io.github.morgaroth.android.utilities.{BluetoothUtils, FragmentContainer, SmartFragmentActivity}

object MainActivity {
  val REQUEST_CODE_ENABLE_BLUETOOTH = 1
  val COMMAND = "command"
}

class MainActivity extends Activity with smart with SmartFragmentActivity
with BTFragment.Callbacks with HelloFragment.Callbacks with BeaconsFragment.Callbacks with WorksFragment.Callbacks {

  implicit lazy val container = FragmentContainer(R.id.container)

  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setActionBar(findViewById(R.id.toolbar).asInstanceOf[Toolbar])

    val fab: FloatingActionButton = findViewById(R.id.fab).asInstanceOf[FloatingActionButton]
    fab.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()
      }
    })
    loadInitialFragment {
      if (BluetoothUtils.isBluetoothEnabled) HelloFragment.newInstance else BTFragment.newInstance
    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.menu_main, menu)
    true
  }

  override def handleOptionsMenuItemSelected = {
    case R.id.action_load_beacons =>
      replaceFragment(BeaconsFragment.newInstance)
    case R.id.action_load_works =>
      replaceFragment(WorksFragment.newInstance)
  }

  def bindBeaconMonitor = {
    log.info("binding to beaconsmonitor")
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    log.info(s"activiry result $requestCode $resultCode $data")
    super.onActivityResult(requestCode, resultCode, data)
  }

  override def BTEnabled(): Unit = {
    log.info("user enabled BT")
    replaceFragment(HelloFragment.newInstance)
  }

  override def BTNotEnabled(): Unit = {
    // todo what if not enabled
    log.info("user NOT enabled BT")
  }

  protected override def onNewIntent(intent: Intent) = {
    super.onNewIntent(intent)
    log.info(s"onIntent $intent")
    Option(intent.getIntExtra(MainActivity.COMMAND, -1)).filter(_ != -1).map {
      case MainActivity.REQUEST_CODE_ENABLE_BLUETOOTH if !BluetoothUtils.isBluetoothEnabled =>
        replaceFragment(BTFragment.newInstance)
      case _ =>
    }
  }

  override def wantBeacons(): Unit = {
    log.info("user wants beacons")
    replaceFragment(BeaconsFragment.newInstance)
  }

  override def wantWorks(): Unit = {
    log.info("user wants works")
    replaceFragment(WorksFragment.newInstance)
  }

  override def loadWorkDetails(w: Work): Unit = {
    replaceFragment(WorkDetailsFragment.newInstance(w))
  }
}
