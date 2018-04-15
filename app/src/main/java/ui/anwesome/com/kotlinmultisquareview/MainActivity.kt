package ui.anwesome.com.kotlinmultisquareview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import ui.anwesome.com.multisquareview.MultiSquareView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MultiSquareView.create(this)
    }
}
