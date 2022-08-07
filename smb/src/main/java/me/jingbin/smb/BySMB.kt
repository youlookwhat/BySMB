package me.jingbin.smb

import com.hierynomus.msdtyp.AccessMask
import com.hierynomus.mssmb2.SMB2CreateDisposition
import com.hierynomus.mssmb2.SMB2ShareAccess
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.SmbConfig
import com.hierynomus.smbj.auth.AuthenticationContext
import com.hierynomus.smbj.connection.Connection
import com.hierynomus.smbj.share.DiskShare
import java.io.*
import java.util.*
import java.util.concurrent.TimeUnit

class BySMB(private val builder: Builder) {

    private var connectShare: DiskShare? = null
    private var connection: Connection? = null

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
        connection = client.connect(builder.ip)
        val authContext = AuthenticationContext(builder.username, builder.password.toCharArray(), null)
        val session = connection?.authenticate(authContext)
        connectShare = session?.connectShare(builder.folderName) as DiskShare?
        if (connectShare == null) throw Exception("请检查文件夹名称")
    }

    /**
     * 向共享文件里写文件
     */
    fun writeToFile(inputFile: File?, callback: OnOperationFileCallback) {
        if (connectShare == null) {
            callback.onFailure("配置错误")
            return
        }
        if (inputFile == null || !inputFile.exists()) {
            callback.onFailure("文件不存在")
            return
        }
        var inputStream: BufferedInputStream? = null
        var outputStream: BufferedOutputStream? = null
        var openFile: com.hierynomus.smbj.share.File? = null
        try {
            inputStream = BufferedInputStream(FileInputStream(inputFile))
            openFile = connectShare!!.openFile(
                    inputFile.name,
                    EnumSet.of(AccessMask.GENERIC_WRITE), null,
                    SMB2ShareAccess.ALL,
                    // FILE_OVERWRITE_IF 可覆盖；FILE_CREATE 只能新建
                    SMB2CreateDisposition.FILE_OVERWRITE_IF, null
            )
            outputStream = BufferedOutputStream(openFile.outputStream)

            val buffer = ByteArray(1024)
            var len: Int
            while (true) {
                // 读取长度
                len = inputStream.read(buffer, 0, buffer.size)
                if (len != -1) {
                    outputStream.write(buffer, 0, len)
                } else {
                    break
                }
            }
            callback.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFailure(e.message ?: "上传失败")
        } finally {
            try {
                outputStream?.flush()
                inputStream?.close()
                // 需要调用close，不然删除会失效
                openFile?.close()
                connectShare?.close()
                connection?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取connectShare可以自己操作
     * */
    fun getConnectShare(): DiskShare? {
        return connectShare
    }

    /**
     * 获取当前根目录下的所有文件名
     */
    fun listShareFileName(callback: OnReadFileListNameCallback) {
        listShareFileName("", null, callback)
    }

    /**
     * 文件列表
     * @param path          路径 默认""则在当前的根目录下
     * @param searchPattern 文件显示规则 默认null当前目录下的所有文件 示例："*.TXT"
     */
    fun listShareFileName(path: String = "", searchPattern: String? = null, callback: OnReadFileListNameCallback) {
        if (connectShare == null) {
            callback.onFailure("配置错误")
            return
        }
        val fileNameList = arrayListOf<String>()
        try {
            val list = connectShare!!.list(path, searchPattern)
            for (information in list) {
                fileNameList.add(information.fileName)
            }
            callback.onSuccess(fileNameList)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFailure(e.message ?: "获取文件名失败")
        }
    }

    /**
     * 删除文件
     * @param path 文件名全路径，在根目录直接传文件名
     * */
    fun deleteFile(path: String, callback: OnOperationFileCallback) {
        if (connectShare == null) {
            callback.onFailure("配置错误")
            return
        }
        try {
            connectShare!!.rm(path)
            callback.onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onFailure(e.message ?: "删除失败")
        } finally {
            try {
                connectShare?.close()
                connectShare = null
                connection = null
                connection?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {


        class Builder {
            @JvmField
            var readTimeOut: Long = 60L
            @JvmField
            var writeTimeOut: Long = 60L
            @JvmField
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

            /**@param soTimeOut Socket超时时间，单位秒 默认0秒*/
            fun setSoTimeOut(soTimeOut: Long): Builder {
                this.soTimeOut = soTimeOut
                return this
            }

            fun setConfig(ip: String, username: String, password: String, folderName: String): Builder {
                this.ip = ip
                this.username = username
                this.password = password
                this.folderName = folderName
                return this
            }

            fun build(): BySMB {
                val bySMB = BySMB(this)
                bySMB.init()
                return bySMB
            }
        }

        @JvmStatic
        fun with(): Builder {
            return Builder()
        }

        /**需要先初始化*/
        @JvmStatic
        fun initProperty(soTimeout: String = "60000", responseTimeout: String = "30000") {
            System.setProperty("jcifs.smb.client.dfs.disabled", "true")
            System.setProperty("jcifs.smb.client.soTimeout", soTimeout)
            System.setProperty("jcifs.smb.client.responseTimeout", responseTimeout)
        }
    }

}