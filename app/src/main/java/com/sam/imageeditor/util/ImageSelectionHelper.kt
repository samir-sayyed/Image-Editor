package com.sam.imageeditor.util

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment


object ImageSelectionHelper {
    // function to let's the user to choose image from camera or gallery
    fun selectImage(
        requestPermissionLauncher: ActivityResultLauncher<String>,
        getPictureResult: ActivityResultLauncher<Intent>,
        getPicturePickResult: ActivityResultLauncher<Intent>,
        imageUri: Uri?,
        fragment: Fragment,
        isSelectFromCamera: Boolean
    ) {
        if (isSelectFromCamera) {
            // Open the camera and get the photo
            checkPermission(
                getPictureResult,
                imageUri,
                fragment,
                requestPermissionLauncher
            )
        } else {
            // choose from  external storage
            val pickPhoto =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getPicturePickResult.launch(pickPhoto)
        }
    }


    private fun checkPermission(
        getPictureResult: ActivityResultLauncher<Intent>,
        imageUri: Uri?,
        fragment: Fragment,
        requestPermissionLauncher: ActivityResultLauncher<String>
    ) {
        if (ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            }
            getPictureResult.launch(takePicture)
        } else {
            // You can directly ask for the permission.
            // The registered ActivityResultCallback gets the result of this request.
            requestPermissionLauncher.launch(
                Manifest.permission.CAMERA
            )
        }
    }
}