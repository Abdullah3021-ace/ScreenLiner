package com.example.screenprank

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.app.NotificationCompat

class OverlayService : Service() {
    private var wm: WindowManager? = null
    private var overlayView: ViewGroup? = null
    private lateinit var sPreferance: SPreferance
    private var initialX = 0f
    private var isViewAdded = false

    val handler = Handler(Looper.getMainLooper())
    lateinit var runnable: Runnable

    @SuppressLint("ClickableViewAccessibility")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onCreate()

        sPreferance = SPreferance(this)

        if (Build.VERSION.SDK_INT >= 26) {
            val CHANNEL_ID = "channel1"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Overlay notification",
                NotificationManager.IMPORTANCE_LOW
            )

            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Screen Line Prank")
                .setContentText("Displaying a line on your device screen.")
                .setSmallIcon(R.drawable.screen_line_prank_icon)
                .build()

            startForeground(1, notification)
        }

        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_layout, null) as ViewGroup
        var shouldMove = false
        val line = overlayView!!.findViewById<View>(R.id.line)
        val handle = overlayView!!.findViewById<View>(R.id.move_line)

        runnable = object : Runnable {
            override fun run() {

                if (sPreferance.getColorCode() != 0) {
                    line!!.setBackgroundColor(sPreferance.getColorCode())
                }

                handler.postDelayed(this, 1000)

                if (sPreferance.getSleepStatus()) {
                    overlayView!!.keepScreenOn = true
                } else {
                    overlayView!!.keepScreenOn = false

                }


                shouldMove = sPreferance.getFloatationStatus()
                if (shouldMove) {
                    handle.visibility = View.VISIBLE
                }else{
                    handle.visibility = View.GONE

                }
                println(shouldMove)
            }
        }

        handler.post(runnable)



        overlayView!!.fitsSystemWindows = false

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        ) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
//        params.gravity = Gravity.CENTER
        params.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isViewAdded) {
                wm!!.addView(overlayView, params)
                isViewAdded = true
            }
        }, sPreferance.getDelayTime().toLong())


        handle!!.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0f
            private var initialY = 0f
            private var offsetX = 0f
            private var offsetY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (shouldMove) {
                            offsetX = event.rawX - params.x
                            offsetY = event.rawY - params.y
                        }
                    }

                    MotionEvent.ACTION_MOVE -> {
                        if (shouldMove) {
                            val newX = event.rawX - offsetX
                            params.x = newX.toInt()

                            // Update the position of overlayView
                            wm?.updateViewLayout(overlayView, params)
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        // Optional: Snap the view to the nearest edge or grid
                    }
                }
                return true
            }
        })




        return START_NOT_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()

        if (overlayView != null) {
            try {
                if (overlayView?.isAttachedToWindow == true) {
                    wm?.removeView(overlayView)
                }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } finally {
                overlayView = null
            }
        }

        handler.removeCallbacks(runnable)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    companion object {
        private const val TAG = "OverlayService"
    }
}
