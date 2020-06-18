package com.lhd.views.demorangeindicatorseekbar

import android.graphics.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.lhd.views.rangeindicatorseekbar.RangeIndicatorSeekBar
import com.lhd.views.rangeindicatorseekbar.eLog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), RangeIndicatorSeekBar.IRangerListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()
    }

    private fun initView() {
        imgMain.post {
            rSeekBar.rangerListener = this
        }
    }

    override fun onStartTouch(thumbIndex: Int) {
        when (thumbIndex) {
            RangeIndicatorSeekBar.THUMB_INDEX_NONE -> {

            }
            RangeIndicatorSeekBar.THUMB_INDEX_LEFT -> {

            }
            RangeIndicatorSeekBar.THUMB_INDEX_RIGHT -> {

            }
        }
    }

    private fun rotation(percent: Float) {
        imgMain.rotation = percent*360
    }

    override fun onStopTouch(lastThumbIndex: Int) {

    }

    override fun onRangeChanging(minProgress: Float, maxProgress: Float, thumbIndex: Int) {
        val percent = (minProgress - (rSeekBar.max - maxProgress)) / (rSeekBar.max - rSeekBar.min)
        eLog("Percent: $percent")
        rotation(percent)
    }

    override fun onRangeChanged(minProgress: Float, maxProgress: Float, thumbIndex: Int) {

    }

    fun imageClick(view: View) {

    }
}
