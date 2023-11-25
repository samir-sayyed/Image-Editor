package com.sam.imageeditor.fragments

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.sam.imageeditor.adapter.ImageAdapter
import com.sam.imageeditor.databinding.FragmentHomeBinding
import com.sam.imageeditor.util.ImageSelectionHelper
import com.sam.imageeditor.util.ImportImagesHelper
import com.sam.imageeditor.util.ShowDialogParams
import com.sam.imageeditor.util.showDialog
import java.io.File


class HomeFragment : Fragment() {

    companion object {
        private const val TAG = "HomeFragment"
    }

    private var viewBinding: FragmentHomeBinding? = null
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
        val recentImages = ImportImagesHelper.getEditedImages(requireContext())

        if(recentImages.isNotEmpty()) {
            viewBinding?.imageRecyclerView?.visibility = View.VISIBLE
            viewBinding?.tvNoRecentEdits?.visibility = View.GONE
            val imageAdapter =
                ImageAdapter(ImportImagesHelper.getEditedImages(requireContext()), onClick = {
                    findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToImageEditFragment(it.toString())
                    )
                })
            viewBinding?.imageRecyclerView?.adapter = imageAdapter
            val spanCount = 3
            val layoutManager = GridLayoutManager(requireContext(), spanCount)
            viewBinding?.imageRecyclerView?.layoutManager = layoutManager
            viewBinding?.imageRecyclerView?.adapter = imageAdapter
        }else{
            viewBinding?.imageRecyclerView?.visibility = View.GONE
            viewBinding?.tvNoRecentEdits?.visibility = View.VISIBLE
        }
    }

    private fun initListeners() {
        viewBinding?.btnCamera?.setOnClickListener {
            imageUri = FileProvider.getUriForFile(
                requireContext(),
                "com.sam.imageeditor.provider",
                File(
                    requireContext().getExternalFilesDir(null)
                        .toString() + File.separator.toString() + "pic_" + System.currentTimeMillis()
                        .toString() + ".jpg"
                )
            )
            ImageSelectionHelper.selectImage(
                requestPermissionLauncher = requestPermissionLauncher,
                getPictureResult = getPictureResult,
                getPicturePickResult = getPicturePickResult,
                imageUri = imageUri,
                fragment = this,
                isSelectFromCamera = true
            )
        }

        viewBinding?.btnGallary?.setOnClickListener {
            ImageSelectionHelper.selectImage(
                requestPermissionLauncher = requestPermissionLauncher,
                getPictureResult = getPictureResult,
                getPicturePickResult = getPicturePickResult,
                imageUri = imageUri,
                fragment = this,
                isSelectFromCamera = false
            )
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permissionGranted ->
            if (permissionGranted) {
                val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                    putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                }
                getPictureResult.launch(takePicture)
            } else {
                //ask permission again
                showDialog(
                    ShowDialogParams(
                        "Require Camera access",
                        "Kindly provide camera access to the app for capturing image",
                        "OK",
                        {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri = Uri.fromParts("package", activity?.packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        },
                        "Cancel",
                        {},
                        showMessage = true
                    )
                )
            }
        }

    private val getPictureResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                try {
                    if (imageUri != null) {
                       findNavController().navigate(
                           HomeFragmentDirections.actionHomeFragmentToImageEditFragment(imageUri.toString())
                       )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "upload failed: $e ")

                }
            }
        }

    private val getPicturePickResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedImage: Uri? = it.data?.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                if (selectedImage != null) {
                    try {
                        val cursor: Cursor? =
                            requireContext().contentResolver.query(
                                selectedImage,
                                filePathColumn,
                                null,
                                null,
                                null
                            )
                        if (cursor != null) {
                            cursor.moveToFirst()
                            cursor.close()
                            findNavController().navigate(
                                HomeFragmentDirections.actionHomeFragmentToImageEditFragment(selectedImage.toString())
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "upload failed: $e")
                    }
                }
            }
        }

}