package me.jingbin.smb

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.share.DiskShare
import org.apache.log4j.BasicConfigurator
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit

open class BySMB(val builder: Builder) {

    var connectShare: DiskShare? = null

    fun init() {
        val config = SmbConfig.builder()
            // 设置读取超时
            .withTimeout(builder.readTimeOut, TimeUnit.SECONDS)
            // 设置写入超时
            .withWriteTimeout(builder.writeTimeOut, TimeUnit.SECONDS)
            // 设置Socket链接超时
            .withSoTimeout(builder.soTimeOut, TimeUnit.SECONDS)
            .build()

        val client = SMBClient(config)
        val connect = client.connect(builder.ip)
        val authContext =
            AuthenticationContext(builder.username, builder.password.toCharArray(), "DOMAIN")
        val session = connect.authenticate(authContext)
        connectShare = session.connectShare(builder.folderName) as DiskShare?
        if (connectShare == null) throw Exception("请检查文件夹名称")

    }

    /**
     * 向共享文件里写文件
     */
    fun writeToFile(inputFile: File?, callback: OnUploadFileCallback) {
        if (connectShare == null) {
            callback.onFailure("配置错误")
            return
        }
        if (inputFile == null || !inputFile.exists()) {
            callback.onFailure("文件不存在")
            return
        }
        val inputStream = BufferedInputStream(FileInputStream(inputFile))
        val openFile = connectShare!!.openFile(
            inputFile.name,
            EnumSet.of(AccessMask.GENERIC_WRITE), null,
            SMB2ShareAccess.ALL,
            SMB2CreateDisposition.FILE_CREATE, null
        )
        val outputStream = BufferedOutputStream(openFile.outputStream)
        try {
            val buffer = ByteArray(4096)
            var len = 0
            // 读取长度
            while (len != -1) {
                len = inputStream.read(buffer, 0, buffer.size)
                outputStream.write(buffer, 0, len)
            }
        } finally {
            try {
                outputStream.flush()
                inputStream.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                callback.onFailure(e.message ?: "上传失败")
            }
            callback.onSuccess()
        }
    }


    companion object {

        class Builder {
            var readTimeOut: Long = 60L
            var writeTimeOut: Long = 60L
            var soTimeOut: Long = 0L
            var ip: String = ""
            var username: String = ""
            var password: String = ""
            var folderName: String = ""


            /**@param readTimeOut 读取时间，默认60秒*/
            fun setReadTimeOut(readTimeOut: Long): Builder {
                this.readTimeOut = readTimeOut
                return this
            }

            /**@param writeTimeOut 写入时间，单位秒 默认60秒*/
            fun setWriteTimeOut(writeTimeOut: Long): Builder {
                this.writeTimeOut = writeTimeOut
                return this
            }

            /**@param soTimeOut Socket超时，单位秒 默认0秒*/
            fun setSoTimeOut(soTimeOut: Long): Builder {
                this.soTimeOut = soTimeOut
                return this
            }

            fun setConfig(
                ip: String,
                username: String,
                password: String,
                folderName: String
            ): Builder {
                this.ip = ip
                this.username = username
                this.password = password
                this.folderName = folderName
                return this
            }

            fun build(): BySMB? {
                val bySMB = BySMB(this)
                bySMB.init()
                return bySMB
//                return try {
//                    bySMB.init()
//                    bySMB
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                    null
//                }
            }
        }

        fun with(): Builder {
            return Builder()
        }

        fun initProperty(soTimeout: String = "60000", responseTimeout: String = "30000") {
            System.setProperty("jcifs.smb.client.dfs.disabled", "true")
            System.setProperty("jcifs.smb.client.soTimeout", soTimeout)
            System.setProperty("jcifs.smb.client.responseTimeout", responseTimeout)
            BasicConfigurator.configure()
        }
    }

}