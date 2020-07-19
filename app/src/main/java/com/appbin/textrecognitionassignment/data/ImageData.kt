package com.appbin.textrecognitionassignment.data

import android.graphics.Bitmap
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ImageData(val bitmap:Bitmap, val array : ArrayList<String>) : Parcelable