package me.jingbin.smb.demo

import android.content.Context
import androidx.multidex.MultiDexApplication
import me.jingbin.smb.BySMB

class App : MultiDexApplication() {

    companion object {
        lateinit var instance: Context
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 可直接在Activity里初始化
//        BySMB.initProperty()
    }
}