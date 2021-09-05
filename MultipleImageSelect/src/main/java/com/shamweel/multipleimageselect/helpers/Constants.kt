package com.shamweel.multipleimageselect.helpers

import android.Manifest

class Constants {
    companion object {

        var REQUIRED_READ_PERMISSION =  Manifest.permission.READ_EXTERNAL_STORAGE

        const val REQUEST_CODE = 2000
        const val BUNDLE_EXTRA_ALBUM = "bundle_album"
        const val BUNDLE_EXTRA_IMAGES = "bundle_images"
        const val INTENT_EXTRA_IMAGES = "images"
        const val INTENT_EXTRA_LIMIT = "limit"
        const val DEFAULT_LIMIT = 10
        const val DEFAULT_URI_REQUIRED = true
        const val INTENT_IMAGE_URI = "image_uri"
        const val DEFAULT_FRAGMENT_STACK = 1
        const val TRANSITION_NAME = "transition_name"

        /**
         * There are two ways to get Images
         * 1. URI -> External Storage Image Uri's
         * 2. PATH -> AS of SDK 29, we can't access an image by path on external Storage, so we'll copy the selected Images to our Scope Storage and will return their particular paths.
         *
         * Currently, Uri field is String, if chosen 1. Uri -> Uri String else 2. Uri -> Path String
         *
         * use @INTENT_GET_URI
         * intent.putExtra(Constants.INTENT_GET_URI, true) -> URI
         * intent.putExtra(Constants.INTENT_GET_URI, false) -> SCOPED PATH
         *
         */
        const val INTENT_GET_URI = "intent_get_uri"


        //Runnable Delays
        const val RESET_SELECTED_LONG_MILLIS : Long = 500
        const val PREVIEW_DIALOG_WAIT_MILLIS : Long = 400
    }


}