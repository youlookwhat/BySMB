# BySMB [![](https://jitpack.io/v/youlookwhat/BySMB.svg)](https://jitpack.io/#youlookwhat/BySMB)

通过 SMB(Server Message Block)，实现手机(Android)给电脑传输数据。


### 1.前提条件
- 手机和电脑连接到同一局域网
- 电脑需要设置用户名和密码
- 设置共享文件夹 (smb://username:password@ip/folder。（登录鉴权）)
    -  Mac设置：系统偏好设置-共享-文件共享-添加共享文件夹
    -  Windows设置：文件夹-共享-高级共享-权限-打开更改权限
 - 电脑不能息屏

设置共享文件夹:
|Mac设置|Windows设置|
|:--:|:--:|
|![Mac设置](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/dd8cfa02704f4317b9eaad08662a941f~tplv-k3u1fbpfcp-watermark.image?)|![Windows设置](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/75ddb4d54b004bebbd9baf29369d074f~tplv-k3u1fbpfcp-watermark.image?)|

### 2.代码配置

1).代码引入
```kotlin
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}

dependencies {
    implementation 'com.github.youlookwhat:BySMB:1.1.0'
}
```
2).开启联网权限
```kotlon
<uses-permission android:name="android.permission.INTERNET" />
```

3).在`Application`初始化
``` kotlon
BySMB.initProperty()
```
4).得到SMB实例
``` java
val bySmb = BySMB.with()
        .setConfig(
                et_ip.text.toString(),       // ip
                et_username.text.toString(),// 用户名
                et_password.text.toString(),// 密码
                et_foldName.text.toString()// 共享文件夹名
        )
        .setReadTimeOut(60)
        .setSoTimeOut(180)
        .build()
```

查看ip:
 - Mac上查看ip：`ifconfig | grep "inet"`
 - Windows上查看ip：`ipconfig`

### 3.上传文件到电脑
```kotlin
fun upload(bySmb: BySMB) {
    // 生成文件 File
    val writeStringToFile = writeStringToFile(
            instance,
            et_content.text.toString(), // 文本内容
            et_fileName.text.toString()// 文件名，例如:随感笔记.txt
    )
    // 上传
    bySmb.writeToFile(writeStringToFile, object : OnOperationFileCallback {

        override fun onSuccess() {
            // 成功
        }

        override fun onFailure(message: String) {
            // 失败
        }

    })
}
```

注意：如上传相同文件名的文件，会覆盖之前文件的内容。

### 4.查找电脑上的文件列表
```kotlin
fun listFile(bySmb: BySMB){
    // 读取根目录下的所有文件，重载方法("", "*.txt", callback)
    bySmb.listShareFileName(object : OnReadFileListNameCallback {
        override fun onSuccess(fileNameList: List<String>) {
            // 读取成功 fileNameList文件名列表
        }

        override fun onFailure(message: String) {
             // 失败
        }
    })
}
```

### 5.删除电脑上的文件
```kotlin
fun deleteFile(bySmb: BySMB){
    bySmb.deleteFile(et_fileName.text.toString(), object : OnOperationFileCallback {
        override fun onSuccess() {
	    // 删除成功
        }

        override fun onFailure(message: String) {
            // 失败
        }
    })
}
```

