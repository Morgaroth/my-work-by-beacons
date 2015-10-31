package io.github.morgaroth.android.mywork

import java.util.{Timer, TimerTask}

import android.app.Service
import android.content.Intent
import android.os.{Binder, IBinder, RemoteException}
import android.support.annotation.Nullable
import android.util.Log
import com.kontakt.sdk.android.configuration.MonitorPeriod
import com.kontakt.sdk.android.connection.OnServiceBoundListener
import com.kontakt.sdk.android.device.{BeaconDevice, Region}
import com.kontakt.sdk.android.factory.Filters
import com.kontakt.sdk.android.manager.BeaconManager

object BeaconMonitorService {

  trait DataListener {
    def onBeaconsLoaded(beacons: List[Beacon])

    def onBeaconReached(beacon: Beacon)
  }

}

class BeaconMonitorService extends Service {
  private var beaconManager: BeaconManager = null
  private final val binder: IBinder = new BeaconMonitorService#LocalBinder
  private[service] var timer: Timer = null
  private var beacons: List[Beacon] = null
  private var enteredBeacons: List[Beacon] = List.empty[Beacon]
  private var idCounter: Int = 0
  private var pendingIntentCounter: Int = 0
  private var notificationsMap: Map[Beacon, Integer] = Map.empty[Beacon, Integer]

  def getBeacons: List[Beacon] = {
    beacons
  }

  def getEnteredBeacons: List[Beacon] = {
    enteredBeacons
  }

  class LocalBinder extends Binder {
    def getService: BeaconMonitorService = {
      BeaconMonitorService.this
    }
  }

  private var dataListener: BeaconMonitorService.DataListener = null
  private var rangingListener: BeaconManager.RangingListener = null
  private var monitoringListener: BeaconManager.MonitoringListener = null

  def setRangingListener(rangingListener: BeaconManager.RangingListener) {
    this.rangingListener = rangingListener
  }

  def setMonitoringListener(monitoringListener: BeaconManager.MonitoringListener) {
    this.monitoringListener = monitoringListener
  }

  def setDataListener(dataListener: BeaconMonitorService.DataListener) {
    this.dataListener = dataListener
  }

  def isConnectedToBeaconManager: Boolean = {
    beaconManager.isConnected
  }

  @Nullable def onBind(intent: Intent): IBinder = {
    binder
  }

