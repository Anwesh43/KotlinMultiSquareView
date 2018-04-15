package ui.anwesome.com.multisquareview

/**
 * Created by anweshmishra on 15/04/18.
 */

import android.view.*
import android.content.*
import android.graphics.*

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
}