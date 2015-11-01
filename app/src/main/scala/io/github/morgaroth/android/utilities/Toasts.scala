package io.github.morgaroth.android.utilities

import android.content.Context
import android.widget.{Toast => AToast}

/**
 * Created by mateusz on 31.10.15.
 */
object Toasts {
  def long(msg: String)(implicit ctx: Context) = AToast.makeText(ctx, msg, AToast.LENGTH_LONG).show()

  def long(msgId: Int)(implicit ctx: Context) = AToast.makeText(ctx, msgId, AToast.LENGTH_LONG).show()

  def short(msgId: Int)(implicit ctx: Context) = AToast.makeText(ctx, msgId, AToast.LENGTH_SHORT).show()

  def short(msg: String)(implicit ctx: Context) = AToast.makeText(ctx, msg, AToast.LENGTH_SHORT).show()
}
