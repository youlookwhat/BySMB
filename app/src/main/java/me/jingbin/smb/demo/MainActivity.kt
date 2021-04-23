package me.jingbin.smb.demo

import android.app.Activity
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import me.jingbin.smb.BySMB
import me.jingbin.smb.OnReadFileListNameCallback
import me.jingbin.smb.OnOperationFileCallback
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private val instance by lazy { this }
    private lateinit var handle: MyHandle
    private var progressDialog: ProgressDialog? = null

    companion object {
        class MyHandle(activity: MainActivity) : Handler() {

            private var mWeakReference: WeakReference<MainActivity>? = WeakReference<MainActivity>(activity)

            override fun handleMessage(msg: Message) {
                val activity = mWeakReference?.get()
                activity?.let {
                    activity.progressDialog?.hide()
                    when (msg.what) {
                        1 -> {
                            // 读取文件列表成功
                            val list = msg.obj as List<*>
                            activity.tv_file_list.text = list.toString()
                        }
                        2 -> {
                            // 写入或删除成功后再次读取
                            activity.tv_log.text = msg.obj.toString()
                            activity.operation(2)
                        }
                        else -> {
                            // 失败
                            val msgContent = msg.obj
                            activity.tv_log.text = msgContent.toString()
                            Toast.makeText(activity.instance, msgContent.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        et_ip.setText(SpUtil.getString("ip"))
        et_username.setText(SpUtil.getString("username"))
        et_password.setText(SpUtil.getString("password"))
        et_foldName.setText(SpUtil.getString("foldName"))
        et_content.setText(SpUtil.getString("content"))
        et_fileName.setText(SpUtil.getString("contentFileName"))

        handle = MyHandle(this)
        tv_send.setOnClickListener { operation(1); saveEditValue() }
        tv_read.setOnClickListener { operation(2); saveEditValue() }
        tv_delete.setOnClickListener { operation(3); saveEditValue() }

    }

    /**增加 查看 删除*/
    private fun operation(state: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
        }
        when (state) {
            1 -> progressDialog?.setMessage("上传中...")
            2 -> progressDialog?.setMessage("读取中...")
            3 -> progressDialog?.setMessage("删除中...")
        }
        progressDialog?.show()
        /**
         * 异步线程
         */
        Thread(Runnable {
            try {
                val bySmb = BySMB.with()
                        .setConfig(
                                et_ip.text.toString(),
                                et_username.text.toString(),
                                et_password.text.toString(),
                                et_foldName.text.toString()
                        )
                        .setReadTimeOut(60)
                        .setSoTimeOut(180)
                        .build()

                when (state) {
                    1 -> {
                        val writeStringToFile = writeStringToFile(
                                instance,
                                et_content.text.toString(),
                                et_fileName.text.toString()
                        )
                        // 写入
                        bySmb.writeToFile(writeStringToFile, object : OnOperationFileCallback {

                            override fun onSuccess() {
                                // 成功
                                val msg = Message.obtain()
                                msg.obj = "写入成功"
                                msg.what = 2
                                handle.sendMessage(msg)
                            }

                            override fun onFailure(message: String) {
                                val msg = Message.obtain()
                                msg.obj = message
                                handle.sendMessage(msg)
                            }

                        })
                    }
                    2 -> {
                        // 读取 ("", "*.txt", callback)
                        bySmb.listShareFileName(object : OnReadFileListNameCallback {
                            override fun onSuccess(fileNameList: List<String>) {
                                // 成功
                                val msg = Message.obtain()
                                msg.obj = fileNameList
                                msg.what = 1
                                handle.sendMessage(msg)
                            }

                            override fun onFailure(message: String) {
                                val msg = Message.obtain()
                                msg.obj = message
                                handle.sendMessage(msg)
                            }
                        })
                    }
                    3 -> {
                        // 删除
                        bySmb.deleteFile(et_fileName.text.toString(), object : OnOperationFileCallback {
                            override fun onSuccess() {
                                val msg = Message.obtain()
                                msg.obj = "删除成功"
                                msg.what = 2
                                handle.sendMessage(msg)
                            }

                            override fun onFailure(message: String) {
                                val msg = Message.obtain()
                                msg.obj = message
                                handle.sendMessage(msg)
                            }
                        })
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                val msg = Message.obtain()
                msg.obj = "连接失败： " + e.message
                handle.sendMessage(msg)
            }
        }).start()
    }

    /**
     * 在本地生成文件
     * */
    private fun writeStringToFile(context: Activity, content: String, writeFileName: String): File? {
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
        saveEditValue()
    }

    private fun saveEditValue() {
        SpUtil.putString("ip", et_ip.text.toString())
        SpUtil.putString("username", et_username.text.toString())
        SpUtil.putString("password", et_password.text.toString())
        SpUtil.putString("foldName", et_foldName.text.toString())
        SpUtil.putString("content", et_content.text.toString())
        SpUtil.putString("contentFileName", et_fileName.text.toString())
    }
}