# BySMB [![](https://jitpack.io/v/youlookwhat/BySMB.svg)](https://jitpack.io/#youlookwhat/BySMB)

通过 SMB(Server Message Block)，实现设备(Android)给电脑传输数据。

### 硬件条件
 -  连接到同一局域网！手机和电脑连接到同一局域网。
 -  电脑需要有用户名和密码
 -  设置共享文件夹 (smb://username:password@ip/folder。（登录鉴权）)
 	-  mac 电脑需要在选项里打开当前账户
 	-  window 电脑 文件夹-共享-高级共享-权限-打开更改权限

### 代码配置
#### 1.代码引入
```kotlin
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}

dependencies {
    implementation 'com.github.youlookwhat:BySMB:1.0.0'
}
```
#### 2.开启联网权限
``` kotlon
<uses-permission android:name="android.permission.INTERNET" />
```

#### 3.需要先初始化
``` kotlon
BySMB.initProperty()
```

#### 4.参数配置及上传（注意在子线程）

```kotlin
try {
    // 初始化
    val bySmb = BySMB.with()
        .setConfig(
            et_ip.text.toString(),       // 电脑ip
            et_username.text.toString(), // 电脑用户名
            et_password.text.toString(), // 电脑用户名的密码
            et_foldName.text.toString()  // 共享文件夹名称 
        )
        .setReadTimeOut(60)
        .setSoTimeOut(180)
        .build()

    // 生成文件 File
    val writeStringToFile = writeStringToFile(
        instance,
        et_content.text.toString(),
        et_fileName.text.toString()
    )
    // 上传文件到电脑上的共享文件夹内
    bySmb?.writeToFile(writeStringToFile, object : OnUploadFileCallback {

        override fun onSuccess() {
            // 成功
        }

        override fun onFailure(message: String) {
            // 失败 
            Log.e("onFailure", message)
        }

    })
} catch (e: java.lang.Exception) {
	// 连接失败
    e.printStackTrace()
}



```