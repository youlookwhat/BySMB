package me.jingbin.smb

interface OnOperationFileCallback {

    fun onSuccess()
    fun onFailure(message: String)
}

interface OnReadFileListNameCallback {

    fun onSuccess(fileNameList: List<String>)
    fun onFailure(message: String)
}