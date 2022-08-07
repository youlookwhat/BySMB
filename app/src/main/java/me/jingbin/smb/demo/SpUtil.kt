package me.jingbin.smb.demo

import android.content.Context
import android.content.SharedPreferences

class SpUtil {
    companion object {
        const val CONFIG: String = "data"

        private fun getSharedPreference(fileName: String): SharedPreferences {
            return App.instance.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        }

        @JvmStatic
        fun putString(key: String, value: String) {
            getSharedPreference(CONFIG).edit().putString(key, value).apply()
        }

        @JvmStatic
        fun getString(key: String): String? = getSharedPreference(CONFIG).getString(key, "")
    }
}