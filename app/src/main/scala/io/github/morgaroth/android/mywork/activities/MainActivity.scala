package io.github.morgaroth.android.mywork.activities

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.{FloatingActionButton, Snackbar}
import android.view.{Menu, MenuItem, View}
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.services.BeaconMonitorService
import io.github.morgaroth.android.utilities.{BluetoothUtils, SmartActivity}

object MainActivity{
  val REQUEST_CODE_ENABLE_BLUETOOTH = 1
}

class MainActivity extends Activity with SmartActivity {


  protected override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val fab: FloatingActionButton = findViewById(R.id.fab).asInstanceOf[FloatingActionButton]
    fab.setOnClickListener(new View.OnClickListener() {
      def onClick(view: View) {
        Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show()
      }
    })

    if (!BluetoothUtils.isBluetoothEnabled) {
      log.info("bluetooth isn't enabled")
      val intent: Intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
      startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH)
    } else {
      log.info("bluetooth is enabled")
      startService(Intent[BeaconMonitorService])
    }
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
    if (requestCode == REQUEST_CODE_ENABLE_BLUETOOTH) {
      if (resultCode == RESULT_OK) {
        toast.short("BT enabled")
        bindBeaconMonitor
      } else {
        toast.short("FUCK YOU!")
      }
    } else {
      log.info(s"other activity result $requestCode, $resultCode")
      super.onActivityResult(requestCode, resultCode, data)
    }
  }
}
