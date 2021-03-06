package ui.anwesome.com.multisquareview

/**
 * Created by anweshmishra on 15/04/18.
 */

import android.app.Activity
import android.view.*
import android.content.*
import android.graphics.*
import java.util.concurrent.ConcurrentLinkedQueue

class MultiSquareView (ctx : Context, var n : Int = 6) : View(ctx) {

    val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when(event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
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
            paint.color = Color.parseColor("#673AB7")
            paint.strokeWidth = Math.min(w,h)/60
            paint.strokeCap = Paint.Cap.ROUND
            canvas.save()
            canvas.translate(w/2, h/2)
            for (i in 0..3) {
                canvas.save()
                canvas.rotate(90f * i)
                canvas.drawLine(size, (-size), size, -(size) + 2 * size * state.scale, paint)
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

            if (n > 0) {
                val w : Float = canvas.width.toFloat()
                val h : Float = canvas.height.toFloat()
                paint.color = Color.parseColor("#4CAF50")
                paint.strokeWidth = Math.min(w,h)/60
                paint.strokeCap = Paint.Cap.ROUND
                state.execute { j ->
                    val gap: Float = (0.9f * w) / n
                    for (i in 0..1) {
                        canvas.save()
                        canvas.translate(w/20, h/20 +0.9f * h * i)
                        canvas.drawLine(0f, 0f, gap * j + gap * (squares.at(j)?.state?.scale ?: 0f), 0f, paint)
                        canvas.restore()
                    }
                }
            }
        }

        fun update (stopcb : (Float) -> Unit) {
            state.execute { j ->
                squares.at(j)?.update {
                    state.incrementCounter()
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

    data class Renderer(var view : MultiSquareView) {

        val animator : Animator = Animator(view)

        val multiSquare : SquarePartContainer = SquarePartContainer(view.n)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(Color.parseColor("#212121"))
            multiSquare.draw(canvas, paint)
            animator.animate {
                multiSquare.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            multiSquare.startUpdating {
                animator.start()
            }
        }
    }
    companion object {
        fun create (activity : Activity) : MultiSquareView {
            val view : MultiSquareView = MultiSquareView(activity)
            activity.setContentView(view)
            return view
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