package io.github.morgaroth.android.mywork.services

import java.util.{Timer, TimerTask}

import android.app.{Notification, NotificationManager, PendingIntent, Service}
import android.content.{Context, Intent}
import android.os.{Binder, IBinder, RemoteException}
import android.support.annotation.Nullable
import com.kontakt.sdk.android.configuration.MonitorPeriod
import com.kontakt.sdk.android.connection.OnServiceBoundListener
import com.kontakt.sdk.android.device.{BeaconDevice, Region}
import com.kontakt.sdk.android.factory.Filters
import com.kontakt.sdk.android.manager.BeaconManager
import com.kontakt.sdk.android.manager.BeaconManager.MonitoringListener
import io.github.morgaroth.android.mywork.R
import io.github.morgaroth.android.mywork.activities.MainActivity
import io.github.morgaroth.android.mywork.logic.BeaconInTheAir
import io.github.morgaroth.android.mywork.services.BeaconMonitorService.{BeaconMonitorBinder, BeaconsListener}
import io.github.morgaroth.android.mywork.storage.{ParseManager, Work}
import io.github.morgaroth.android.utilities.{ImplicitContext, logger}

import scala.concurrent.duration._
import scala.util.Try

object BeaconMonitorService {

  case class BeaconMonitorBinder(service: BeaconMonitorService) extends Binder

  trait BeaconsListener {
    def onBeacons(bcns: List[BeaconInTheAir])

    def startMonitoring: Unit

    def stopMonitoring: Unit
  }

}


class BeaconMonitorService extends Service with logger with ImplicitContext {
  log.info("beacon monitor service instantiated")

  override implicit def implicitlyVisibleThisAsContext: Context = this

  private lazy val beaconManager = BeaconManager.newInstance(this)
  private final val binder: IBinder = BeaconMonitorBinder(this)
  private lazy val timer = new Timer

  private var works = List.empty[Work]

  //  private var enteredBeacons: List[Beacon] = List.empty[Beacon]
  private var idCounter: Int = 0

  private var pendingIntentCounter: Int = 0

  //  private var notificationsMap: Map[Beacon, Integer] = Map.empty[Beacon, Integer]

  //  def getBeacons: List[Beacon] = {
  //    beacons
  //  }
  //
  //  def getEnteredBeacons: List[Beacon] = {
  //    enteredBeacons
  //  }

  class LocalBinder extends Binder {
    def getService: BeaconMonitorService = {
      BeaconMonitorService.this
    }
  }

  //  private var rangingListener: BeaconManager.RangingListener = null
  //  private var monitoringListener: Option[MonitoringListener] = _
  private var listeners: List[BeaconsListener] = List.empty

  //  def setRangingListener(rangingListener: BeaconManager.RangingListener) {
  //    this.rangingListener = rangingListener
  //  }
  //
  //  def setMonitoringListener(monitoringListener: MonitoringListener) {
  //    this.monitoringListener = Some(monitoringListener)
  //  }

  def isConnectedToBeaconManager: Boolean = {
    beaconManager.isConnected
  }


  def addDataListener(l: BeaconsListener) = {
    listeners ++= Seq(l)
  }

  @Nullable def onBind(intent: Intent): IBinder = {
    binder
  }

