package com.sam.imageeditor.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

object ImportImagesHelper {

    fun getEditedImages(context: Context): List<Uri> {
        val editedImagesList = mutableListOf<Uri>()
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // Check if the storage directory exists
        if (storageDir != null && storageDir.exists()) {
            // List all files in the directory
            val files = storageDir.listFiles()

            // Filter files based on your naming convention or other criteria
            for (file in files) {
                if (file.isFile && file.name.startsWith("JPEG_") && file.name.endsWith(".jpg")) {
                    val imageURI = FileProvider.getUriForFile(
                        context,
                        "com.sam.imageeditor.provider",
                        file
                    )
                    editedImagesList.add(imageURI)
                }
            }
        }

        return editedImagesList
    }

}