package io.github.morgaroth.android.mywork

import android.app.Application
import com.parse.{ParseObject, Parse}
import io.github.morgaroth.android.mywork.logic.ConfigureLog4J
import io.github.morgaroth.android.mywork.storage.{Beacon, Work}

class MyWorkApp extends Application {
  override def onCreate(): Unit = {
    super.onCreate()
    ConfigureLog4J.configure()
    ParseObject.registerSubclass(classOf[Work])
    ParseObject.registerSubclass(classOf[Beacon])
    Parse.enableLocalDatastore(this)
    Parse.initialize(this, "qxqIwjmrYHE6r3S8YOkBMDDObsoa2fXXbaEs0Vcu", "MFjbUSMnww4D2zqKMul9mfZuZbZr70lQQyeSRPgE")
  }
}
