package io.github.morgaroth.android.mywork.services

import java.util
import java.util.{Timer, TimerTask}

import android.app.{Notification, NotificationManager, PendingIntent, Service}
import android.bluetooth.BluetoothAdapter
import android.content.{BroadcastReceiver, Context, Intent, IntentFilter}
import android.os.{Binder, IBinder, RemoteException}
import android.support.annotation.Nullable
import com.kontakt.sdk.android.configuration.MonitorPeriod
import com.kontakt.sdk.android.connection.OnServiceBoundListener
import com.kontakt.sdk.android.device.{BeaconDevice, Region}
import com.kontakt.sdk.android.manager.BeaconManager
import com.kontakt.sdk.android.manager.BeaconManager.{MonitoringListener, RangingListener}
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.activities.MainActivity
import io.github.morgaroth.android.mywork.logic.BeaconInTheAir
import io.github.morgaroth.android.mywork.storage.WorkingWithData
import io.github.morgaroth.android.utilities.{ImplicitContext, logger}

import scala.collection.JavaConversions._
import scala.compat.Platform
import scala.concurrent.duration._

object BeaconMonitorService {

  case class BeaconMonitorBinder(service: BeaconMonitorService) extends Binder

  val SpyMonitoringPeriod: MonitorPeriod = new MonitorPeriod(10.seconds.toMillis, 5.minutes.toMillis)
  val ExploreMonitoringPeriod: MonitorPeriod = new MonitorPeriod(60.seconds.toMillis, 5.seconds.toMillis)

  trait BeaconsListener {
    def onBeacons(bcns: List[BeaconInTheAir])

    def monitoringStarted: Unit

    def monitoringStopped: Unit
  }

}


class BeaconMonitorService extends Service with logger with ImplicitContext with WorkingWithData {
  log.info("beacon monitor service instantiated")

  import BeaconMonitorService._

  override implicit def implicitlyVisibleThisAsContext: Context = this

  private lazy val beaconManager = BeaconManager.newInstance(this)
  private final val binder: IBinder = BeaconMonitorBinder(this)
  private lazy val timer = new Timer
  private var listeners: List[BeaconsListener] = List.empty
  private var idCounter: Int = 0
  private var pendingIntentCounter: Int = 0
  val worksListener = new BeaconsListener {
    override def monitoringStopped: Unit = {}

    override def monitoringStarted: Unit = {}

    override def onBeacons(bcns: List[BeaconInTheAir]): Unit = {
      val now = Platform.currentTime
      bcns.flatMap(knownBeacons get _.beacon.getUniqueId).map(_._2).groupBy(_.name).mapValues(_.head) mapValues { w =>
        w.InWorks += now
        w.save()
      }
    }
  }

  class LocalBinder extends Binder {
    def getService: BeaconMonitorService = {
      BeaconMonitorService.this
    }
  }

  def isConnectedToBeaconManager: Boolean = {
    beaconManager.isConnected
  }

  def endExploringBeacons(l: BeaconsListener) = {
    removeDataListener(l)
    if (isConnectedToBeaconManager) {
      beaconManager.stopMonitoring()
      beaconManager.setMonitorPeriod(SpyMonitoringPeriod)
      beaconManager.startMonitoring()
    } else {
      beaconManager.setMonitorPeriod(SpyMonitoringPeriod)
    }
  }

  def exploreBeacons(l: BeaconsListener) = {
    addDataListener(l)
    if (isConnectedToBeaconManager) {
      beaconManager.stopMonitoring()
      beaconManager.setMonitorPeriod(ExploreMonitoringPeriod)
      beaconManager.startMonitoring()
    } else {
      beaconManager.setMonitorPeriod(ExploreMonitoringPeriod)
    }
  }

  def addDataListener(l: BeaconsListener) = {
    listeners ++= Seq(l)
  }

  def removeDataListener(l: BeaconsListener) = {
    listeners = listeners.filter(_ != l)
  }

  @Nullable def onBind(intent: Intent): IBinder = {
    log.debug("onBind")
    binder
  }

