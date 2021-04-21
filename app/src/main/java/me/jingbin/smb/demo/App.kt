package me.jingbin.smb.demo

import android.content.Context
import android.support.multidex.MultiDexApplication
import me.jingbin.smb.BySMB

class App : MultiDexApplication() {

    companion object {
        lateinit var instance: Context
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 初始化
        BySMB.initProperty()
    }
}