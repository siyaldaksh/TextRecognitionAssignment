package com.appbin.textrecognitionassignment.ui.photo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PhotoViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    var _galleryButton = MutableLiveData<Boolean>()
    val galleryButton : LiveData<Boolean>
        get() = _galleryButton

    var _cameraButton = MutableLiveData<Boolean>()
    val cameraButton : LiveData<Boolean>
        get() = _cameraButton

    fun selectImage(){
        _galleryButton.value = true
    }

    fun doneSelectVideo(){
        _galleryButton.value = false
    }

    fun captureImage(){
        _cameraButton.value = true
    }

    fun donecaptureImage(){
        _cameraButton.value = false
    }

}