  override def onCreate() {
    super.onCreate()
    log.debug("onCreate")

    //    // ??
    //    List(4506, 38100, 44830, 17527).foreach { filter =>
    //      beaconManager.addFilter(Filters.newMajorFilter(filter))
    //    }

    beaconManager.setMonitorPeriod(new MonitorPeriod(10.seconds.toMillis, 1.minutes.toMillis))
    beaconManager.setScanMode(BeaconManager.SCAN_MODE_LOW_POWER)
    beaconManager.registerMonitoringListener(new MonitoringListener() {
      def onMonitorStart() {
        log.debug("onMonitorStart")
        // DO STH WHEN MONITOR STARTED
        listeners.foreach(_.startMonitoring)
      }

      def onMonitorStop() {
        log.debug("onMonitorStop")
        listeners.foreach(_.stopMonitoring)
      }

      def onBeaconAppeared(region: Region, beacon: BeaconDevice) {
        log.debug("onBeaconAppeared")
        //        monitoringListener.foreach(_.onBeaconAppeared(region, beacon))
        listeners.foreach(_.onBeacons(List(BeaconInTheAir(region, beacon))))
      }

      def onBeaconsUpdated(venue: Region, beacons: java.util.List[BeaconDevice]) {
        log.debug("onBeaconsUpdated")
        import scala.collection.JavaConversions._
        for (beaconDevice <- beacons) {
          log.debug("\t" + beaconDevice.getUniqueId + " " + beaconDevice.getMajor + " " + beaconDevice.getMinor)
        }
        //        monitoringListener.foreach(_.onBeaconsUpdated(venue, beacons))
        listeners.foreach(_.onBeacons(beacons.map(BeaconInTheAir(venue, _)).toList))
      }

      def onRegionEntered(venue: Region) {
        log.debug("onRegionEntered " + venue.getMajor + " " + venue.getMinor)
        //          var beacon: Beacon = null
        //          for (b <- beacons) {
        //            if (venue.getMajor == b.major && venue.getMinor == b.minor) {
        //              beacon = b
        //            }
        //          }
        //          if (beacon != null) {
        //            if (enteredBeacons.contains(beacon)) {
        //              return
        //            }
        //            enteredBeacons.add(beacon)
        //            dataListener.onBeaconReached(beacon)
        //                    val intentMedium: Intent = new Intent(BeaconMonitorService.this, classOf[HubActivity])
        //            intentShort.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
        //            intentShort.putExtra(HubActivity.EXTRA_DURATION_KEY, 60)
        //            intentShort.putExtra(HubActivity.EXTRA_BEACON_ID, beacon.getObjectId)
        //            val pendingIntentMedium: PendingIntent = PendingIntent.getActivity(BeaconMonitorService.this, ({
        //              pendingIntentCounter += 1; pendingIntentCounter - 1
        //            }), intentMedium, PendingIntent.FLAG_UPDATE_CURRENT)
        //            val intentLong: Intent = new Intent(BeaconMonitorService.this, classOf[HubActivity])
        //            intentShort.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
        //            intentShort.putExtra(HubActivity.EXTRA_DURATION_KEY, 120)
        //            intentShort.putExtra(HubActivity.EXTRA_BEACON_ID, beacon.getObjectId)
        //            val pendingIntentLong: PendingIntent = PendingIntent.getActivity(BeaconMonitorService.this, ({
        //              pendingIntentCounter += 1; pendingIntentCounter - 1
        //            }), intentLong, PendingIntent.FLAG_UPDATE_CURRENT)
        //            notificationBuilder.setContentIntent(pendingIntentShort)
        //            notificationBuilder.addAction(R.drawable.clock, "30m", pendingIntentShort)
        //            notificationBuilder.addAction(R.drawable.clock, "1h", pendingIntentMedium)
        //            notificationBuilder.addAction(R.drawable.clock, "2h", pendingIntentLong)
        //            val notification: Notification = notificationBuilder.build
        //            notificationsMap.put(beacon, idCounter)
        //            notificationManager.notify(({
        //              idCounter += 1; idCounter - 1
        //            }), notification)
        //            val raum: Work = beacon.getRaum
        //            val parseManager: ParseManager = new ParseManager
        //            parseManager.addOccupationToRaum(raum, null, true)
        //          }
        //        monitoringListener.foreach(_.onRegionEntered(venue))
      }

      def onRegionAbandoned(venue: Region) {
        log.debug("onRegionAbandoned " + venue.getMajor + " " + venue.getMinor)
        //        var beacon: Beacon = null
        //        import scala.collection.JavaConversions._
        //        for (b <- beacons) {
        //          if (venue.getMajor == b.getMajor && venue.getMinor == b.getMinor) {
        //            beacon = b
        //          }
        //        }
        //        if (beacon != null) {
        //          enteredBeacons.remove(beacon)
        //          val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
        //          notificationManager.cancel(notificationsMap.get(beacon))
        //          val raum: Work = beacon.getRaum
        //          val parseManager: ParseManager = new ParseManager
        //          parseManager.clearOccupationsFromRaum(raum, true)
        //        }
        //        monitoringListener.foreach(_.onRegionAbandoned(venue))
      }
    })
  }

  override def onDestroy() {
    super.onDestroy()
    log.debug("onDestroy")
    if(beaconManager.isConnected) {
      beaconManager.stopMonitoring()
    }
    beaconManager.disconnect()
    timer.cancel()
  }

  override def onTaskRemoved(rootIntent: Intent) {
    log.debug("onTaskRemoved")
    super.onTaskRemoved(rootIntent)
  }

  @throws(classOf[RemoteException])
  private def loadInitialData() {
    works = loadWorks
  }

  def loadWorks: List[Work] = {
    val triedWorks: Try[List[Work]] = ParseManager.works
    triedWorks.failed.map { t =>
      log.error("loading works failed", t)
    }
    triedWorks.getOrElse(List.empty)
  }

  private def scheduleDataRefresh() {
    timer.schedule(new TimerTask() {
      def run() {
        works = loadWorks
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
    //    notificationBuilder.setSmallIcon(R.drawable.app_icon)
    notificationBuilder.setAutoCancel(true)
    val intentShort: Intent = new Intent(BeaconMonitorService.this, classOf[MainActivity])
    intentShort.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
    pendingIntentCounter += 1
    val pendingIntentShort: PendingIntent = PendingIntent.getActivity(BeaconMonitorService.this, pendingIntentCounter, intentShort, PendingIntent.FLAG_UPDATE_CURRENT)
    idCounter += 1
    notificationBuilder.addAction(R.drawable.abc_btn_check_material, "ENABLE", pendingIntentShort)
    notificationManager.notify(idCounter, notificationBuilder.build())
  }
}
