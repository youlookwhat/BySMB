package me.jingbin.smb.demo

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import me.jingbin.smb.BySMB
import me.jingbin.smb.OnUploadFileCallback
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream

class MainActivity : AppCompatActivity() {

    private val instance by lazy { this }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val handle = object : Handler() {
            override fun handleMessage(msg: Message?) {
                msg.let {
                    val msgtmp = msg?.obj
                    Log.e("111", msgtmp.toString())
                }
            }
        }

        /**
        - 异步线程
         */
        Thread(object : Runnable {
            override fun run() {

                try {

                    val bySmb = BySMB.with()
                        .setConfig("192.168.40.119", "jingbin", "bevol", "share")
                        .build()

                    val writeStringToFile = writeStringToFile(instance, "123", "helloSmb.txt");
                    bySmb?.writeToFile(writeStringToFile, object : OnUploadFileCallback {

                        override fun onSuccess() {
                            // 成功
                            val msg = Message.obtain()
                            msg.obj = "成功"
                            //返回主线程
                            handle.sendMessage(msg)

                        }

                        override fun onFailure(message: String) {
                            Log.e("onFailure", message)
                            val msg = Message.obtain()
                            msg.obj = message
                            //返回主线程
                            handle.sendMessage(msg)
                        }

                    })
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }

            }
        }).start()


    }


    /**
     * 在本地生成文件
     * */
    fun writeStringToFile(context: Activity, content: String, writeFileName: String): File? {
        var file: File? = null
        try {
            file = File(context.filesDir, writeFileName)
            if (!file.exists()) {
                file.parentFile?.mkdirs()
            }
            val printStream = PrintStream(FileOutputStream(file))
            printStream.println(content)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file
    }
}