package com.shamweel.multipleimageselect.helpers

import com.shamweel.multipleimageselect.model.ImageData

class ImageHelper {

    companion object {

        var arrayList: ArrayList<ImageData> = ArrayList()

        fun addToSelectedList(imageData: ImageData): Boolean {
            if (!arrayList.contains(imageData)) {
                arrayList.add(imageData)
            }
            return true
        }

        fun removeFromSelectedList(imageData: ImageData): Boolean {
            arrayList.remove(imageData)
            return true
        }

        fun getSelectedIndex(imageData: ImageData): Int {
            if (arrayList.size == 0) {
                return -1
            }

            for (i in 0 until arrayList.size) {
                var imageData_1: ImageData = arrayList[i]
                if (imageData.Uri == imageData_1.Uri) {
                    return i + 1;
                }
            }
            return -1
        }

        fun getSelectedIndexInAdapter(images: ArrayList<ImageData>, imageData: ImageData): Int {
            for (i in 0..images.size) {
                if (imageData.Uri == images[i].Uri) {
                    return i;
                }
            }
            return -1;

        }


    }
}