package com.anwesh.uiprojects.multisquarebouncyview

/**
 * Created by anweshmishra on 20/02/20.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.app.Activity
import android.content.Context

val nodes : Int = 5
val parts : Int = 5
val scGap : Float = 0.02f
val strokeFactor : Int = 90
val foreColor : Int = Color.parseColor("#3F51B5")
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) *  n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawRotSquare(i : Int, w : Float, scale : Float, paint : Paint) {
    val gap : Float = w / parts
    val sf : Float = scale.sinify().divideScale(i, parts)
    save()
    translate(gap * i, 0f)
    for (j in 0..1) {
        save()
        scale(1f - 2 * j, 1f - 2 * j)
        save()
        translate(gap / 2, gap / 2)
        rotate(90f * sf)
        drawLine(0f, 0f, -gap, 0f, paint)
        restore()
        restore()
    }
    restore()
}

fun Canvas.drawMultiRotSquare(w : Float, scale : Float, paint : Paint) {
    for (j in 0..(parts - 1)) {
        drawRotSquare(j, w, scale, paint)
    }
}

fun Canvas.drawMRSNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(0f, gap * (i + 1))
    drawMultiRotSquare(w, scale, paint)
    restore()
}
