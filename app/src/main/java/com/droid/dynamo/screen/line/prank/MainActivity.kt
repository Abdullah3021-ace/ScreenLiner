package com.droid.dynamo.screen.line.prank

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.droid.dynamo.screen.line.prank.SharedVars.TAG
import com.droid.dynamo.screen.line.prank.databinding.ActivityMainBinding
import top.defaults.colorpicker.ColorPickerPopup
import top.defaults.colorpicker.ColorPickerPopup.Builder


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var mDefaultColor = 0

    private lateinit var sPreferance: SPreferance

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        sPreferance = SPreferance(this)
        if (sPreferance.getColorCode() != 0) {
            binding.colorPicker.setBackgroundColor(sPreferance.getColorCode())

        }
//        if (sPreferance.getOrientation() == "horizontal") {
//            binding.orientation.isChecked = true
//        }

//        val handler = Handler()
//        val runnable: Runnable = object : Runnable {
//            override fun run() {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    // For devices running Android 13 (API level 31) and below
//                    if (isPostNotificationPermissionGranted()) {
//                        startOverlayService()
//                    }
//                }
//
//                handler.postDelayed(this, 1000) // 1000 milliseconds = 1 second
//            }
//        }


// Start the runnable immediately
//        handler.post(runnable)

        when (sPreferance.getDelayTime()) {
            0 -> binding.durationText.text = "Off"
            5000 -> binding.durationText.text = "5 Sec"
            10000 -> binding.durationText.text = "10 Sec"
            30000 -> binding.durationText.text = "30 Sec"
            60000 -> binding.durationText.text = "1 Min"
        }

        if (isMyServiceRunning(OverlayService::class.java)) {
            binding.switch1.isChecked = true

        }

        binding.deviceSleep.isChecked = sPreferance.getSleepStatus()

        binding.floatingLine.isChecked = sPreferance.getFloatationStatus()

        if (!checkDrawOverlayPermission()) {
            showCustomDialog()
        }





        binding.switch1.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ) {
                    // For devices running Android 13 (API level 31) and below
                    if (!isPostNotificationPermissionGranted()) {
                        requestPostNotificationPermission()
                        binding.switch1.isChecked = false
                    } else {
                            startOverlayService()


                    }
                } else {
                    // For devices running Android 13 (API level 31) and above
                    startOverlayService()
                }
            } else {
                stopOverlayService()
            }
        }

        binding.deviceSleep.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                sPreferance.saveAutoSleepStatus(shouldSleep = true)
            } else {
                sPreferance.saveAutoSleepStatus(shouldSleep = false)
            }
        })

