package com.mapbox.android.gestures

import android.view.MotionEvent

object TestUtils {

  fun getMotionEvent(action: Int, x: Float, y: Float, previousEvent: MotionEvent? = null): MotionEvent {
    val currentTime = System.currentTimeMillis()
    val downTime = previousEvent?.downTime ?: System.currentTimeMillis()

    var pointerCount = previousEvent?.pointerCount ?: 0
    if (previousEvent != null && previousEvent.actionMasked == MotionEvent.ACTION_POINTER_UP) {
      pointerCount--
    } else if (action == MotionEvent.ACTION_POINTER_DOWN) {
      pointerCount++
    } else if (action == MotionEvent.ACTION_DOWN) {
      pointerCount = 1
    }

    val properties = arrayOfNulls<MotionEvent.PointerProperties>(pointerCount)
    for (i in 0 until pointerCount) {
      val pp = MotionEvent.PointerProperties()
      pp.id = i
      pp.toolType = MotionEvent.TOOL_TYPE_FINGER
      properties[i] = pp
    }

    val pointerCoords = arrayOfNulls<MotionEvent.PointerCoords>(pointerCount)
    for (i in 0 until pointerCount) {
      val pc = MotionEvent.PointerCoords()
      pc.x = x + i * 50
      pc.y = y + i * 50
      pc.pressure = 1f
      pc.size = 1f
      pointerCoords[i] = pc
    }

    return MotionEvent.obtain(downTime, currentTime,
      action, pointerCount, properties,
      pointerCoords, 0, 0, 1f, 1f, 0, 0, 0, 0)
  }
}
