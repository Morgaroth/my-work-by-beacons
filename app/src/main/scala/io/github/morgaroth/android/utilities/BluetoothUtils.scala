package io.github.morgaroth.android.utilities

import android.app.{Activity, Fragment}
import android.bluetooth.{BluetoothAdapter, BluetoothManager}
import android.content.{Intent, Context}
import io.github.morgaroth.android.mywork.activities.MainActivity

object BluetoothUtils {
  val REQUEST_CODE_ENABLE_BLUETOOTH = 1

  def isBluetoothEnabled(implicit context: Context): Boolean = {
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE).asInstanceOf[BluetoothManager]
    bluetoothManager.getAdapter.isEnabled
  }

  def enableBT(caller: Fragment) = {
    val intent: Intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
    caller.startActivityForResult(intent, MainActivity.REQUEST_CODE_ENABLE_BLUETOOTH)
  }

  def handleBTEnabled(succ: Intent => Unit, failed: Intent => Unit): PartialFunction[(Int, Int, Intent), Unit] = {
    case (REQUEST_CODE_ENABLE_BLUETOOTH, Activity.RESULT_OK, data) =>
      succ(data)
    case (REQUEST_CODE_ENABLE_BLUETOOTH, Activity.RESULT_CANCELED, data) =>
      failed(data)
  }
}

