package com.mapbox.android.gestures

import android.view.MotionEvent

object TestUtils {

  fun getMotionEvent(
    actionMasked: Int,
    pointerXs: FloatArray,
    pointerYs: FloatArray,
    actionIndex: Int,
    previousEvent: MotionEvent? = null
  ): MotionEvent {
    require(pointerXs.size == pointerYs.size) { "Pointer coordinates must have matching sizes" }
    require(pointerXs.isNotEmpty()) { "At least one pointer is required" }
    require(actionIndex in pointerXs.indices) { "Action index is out of pointer bounds" }

    val currentTime = System.currentTimeMillis()
    val downTime = previousEvent?.downTime ?: currentTime
    val pointerCount = pointerXs.size

    val action = when (actionMasked) {
      MotionEvent.ACTION_POINTER_DOWN,
      MotionEvent.ACTION_POINTER_UP -> actionMasked or (actionIndex shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
      else -> actionMasked
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
      pc.x = pointerXs[i]
      pc.y = pointerYs[i]
      pc.pressure = 1f
      pc.size = 1f
      pointerCoords[i] = pc
    }

    return MotionEvent.obtain(
      downTime,
      currentTime,
      action,
      pointerCount,
      properties,
      pointerCoords,
      0,
      0,
      1f,
      1f,
      0,
      0,
      0,
      0
    )
  }

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