  override def onCreate() {
    super.onCreate()
    log.debug("onCreate")
    //    beaconManager.setMonitorPeriod(SpyMonitoringPeriod)
    listeners ++= Seq(worksListener)
    beaconManager.setMonitorPeriod(ExploreMonitoringPeriod)
    beaconManager.setScanMode(BeaconManager.SCAN_MODE_LOW_POWER)
    beaconManager.registerRangingListener(new RangingListener {
      override def onBeaconsDiscovered(venue: Region, beacons: util.List[BeaconDevice]): Unit = {
        log.debug("on beacons discovered")
        log.info(venue.toString + beacons.toList.toString)
        listeners.foreach(_.onBeacons(beacons.map(BeaconInTheAir(venue, _)).toList))
      }
    })
    beaconManager.registerMonitoringListener(new MonitoringListener() {
      def onMonitorStart() {
        log.debug("onMonitorStart")
        // DO STH WHEN MONITOR STARTED
        listeners.foreach(_.monitoringStarted)
      }

      def onMonitorStop() {
        log.debug("onMonitorStop")
        listeners.foreach(_.monitoringStopped)
      }

      def onBeaconAppeared(region: Region, beacon: BeaconDevice) {
        log.debug("onBeaconAppeared")
        listeners.foreach(_.onBeacons(List(BeaconInTheAir(region, beacon))))
      }

      def onBeaconsUpdated(venue: Region, beacons: util.List[BeaconDevice]) {
        log.debug("onBeaconsUpdated")
        import scala.collection.JavaConversions._
        val visibleBeacons: List[BeaconInTheAir] = beacons.map(BeaconInTheAir(venue, _)).toList
        listeners.foreach(_.onBeacons(visibleBeacons))
      }

      def onRegionEntered(venue: Region) {
        log.debug("onRegionEntered " + venue.getMajor + " " + venue.getMinor)

      }

      def onRegionAbandoned(venue: Region) {
        log.debug("onRegionAbandoned " + venue.getMajor + " " + venue.getMinor)

      }
    })
    registerBTStateMonitor()
  }

  def registerBTStateMonitor(): Unit = {
    val mReceiver = new BroadcastReceiver() {
      override def onReceive(context: Context, intent: Intent) {
        val action = intent.getAction

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
          val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
          state match {
            case BluetoothAdapter.STATE_OFF =>
              log.info("Bluetooth off")
            //              stopMonitoring()
            //              showEnableBlueToothNotification()
            case BluetoothAdapter.STATE_TURNING_OFF =>
              log.info("Turning Bluetooth off...")
              stopMonitoring()
              showEnableBlueToothNotification()
            case BluetoothAdapter.STATE_ON =>
              log.info("Bluetooth on")
              beaconManager.startMonitoring()
            case BluetoothAdapter.STATE_TURNING_ON =>
              log.info("Turning Bluetooth on...")
            //              beaconManager.startMonitoring()
          }
        }
      }
    }
    val filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
    registerReceiver(mReceiver, filter)
  }

  override def onDestroy() {
    super.onDestroy()
    log.debug("onDestroy")
    if (beaconManager.isConnected) {
      stopMonitoring()
    }
    beaconManager.disconnect()
    timer.cancel()
  }

  def stopMonitoring(): Unit = {
    log.debug("monitoring stopped")
    beaconManager.stopMonitoring()
  }

  override def onTaskRemoved(rootIntent: Intent) {
    log.debug("onTaskRemoved")
    super.onTaskRemoved(rootIntent)
  }

  @throws(classOf[RemoteException])
  private def loadInitialData() {
    updateBeaconsAndWorksData()
    if (beaconManager.isBluetoothEnabled) {
      beaconManager.startMonitoring()
    } else {
      showEnableBlueToothNotification()
    }
  }

  private def scheduleDataRefresh() {
    timer.schedule(new TimerTask() {
      def run() {
        updateBeaconsAndWorksData()
      }
    }, 5000, 5000)
  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {
    log.debug("onStartCommand")
    try {
      if (!beaconManager.isConnected) {
        beaconManager.connect(new OnServiceBoundListener() {
          @throws(classOf[RemoteException])
          def onServiceBound() {
            log.debug("onServiceBound")
            loadInitialData()
            scheduleDataRefresh()
          }
        })
      }
    }
    catch {
      case e: RemoteException =>
        e.printStackTrace()
    }
    Service.START_STICKY
  }


  def showEnableBlueToothNotification(): Unit = {
    val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    val notificationBuilder: Notification.Builder = new Notification.Builder(BeaconMonitorService.this)
    notificationBuilder.setContentTitle("MyWork app cant work!")
    notificationBuilder.setContentText("Please enable BlueTooth")
    notificationBuilder.setSmallIcon(android.R.drawable.ic_dialog_info)
    notificationBuilder.setAutoCancel(true)
    val intent = new Intent(BeaconMonitorService.this, classOf[MainActivity])
    intent.putExtra(MainActivity.COMMAND, MainActivity.REQUEST_CODE_ENABLE_BLUETOOTH)
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
    pendingIntentCounter += 1
    val pendingIntentShort: PendingIntent = PendingIntent.getActivity(BeaconMonitorService.this, pendingIntentCounter, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    idCounter += 1
    notificationBuilder.addAction(R.drawable.abc_btn_check_material, "ENABLE", pendingIntentShort)
    val notification: Notification = notificationBuilder.build()
    notificationManager.notify(idCounter, notification)
  }
}
