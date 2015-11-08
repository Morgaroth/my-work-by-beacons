package io.github.morgaroth.android.mywork

import android.app.Application
import com.parse.Parse
import io.github.morgaroth.android.mywork.logic.ConfigureLog4J

class MyWorkApp extends Application {
  override def onCreate(): Unit = {
    super.onCreate()
    ConfigureLog4J.configure()
    Parse.enableLocalDatastore(this)
    Parse.initialize(this, "qxqIwjmrYHE6r3S8YOkBMDDObsoa2fXXbaEs0Vcu", "MFjbUSMnww4D2zqKMul9mfZuZbZr70lQQyeSRPgE")
  }
}
