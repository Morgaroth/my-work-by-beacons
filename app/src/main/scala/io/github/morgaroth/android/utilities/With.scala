package io.github.morgaroth.android.utilities

/**
 * Created by mateusz on 01.11.15.
 */
object With {
  def apply[T](v: => T)(fun: T => Unit) = {
    val tmp: T = v
    fun(tmp)
    tmp
  }
}
