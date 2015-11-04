package io.github.morgaroth.android.utilities.fragments

import android.content.Context

/**
 * Created by mateusz on 04.11.15.
 */
trait AttachedActivity[T] {
  this: SmartFragment =>

  var attached: Option[T] = _

  def attachActivity(ctx: Context): T

  override def onAttachFunction(ctx: Context): Unit = {
    try {
      attached = Some(attachActivity(ctx))
      log.info(s"to $this attached $attached")
    } catch {
      case t: Throwable =>
        log.error(s"attaching ${t.getMessage}")
    }
  }
}
