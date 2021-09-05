package com.shamweel.multipleimageselect.helpers

import android.content.Context

class GlobalVariables {

    companion object {
        //Maximum number of images that can be selected at a time
        var currentLimit = 0

        //Result intent to be list of URIs or Paths
        var isUriRequired = true
    }
}