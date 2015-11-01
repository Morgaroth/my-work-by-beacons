package io.github.morgaroth.android.utilities

import android.content.Context

/**
 * Created by mateusz on 31.10.15.
 */
trait ImplicitContext {

  implicit def implicitlyVisibleThisAsContext:Context
}
