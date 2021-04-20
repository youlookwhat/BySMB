package me.jingbin.smb

interface OnUploadFileCallback {

    fun onSuccess()
    fun onFailure(message: String)
}

interface OnReadFileListNameCallback{
    fun onSuccess(fileNameList:List<String>)
    fun onFailure(message: String)
}