  override def onCreate() {
    super.onCreate()
    Log.d(this.getClass.getName, "onCreate")
    beaconManager = BeaconManager.newInstance(this)
    val filters: Array[Int] = Array[Int](4506, 38100, 44830, 17527)
    for (filter <- filters) {
      beaconManager.addFilter(Filters.newMajorFilter(filter))
    }
    beaconManager.setMonitorPeriod(new MonitorPeriod(50000, 5000))
    beaconManager.setScanMode(BeaconManager.SCAN_MODE_LOW_LATENCY)
    beaconManager.registerMonitoringListener(new BeaconManager.MonitoringListener() {
      def onMonitorStart() {
        Log.d(this.getClass.getName, "onMonitorStart")
        // DO STH WHEN MONITOR STARTED
        if (monitoringListener != null) {
          monitoringListener.onMonitorStart()
        }
      }

      def onMonitorStop() {
        Log.d(this.getClass.getName, "onMonitorStop")
        if (monitoringListener != null) {
          monitoringListener.onMonitorStop()
        }
      }

      def onBeaconAppeared(region: Region, beacon: BeaconDevice) {
        Log.d(this.getClass.getName, "onBeaconAppeared")
        if (monitoringListener != null) {
          monitoringListener.onBeaconAppeared(region, beacon)
        }
      }

      def onBeaconsUpdated(venue: Region, beacons: java.util.List[BeaconDevice]) {
        Log.d(this.getClass.getName, "onBeaconsUpdated")
        import scala.collection.JavaConversions._
        for (beaconDevice <- beacons) {
          Log.d(this.getClass.getName, "\t" + beaconDevice.getUniqueId + " " + beaconDevice.getMajor + " " + beaconDevice.getMinor)
        }
        if (monitoringListener != null) {
          monitoringListener.onBeaconsUpdated(venue, beacons)
        }
      }

      def onRegionEntered(venue: Region) {
        Log.d(this.getClass.getName, "onRegionEntered " + venue.getMajor + " " + venue.getMinor)
        if (dataListener != null) {
          var beacon: Beacon = null
          for (b <- beacons) {
            if (venue.getMajor == b.major && venue.getMinor == b.minor) {
              beacon = b
            }
          }
//          if (beacon != null) {
//            if (enteredBeacons.contains(beacon)) {
//              return
//            }
//            enteredBeacons.add(beacon)
//            dataListener.onBeaconReached(beacon)
//            val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
//            val notificationBuilder: Notification.Builder = new Notification.Builder(BeaconMonitorService.this)
//            notificationBuilder.setContentTitle("You entered:" + beacon.getRaum.getName)
//            notificationBuilder.setContentText("Choose occupation duration")
//            notificationBuilder.setSmallIcon(R.drawable.app_icon)
//            notificationBuilder.setAutoCancel(true)
//            val intentShort: Intent = new Intent(BeaconMonitorService.this, classOf[HubActivity])
//            intentShort.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
//            intentShort.putExtra(HubActivity.EXTRA_DURATION_KEY, 30)
//            intentShort.putExtra(HubActivity.EXTRA_BEACON_ID, beacon.getObjectId)
//            val pendingIntentShort: PendingIntent = PendingIntent.getActivity(BeaconMonitorService.this, ({
//              pendingIntentCounter += 1; pendingIntentCounter - 1
//            }), intentShort, PendingIntent.FLAG_UPDATE_CURRENT)
//            val intentMedium: Intent = new Intent(BeaconMonitorService.this, classOf[HubActivity])
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
        }
        if (monitoringListener != null) {
          monitoringListener.onRegionEntered(venue)
        }
      }

      def onRegionAbandoned(venue: Region) {
        Log.d(this.getClass.getName, "onRegionAbandoned " + venue.getMajor + " " + venue.getMinor)
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
//        if (monitoringListener != null) {
//          monitoringListener.onRegionAbandoned(venue)
//        }
      }
    })
  }

  override def onDestroy() {
    super.onDestroy()
    Log.d(this.getClass.getName, "onDestroy")
    beaconManager.stopMonitoring()
    beaconManager.disconnect()
    beaconManager = null
    if (timer != null) {
      timer.cancel()
      timer = null
    }
  }

  override def onTaskRemoved(rootIntent: Intent) {
    Log.d(this.getClass.getName, "onTaskRemoved")
    super.onTaskRemoved(rootIntent)
  }

  @throws(classOf[RemoteException])
  private def loadInitialData() {
//    val parseManager: ParseManager = new ParseManager
//    beacons = parseManager.getBeacons(classOf[Beacon])
//    if (beacons != null) {
//      val regionSet: Set[Region] = new HashSet[Region]
//      import scala.collection.JavaConversions._
//      for (beacon <- beacons) {
//        regionSet.add(new Region(UUID.fromString(beacon.getUuid), beacon.getMajor, beacon.getMinor))
//      }
//      beaconManager.startMonitoring(regionSet)
//      if (dataListener != null) {
//        dataListener.onBeaconsLoaded(beacons)
//      }
//    }
  }

  private def scheduleDataRefresh() {
    timer = new Timer
    timer.schedule(new TimerTask() {
      def run() {
//        val parseManager: ParseManager = new ParseManager
//        beacons = parseManager.getBeacons(classOf[Beacon])
      }
    }, 5000, 5000)
  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {
    Log.d(this.getClass.getName, "onStartCommand")
    try {
      if (!beaconManager.isConnected) {
        beaconManager.connect(new OnServiceBoundListener() {
          @throws(classOf[RemoteException])
          def onServiceBound() {
            Log.d(this.getClass.getName, "onServiceBound")
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
}
