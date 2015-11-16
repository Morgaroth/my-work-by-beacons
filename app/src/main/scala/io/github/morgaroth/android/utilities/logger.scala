package io.github.morgaroth.android.utilities

import android.util.Log

/**
 * Created by mateusz on 31.10.15.
 */
class AndroidLogger(name: String) {
  def v(msg: String) = Log.v(name, msg)

  def v(msg: String, t: Throwable) = Log.v(name, msg, t)

  def debug(msg: String) = Log.d(name, msg)

  def debug(msg: String, t: Throwable) = Log.d(name, msg, t)

  def info(msg: String) = Log.i(name, msg)

  def info(msg: String, t: Throwable) = Log.i(name, msg, t)

  def warn(msg: String) = Log.w(name, msg)

  def warn(msg: String, t: Throwable) = Log.w(name, t)

  def warn(t: Throwable) = Log.w(name, t)

  def error(msg: String) = Log.e(name, msg)

  def error(msg: String, t: Throwable) = Log.e(name, msg, t)

  def getStackTraceString(t: Throwable) = Log.getStackTraceString(t)
}

trait logger {
  //  lazy val log = new AndroidLogger(getClass.getSimpleName)
  lazy val log = org.apache.log4j.Logger.getLogger(getClass.getSimpleName)
}
