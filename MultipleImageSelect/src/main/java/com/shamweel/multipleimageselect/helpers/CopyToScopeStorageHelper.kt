package com.shamweel.multipleimageselect.helpers

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class CopyToScopeStorageHelper {

    companion object {

        fun copyFileToInternalStorage(context: Context, uri: Uri, newDirName: String): String {
            val returnCursor: Cursor? = context.contentResolver.query(
                uri, arrayOf(
                    OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE
                ), null, null, null
            )


            val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor?.getColumnIndex(OpenableColumns.SIZE)
            returnCursor?.moveToFirst()
            val name = returnCursor?.getString(nameIndex!!)
            val size = (returnCursor?.getLong(sizeIndex!!)!!).toString()
            val output: File
            if (newDirName != "") {
                val dir: File = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        .toString() + "/" + newDirName
                )
                if (!dir.exists()) {
                    dir.mkdir()
                }
                output = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        .toString() + "/" + newDirName + "/" + name
                )
            } else {
                output = File(
                   context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        .toString() + "/" + name
                )
            }
            try {
                val inputStream: InputStream? =
                    context.contentResolver.openInputStream(uri)
                val outputStream = FileOutputStream(output)
                var read = 0
                val bufferSize = 1024
                val buffers = ByteArray(bufferSize)
                while (inputStream?.read(buffers).also { read = it!! } != -1) {
                    outputStream.write(buffers, 0, read)
                }
                inputStream?.close()
                outputStream.close()
            } catch (e: Exception) {
                Log.e("Exception", e.message!!)
            }
            return output.path
        }

    }

}