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
import io.github.morgaroth.android.mywork.activities.MainActivity
import io.github.morgaroth.android.mywork.logic.BeaconInTheAir
import io.github.morgaroth.android.mywork.storage.{Work, Beacon, WorkingWithData}
import io.github.morgaroth.android.utilities.{ImplicitContext, logger}

import scala.collection.JavaConversions._
import scala.compat.Platform
import scala.concurrent.duration._

object BeaconMonitorService {

  case class BeaconMonitorBinder(service: BeaconMonitorService) extends Binder

  val SpyMonitoringPeriod: MonitorPeriod = new MonitorPeriod(10.seconds.toMillis, 3.minutes.toMillis)
  val ExploreMonitoringPeriod: MonitorPeriod = new MonitorPeriod(60.seconds.toMillis, 5.seconds.toMillis)

  val COMMAND = "command"
  val UserCancelledEnableBT = 1
  val UserRejectedNotification = 2

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

  private var beaconManager: BeaconManager = _
  private final val binder: IBinder = BeaconMonitorBinder(this)
  private lazy val timer = new Timer
  private var listeners: List[BeaconsListener] = List.empty
  private var idCounter: Int = 0
  private var pendingIntentCounter: Int = 0
  private var monitoringPeriod: MonitorPeriod = SpyMonitoringPeriod

  val worksListener = new BeaconsListener {
    override def monitoringStopped: Unit = {}

    override def monitoringStarted: Unit = {}

    override def onBeacons(bcns: List[BeaconInTheAir]): Unit = {
      val now = Platform.currentTime
      val notSave = now - 60 * 1000
      log.debug(s"checking working hours with beacons ${bcns.map(_.beacon.getUniqueId).toSet} and works ${knownBeacons.keySet}")
      val visibleBeaconsOfWorks: List[(Beacon, Work)] = bcns.flatMap(knownBeacons get _.beacon.getUniqueId)
      val inWorks: Iterable[Work] = visibleBeaconsOfWorks.map(_._2).groupBy(_.name).mapValues(_.head).values
      val activeWorks = inWorks.filter(_.InWorks().lastOption.getOrElse(0l) < notSave)
      log.debug(s"visible in works are: $inWorks, but only $activeWorks will be updated")
      activeWorks map { w =>
        log.debug(s"adding work at $now for ${w.name}")
        try {
          w.InWorks += now
          w.save()
        } catch {
          case t: Throwable => log.error(s"saving $w", t)
        }
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
    log.info("onExploringStop")
    removeDataListener(l)
    monitoringPeriod = SpyMonitoringPeriod
    reloadService()
  }

  def exploreBeacons(l: BeaconsListener) = {
    log.info("onExploringStart")
    addDataListener(l)
    monitoringPeriod = ExploreMonitoringPeriod
    reloadService()
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
    registerBTStateMonitor()
    updateBeaconsAndWorksData()
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
            case BluetoothAdapter.STATE_TURNING_OFF =>
              log.info("Turning Bluetooth off...")
              stopMonitoring()
              log.info("showing notification from bt state listener")
              showEnableBlueToothNotification()
            case BluetoothAdapter.STATE_ON =>
              log.info("Bluetooth on")
              beaconManager.startMonitoring()
            case BluetoothAdapter.STATE_TURNING_ON =>
              log.info("Turning Bluetooth on...")
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
    disableService()
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
    if (beaconManager.isBluetoothEnabled && beaconManager.isConnected) {
      beaconManager.startMonitoring()
    } else {
      log.info("showing notificatation from load initial data")
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

  private def scheduleEnableBT() {
    timer.schedule(new TimerTask() {
      def run() {
        if (!beaconManager.isBluetoothEnabled) {
          log.info("showing notification from periodical task")
          showEnableBlueToothNotification()
        }
      }
    }, 10.seconds.toMillis, 1.hour.toMillis)
  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {
    log.debug("onStartCommand")
    Option(intent).map(_.getIntExtra(BeaconMonitorService.COMMAND, -1)).filter(_ != -1) match {
      case Some(UserRejectedNotification) =>
        log.info("user rejected notification")
      case _ =>
        try {
          scheduleDataRefresh()
          enableService()
          scheduleEnableBT()
        }
        catch {
          case e: RemoteException =>
            e.printStackTrace()
        }
    }
    Service.START_STICKY
  }

  def reloadService(): Unit = {
    disableService()
    enableService()
  }

  def disableService(): Unit = {
    beaconManager.disconnect()
    beaconManager = null
  }

  def enableService(): Unit = {
    beaconManager = BeaconManager.newInstance(this)
    beaconManager.setMonitorPeriod(monitoringPeriod)
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
    if (!beaconManager.isConnected) {
      beaconManager.connect(new OnServiceBoundListener() {
        @throws(classOf[RemoteException])
        def onServiceBound() {
          log.debug("onServiceBound")
          loadInitialData()
        }
      })
    }
  }

  var lastNotification: Option[Notification] = _

  def showEnableBlueToothNotification(): Unit = {
    val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    val notificationBuilder: Notification.Builder = new Notification.Builder(BeaconMonitorService.this)
    notificationBuilder.setContentTitle("MyWork app cant work!")
    notificationBuilder.setContentText("Click to enable Bluetooth")
    notificationBuilder.setSmallIcon(android.R.drawable.ic_dialog_info)
    notificationBuilder.setAutoCancel(true)
    val intent = new Intent(BeaconMonitorService.this, classOf[MainActivity])
    intent.putExtra(MainActivity.COMMAND, MainActivity.REQUEST_CODE_ENABLE_BLUETOOTH)
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
    pendingIntentCounter += 1
    val pendingIntent = PendingIntent.getActivity(BeaconMonitorService.this, pendingIntentCounter, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    notificationBuilder.setContentIntent(pendingIntent)
    val notification: Notification = notificationBuilder.build()
    notificationManager.cancel(idCounter)
    idCounter += 1
    notificationManager.notify(idCounter, notification)
  }
}
