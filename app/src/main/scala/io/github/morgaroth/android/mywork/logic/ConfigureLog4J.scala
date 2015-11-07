package io.github.morgaroth.android.mywork.logic

import java.io.File
import android.util.Log
import org.apache.log4j.Level
import android.os.Environment
import de.mindpipe.android.logging.log4j.LogConfigurator

object ConfigureLog4J {
  def configure() = {
    try {
      val logConfigurator = new LogConfigurator()

      logConfigurator.setFileName(Environment.getExternalStorageDirectory + File.separator + "MyWork.log")
      logConfigurator.setRootLevel(Level.DEBUG)
      // Set log level of a specific logger
      logConfigurator.setLevel("org.apache", Level.ERROR)
      logConfigurator.configure()
      logConfigurator.isResetConfiguration
    } catch {
      case t: Throwable =>
        Log.e("[ConfigureLog4j]", "error during setup file configurator", t)
    }
  }
}