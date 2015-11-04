package io.github.morgaroth.android.mywork.hooks

import android.content.{BroadcastReceiver, Context, Intent}
import io.github.morgaroth.android.mywork.services.BeaconMonitorService

class OnBootHook extends BroadcastReceiver {

  override def onReceive(context: Context, intent: Intent) {
    val myIntent = new Intent(context, classOf[BeaconMonitorService])
    context.startService(myIntent)
  }
}