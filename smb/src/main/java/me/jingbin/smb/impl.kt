package me.jingbin.smb

interface OnUploadFileCallback {

    fun onSuccess()
    fun onFailure(message: String)
}