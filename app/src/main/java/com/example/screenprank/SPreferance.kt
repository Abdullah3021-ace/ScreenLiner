package com.example.screenprank

import android.content.Context

class SPreferance(context: Context) {
    private companion object {
        private const val APP_PREFS = "Screen Liner"
        private const val COLOR_CODE = "colorCode"
        private const val DEVICE_SLEEP = "Device Sleep"
        private const val ORIENTATION = "Line Orientation"
        private const val FLOATING_LINE = "Line Movement"
        private const val DELAY_TIME = "Delay Time"
    }

    private val sharedPreferences = context.getSharedPreferences(APP_PREFS, Context.MODE_PRIVATE)
    private val editor = sharedPreferences.edit()


    fun saveColorCode(colorCode: Int) {
        editor.putInt(COLOR_CODE, colorCode)
        editor.apply()
    }
    fun saveDelayTime(delayTime: Int) {
        editor.putInt(DELAY_TIME, delayTime)
        editor.apply()
    }

    fun saveAutoSleepStatus(shouldSleep: Boolean) {
        editor.putBoolean(DEVICE_SLEEP, shouldSleep)
        editor.apply()
    }

    fun saveLineOrientation(orientation: String) {
        editor.putString(ORIENTATION, orientation)
        editor.apply()
    }

    fun saveLineFloating(floating: Boolean) {
        editor.putBoolean(FLOATING_LINE, floating)
        editor.apply()
    }

    fun getColorCode(): Int {
        return sharedPreferences.getInt(COLOR_CODE, 0)
    }
    fun getDelayTime(): Int {
        return sharedPreferences.getInt(DELAY_TIME, 0)
    }

    fun getSleepStatus(): Boolean {
        return sharedPreferences.getBoolean(DEVICE_SLEEP, false)
    }

    fun getFloatationStatus(): Boolean {
        return sharedPreferences.getBoolean(FLOATING_LINE, false)
    }

    fun getOrientation(): String? {
        return sharedPreferences.getString(ORIENTATION, "vertical")
    }
}