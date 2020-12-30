package me.jingbin.smb.demo

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import me.jingbin.smb.BySMB
import me.jingbin.smb.OnUploadFileCallback
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.net.UnknownHostException

class MainActivity : AppCompatActivity() {

    private lateinit var handle: Handler
    private val instance by lazy { this }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        et_ip.setText(SpUtil.getString("ip"))
        et_username.setText(SpUtil.getString("username"))
        et_password.setText(SpUtil.getString("password"))
        et_foldName.setText(SpUtil.getString("foldName"))
        et_content.setText(SpUtil.getString("content"))
        et_fileName.setText(SpUtil.getString("contentFileName"))

        handle = object : Handler() {
            override fun handleMessage(msg: Message?) {
                msg.let {
                    val msgtmp = msg?.obj
                    Log.e("handleMessage", msgtmp.toString())
                    tv_log.text = msgtmp.toString()
                    progressDialog.hide()
                }
            }
        }

        tv_send.setOnClickListener { startUpload() }

    }

    lateinit var progressDialog: ProgressDialog

    fun startUpload() {
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("上传中...")
        progressDialog.show()
        /**
         * 异步线程
         */
        Thread(object : Runnable {
            override fun run() {

                try {

                    val bySmb = BySMB.with()
                        .setConfig(
                            et_ip.text.toString(), et_username.text.toString(),
                            et_password.text.toString(), et_foldName.text.toString()
                        )
//                        .setConfig("192.168.40.119", "景彬", "bevol", "share")
                        .setReadTimeOut(60)
                        .setSoTimeOut(180)
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
                    val msg = Message.obtain()
                    msg.obj = "连接失败： " + e.message
                    //返回主线程
                    handle.sendMessage(msg)
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

    override fun onBackPressed() {
        super.onBackPressed()
        SpUtil.putString("ip", et_ip.text.toString())
        SpUtil.putString("username", et_username.text.toString())
        SpUtil.putString("password", et_password.text.toString())
        SpUtil.putString("foldName", et_foldName.text.toString())
        SpUtil.putString("content", et_content.text.toString())
        SpUtil.putString("contentFileName", et_fileName.text.toString())
    }
}