//        binding.orientation.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                sPreferance.saveLineOrientation(orientation = "horizontal")
//            } else {
//                sPreferance.saveLineOrientation(orientation = "vertical")
//            }
//        })

        binding.floatingLine.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                sPreferance.saveLineFloating(floating = true)
            } else {
                sPreferance.saveLineFloating(floating = false)
            }
        })

        binding.colorPicker.setOnClickListener {
            Builder(this@MainActivity)
                .initialColor(Color.RED)
                .enableBrightness(true)
                .enableAlpha(true)
                .okTitle("Choose")
                .cancelTitle("Cancel")
                .showIndicator(true)
                .showValue(
                    false
                )
                .build()
                .show(it, object : ColorPickerPopup.ColorPickerObserver() {
                    override fun onColorPicked(color: Int) {
                        mDefaultColor = color
                        binding.colorPicker.setBackgroundColor(mDefaultColor)
                        sPreferance.saveColorCode(mDefaultColor)
                    }


                })
        }



        binding.durationTextSelector.setOnClickListener {
            val inflater = LayoutInflater.from(this)
            val popupView = inflater.inflate(R.layout.delay_menu, null)

            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            val xOffset = -25

            val yOffset = 10

            popupWindow.showAsDropDown(binding.durationTextSelector, xOffset, yOffset)

            val off = popupView.findViewById<TextView>(R.id.Off)
            val fiveSec = popupView.findViewById<TextView>(R.id.fiveSec)
            val tenSec = popupView.findViewById<TextView>(R.id.ten_sec)
            val thirtySec = popupView.findViewById<TextView>(R.id.thirty_secs)
            val oneMinute = popupView.findViewById<TextView>(R.id.oneMinue)

            off.setOnClickListener {
                binding.durationText.text = "Off"
                sPreferance.saveDelayTime(delayTime = 0)
                popupWindow.dismiss()
            }
            fiveSec.setOnClickListener {
                binding.durationText.text = "5 Sec"

                sPreferance.saveDelayTime(delayTime = 5000)
                popupWindow.dismiss()
            }
            tenSec.setOnClickListener {
                binding.durationText.text = "10 Sec"

                sPreferance.saveDelayTime(delayTime = 10000)
                popupWindow.dismiss()
            }
            thirtySec.setOnClickListener {
                binding.durationText.text = "30 Sec"

                sPreferance.saveDelayTime(delayTime = 30000)
                popupWindow.dismiss()
            }
            oneMinute.setOnClickListener {
                binding.durationText.text = "1 Min"

                sPreferance.saveDelayTime(delayTime = 60000)
                popupWindow.dismiss()
            }


        }
        binding.settings.setOnClickListener {
            val inflater = LayoutInflater.from(this)
            val popupView = inflater.inflate(R.layout.settings_menu, null)

            // Create the PopupWindow
            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true // Focusable
            )

            val xOffset = -250
            val yOffset = 10

            val appPackageName = this.packageName


            popupView.findViewById<ConstraintLayout>(R.id.shareAppI).setOnClickListener {
                val appLink = "https://play.google.com/store/apps/details?id=$appPackageName"
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Check out this awesome app: $appLink")
                    type = "text/plain"
                }
                this.startActivity(Intent.createChooser(shareIntent, "Share App via"))
                popupWindow.dismiss()

            }

            popupView.findViewById<ConstraintLayout>(R.id.privacyPolicy).setOnClickListener {
                val privacyPolicyUrl = "http://www.yourprivacypolicyurl.com"
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(privacyPolicyUrl)
                }
                this.startActivity(intent)
                popupWindow.dismiss()

            }

            popupView.findViewById<ConstraintLayout>(R.id.RateUs).setOnClickListener {
                try {
                    this.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$appPackageName")
                        )
                    )
                } catch (e: android.content.ActivityNotFoundException) {
                    this.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                        )
                    )
                }
                popupWindow.dismiss()

            }

            popupWindow.showAsDropDown(binding.settings, xOffset, yOffset)
        }


    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPostNotificationPermission() {
        ActivityCompat.requestPermissions(
            this@MainActivity,
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            REQUEST_CODE_N
        )
    }



    private val REQUEST_CODE = 1
    private val REQUEST_CODE_N = 10

    private fun checkDrawOverlayPermission(): Boolean {
        /** check if we already  have permission to draw over other apps  */
        if (!Settings.canDrawOverlays(this)) {
            Log.d(TAG, "canDrawOverlays NOK")

            return false
        } else {
            Log.d(TAG, "canDrawOverlays OK")
        }
        return true
    }


    private fun startOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun stopOverlayService() {
        val intent = Intent(this, OverlayService::class.java)
        stopService(intent)
    }

    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun showCustomDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.permissions_dialog, null)

        val dialogBuilder = AlertDialog.Builder(this).apply {
            setView(dialogView)
            setCancelable(true)
        }

        val dialog = dialogBuilder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Corrected reference to window
        dialogView.findViewById<CardView>(R.id.cardView2).setOnClickListener {

            dialog.dismiss()
            finish()
        }
        dialogView.findViewById<CardView>(R.id.cardView3).setOnClickListener {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_CODE)
            dialog.dismiss()
        }



        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {

            if (Settings.canDrawOverlays(this)) {
            } else {
                finish()
            }
        }
        else if (requestCode == REQUEST_CODE_N) {
            // Check if the user granted post-notification permission
            if (isPostNotificationPermissionGranted()) {
                // Permission granted, start your overlay service
                println("called")

                startOverlayService()
            } else {
                // Permission not granted, inform the user and offer to go to settings
                showDialogForPostNotificationPermission()
                binding.switch1.isChecked = false
            }
        }
    }
    private fun showDialogForOverlayPermission() {
        // Show a dialog to inform the user that overlay permission is required
        // and provide an option to go to settings
        AlertDialog.Builder(this)
            .setTitle("Overlay Permission Required")
            .setMessage("Please grant overlay permission for the app to work properly.")
            .setPositiveButton("Go to Settings") { dialog, which ->
                // Open app settings to allow the user to grant overlay permission
                openOverlayPermissionSettings()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // Close the activity or handle the cancellation
                finish()
            }
            .show()
    }

    private fun showDialogForPostNotificationPermission() {
        // Show a dialog to inform the user that post-notification permission is required
        // and provide an option to go to settings
        AlertDialog.Builder(this)
            .setTitle("Post Notification Permission Required")
            .setMessage("Please grant post-notification permission for the app to work properly.")
            .setPositiveButton("Go to Settings") { dialog, which ->
                // Open app settings to allow the user to grant post-notification permission
                openPostNotificationPermissionSettings()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // Handle cancellation or inform the user
                // For example, finish() the activity or show a message
                finish()
            }
            .show()
    }

    private fun openOverlayPermissionSettings() {
        // Open the system settings screen for overlay permission
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        startActivityForResult(intent, REQUEST_CODE)
    }

    private fun openPostNotificationPermissionSettings() {
        // Open the system settings screen for post-notification permission
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivityForResult(intent, 1)
    }

    private fun isPostNotificationPermissionGranted(): Boolean {
        // Check if post-notification permission is granted
        val notificationPermission = Manifest.permission.POST_NOTIFICATIONS
        return ActivityCompat.checkSelfPermission(this, notificationPermission) == PackageManager.PERMISSION_GRANTED
    }


}
