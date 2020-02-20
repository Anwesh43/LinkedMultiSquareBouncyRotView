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
val scGap : Float = 0.02f / parts
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
    translate(gap * i + gap / 2, 0f)
    for (j in 0..1) {
        save()
        scale(1f - 2 * j, 1f - 2 * j)
        save()
        translate(gap / 2, gap / 2)
        for (k in 0..1) {
            save()
            rotate(90f * sf * k)
            drawLine(0f, 0f, -gap, 0f, paint)
            restore()
        }
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

fun Canvas.drawMBSNode(i : Int, scale : Float, paint : Paint) {
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

class MultiSquareBouncyView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class MSBNode(var i : Int, val state : State = State()) {

        private var next : MSBNode? = null
        private var prev : MSBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = MSBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawMBSNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : MSBNode {
            var curr : MSBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class MultiBouncySquare(var i : Int) {

        private val root : MSBNode = MSBNode(0)
        private var curr : MSBNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : MultiSquareBouncyView) {

        private val animator : Animator = Animator(view)
        private val msb : MultiBouncySquare = MultiBouncySquare(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            msb.draw(canvas, paint)
            animator.animate {
                msb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            msb.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : MultiSquareBouncyView {
            val view : MultiSquareBouncyView = MultiSquareBouncyView(activity)
            activity.setContentView(view)
            return view
        }
    }
}
