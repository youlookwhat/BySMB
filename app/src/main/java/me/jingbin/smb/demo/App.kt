package me.jingbin.smb.demo

import androidx.multidex.MultiDexApplication
import me.jingbin.smb.BySMB

class App: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
//        System.setProperty("jcifs.smb.client.dfs.disabled", "true")
//        System.setProperty("jcifs.smb.client.soTimeout", "1000000")
//        System.setProperty("jcifs.smb.client.responseTimeout", "30000")
//        BasicConfigurator.configure()

        BySMB.init()
    }
}