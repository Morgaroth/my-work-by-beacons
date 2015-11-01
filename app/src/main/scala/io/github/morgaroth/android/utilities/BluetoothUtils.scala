package io.github.morgaroth.android.utilities

import android.bluetooth.BluetoothManager
import android.content.Context

/**
 * Created by mateusz on 31.10.15.
 */
object BluetoothUtils {
  def isBluetoothEnabled(implicit context: Context): Boolean = {
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE).asInstanceOf[BluetoothManager]
    bluetoothManager.getAdapter.isEnabled
  }

  def isBluetoothDisabled(implicit context: Context) = !isBluetoothEnabled
}

