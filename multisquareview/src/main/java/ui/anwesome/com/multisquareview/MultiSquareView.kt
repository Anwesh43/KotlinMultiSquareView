package ui.anwesome.com.multisquareview

/**
 * Created by anweshmishra on 15/04/18.
 */

import android.view.*
import android.content.*
import android.graphics.*
import java.util.concurrent.ConcurrentLinkedQueue

class MultiSquareView (ctx : Context) : View(ctx) {

    val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(stopcb : (Float) -> Unit) {
            scale += 0.1f * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                stopcb(scale)
            }
        }

        fun startUpdating(startcb : () -> Unit) {
            if (dir == 0f) {
                dir = 1  - 2 * prevScale
                startcb()
            }
        }
    }

    data class ContainerState (var n : Int, var j : Int = 0, var dir : Int = 1) {

        fun incrementCounter() {
            j += dir
            if (j == n || j == -1) {
                dir *=-1
                j += dir
            }
        }

        fun execute(cb : (Int) -> Unit) {
            cb(j)
        }
    }

    data class SquarePart(var i : Int, val state : State = State()) {

        fun draw(canvas : Canvas, paint : Paint, n : Int) {
            val w : Float = canvas.width.toFloat()
            val h : Float = canvas.height.toFloat()
            val size : Float = (i+1) * (Math.min(w,h)/(2 * (n+1)))
            canvas.save()
            canvas.translate(w/2, h/2)
            for (i in 0..3) {
                canvas.save()
                canvas.rotate(90f * i)
                canvas.drawLine(size, (-size), size, -(size) + size * state.scale, paint)
                canvas.restore()
            }
            canvas.restore()
        }

        fun update(stopcb : (Float) -> Unit) {
            state.update(stopcb)
        }

        fun startUpdating(startcb : () -> Unit) {
            state.startUpdating(startcb)
        }

    }

    data class SquarePartContainer(var n : Int, var state : ContainerState = ContainerState(n)) {

        val squares : ConcurrentLinkedQueue<SquarePart> = ConcurrentLinkedQueue()

        init {
            for (i in 0..n-1) {
                squares.add(SquarePart(i))
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            squares.forEach {
                it.draw(canvas, paint, n)
            }
        }

        fun update (stopcb : (Float) -> Unit) {
            state.execute { j ->
                squares.at(j)?.update {
                    stopcb(it)
                }
            }
        }
        fun startUpdating(startcb : () -> Unit) {
            state.execute { j ->
                squares.at(j)?.startUpdating {
                    startcb()
                }
            }
        }
    }

    data class Animator (var view : MultiSquareView, var animated : Boolean = false) {
        fun animate(updatecb : () -> Unit) {
            try {
                updatecb()
                Thread.sleep(50)
                view.invalidate()
            }
            catch(ex : Exception) {

            }
        }

        fun start () {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop () {
            if (animated) {
                animated = false
            }
        }
    }
}

fun ConcurrentLinkedQueue<MultiSquareView.SquarePart>.at(j : Int) : MultiSquareView.SquarePart? {
    var i : Int = 0
    forEach {
        if (i == j) {
            return it
        }
        i ++
    }
    return null
}