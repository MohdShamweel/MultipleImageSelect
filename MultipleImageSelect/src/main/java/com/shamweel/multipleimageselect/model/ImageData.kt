package com.shamweel.multipleimageselect.model

import android.os.Parcelable
import java.io.Serializable

data class ImageData(
    val id: Long,
    val title: String,
    var Uri: String,
) : Serializable
