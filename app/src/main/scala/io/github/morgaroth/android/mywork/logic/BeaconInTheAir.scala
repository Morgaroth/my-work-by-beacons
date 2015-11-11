package io.github.morgaroth.android.mywork.logic

import com.kontakt.sdk.android.device.{BeaconDevice, Region}
import io.github.morgaroth.android.mywork.storage.{Work, Beacon}

/**
 * Created by mateusz on 04.11.15.
 */
case class BeaconInTheAir(region: Region, beacon: BeaconDevice, known: Option[Work] = None)

