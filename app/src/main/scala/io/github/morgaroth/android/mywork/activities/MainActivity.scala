package io.github.morgaroth.android.mywork.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.{FloatingActionButton, Snackbar}
import android.view.{Menu, MenuItem, View}
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.fragments.BTFragment
import io.github.morgaroth.android.utilities.activities.smart
import io.github.morgaroth.android.utilities.{FragmentContainer, SmartFragmentActivity}

object MainActivity {
  val REQUEST_CODE_ENABLE_BLUETOOTH = 1
}

class MainActivity extends Activity with smart with SmartFragmentActivity
with BTFragment.Callbacks {

  implicit lazy val container = FragmentContainer(R.id.container)

  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val fab: FloatingActionButton = findViewById(R.id.fab).asInstanceOf[FloatingActionButton]
    fab.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()
      }
    })


    //    if (!BluetoothUtils.isBluetoothEnabled) {
    //      log.info("bluetooth isn't enabled")
    //      val intent: Intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    //      startActivityForResult(intent, MainActivity.REQUEST_CODE_ENABLE_BLUETOOTH)
    //    } else {
    //      log.info("bluetooth is enabled")
    //      startService(Intent[BeaconMonitorService])
    //    }
    loadInitialFragment(BTFragment.newInstance)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    getMenuInflater.inflate(R.menu.menu_main, menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    val id: Int = item.getItemId
    if (id == R.id.action_settings) {
      true
    } else {
      super.onOptionsItemSelected(item)
    }
  }

  def bindBeaconMonitor = {
    log.info("binding to beaconsmonitor")
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    log.info(s"activiry result $requestCode $resultCode $data")
    super.onActivityResult(requestCode, resultCode, data)
  }

  override def any(): String = "dupa"
}
