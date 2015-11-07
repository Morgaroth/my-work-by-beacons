package io.github.morgaroth.android.mywork

import android.app.Application
import io.github.morgaroth.android.mywork.logic.ConfigureLog4J

class MyWorkApp extends Application {
  override def onCreate(): Unit = {
    super.onCreate()
    ConfigureLog4J.configure()
  }
}
