package com.sam.imageeditor.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.sam.imageeditor.R
import com.sam.imageeditor.databinding.FragmentImageEditBinding
import com.sam.imageeditor.util.ShowDialogParams
import com.sam.imageeditor.util.showDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong


class ImageEditFragment : Fragment() {

    companion object {
        private const val TAG = "ImageEditFragment"
    }

    private var viewBinding: FragmentImageEditBinding? = null
    private val args by navArgs<ImageEditFragmentArgs>()
    private lateinit var imageBitmap: Bitmap
    private var isDiscardChanges = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentImageEditBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    remove()
                    isEnabled = false
                    if (isDiscardChanges) {
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }else
                        showUnsavedChangesDailog()
                }
            })
        imageBitmap = getBitmapFromURI()!!
        loadImageInView()
        getEXIFInfo(args.imageUri.toUri())
        initOnClickListeners()
    }

    private fun initOnClickListeners() {
        viewBinding?.infoButton?.setOnClickListener {
            showDialog(
                ShowDialogParams(
                    title = getString(R.string.image_info),
                    message = getEXIFInfo(args.imageUri.toUri()),
                    textPositive = getString(R.string.close)
                )
            )
        }

        viewBinding?.saveButton?.setOnClickListener {
            saveBitmapToStorage(imageBitmap)
        }

        viewBinding?.flipHorizontalButton?.setOnClickListener {
            flipBitmap(true, false).also {
                imageBitmap = it
                loadImageInView()
            }
        }

        viewBinding?.flipVerticalButton?.setOnClickListener {
            flipBitmap(false, true).also {
                imageBitmap = it
                loadImageInView()
            }
        }

        viewBinding?.backButton?.setOnClickListener {
            showUnsavedChangesDailog()
        }

        viewBinding?.undoButton?.setOnClickListener {
            imageBitmap = getBitmapFromURI()!!
            loadImageInView()
            viewBinding?.undoButton?.visibility = View.GONE
        }

        viewBinding?.cropButton?.setOnClickListener {
            cropImage(0.2f, 0.2f, 0.8f, 0.8f).also { croppedBitmap ->
                imageBitmap = croppedBitmap
                loadImageInView()
                viewBinding?.undoButton?.visibility = View.VISIBLE
            }
        }
    }

    private fun showUnsavedChangesDailog() {
        showDialog(
            ShowDialogParams(
                title = getString(R.string.unsaved_file),
                message = getString(R.string.are_you_sure),
                textNegative = getString(R.string.discard),
                textPositive = getString(R.string.save),
                negativeListener = {
                    isDiscardChanges = true
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                },
                positiveListener = {
                    isDiscardChanges = true
                    saveBitmapToStorage(imageBitmap)
                }
            )
        )
    }

    private fun loadImageInView() {
        Glide.with(requireContext())
            .load(imageBitmap)
            .into(viewBinding?.imageView!!)
    }

    private fun getBitmapFromURI(): Bitmap? = BitmapFactory.decodeStream(
        requireContext().contentResolver.openInputStream(args.imageUri.toUri())
    )

    private fun flipBitmap(flipHorizontal: Boolean, flipVertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.postScale(if (flipHorizontal) -1f else 1f, if (flipVertical) -1f else 1f)
        return Bitmap.createBitmap(
            imageBitmap,
            0,
            0,
            imageBitmap.width,
            imageBitmap.height,
            matrix,
            true
        )
    }


    private fun cropImage(
        leftPercentage: Float,
        topPercentage: Float,
        rightPercentage: Float,
        bottomPercentage: Float
    ): Bitmap {
        val width = imageBitmap.width
        val height = imageBitmap.height
        val left = (leftPercentage * width).toInt()
        val top = (topPercentage * height).toInt()
        val right = (rightPercentage * width).toInt()
        val bottom = (bottomPercentage * height).toInt()
        return Bitmap.createBitmap(imageBitmap, left, top, right - left, bottom - top)
    }

    private fun saveBitmapToStorage(bitmap: Bitmap) {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        try {
            val imageFile = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
            )

            val fos = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
            fos.close()
            if (imageFile.exists()) {
                Toast.makeText(requireContext(), "Image saved successfully", Toast.LENGTH_SHORT)
                    .show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getEXIFInfo(imageUri: Uri): String {

        val inputStream = requireActivity().contentResolver.openInputStream(imageUri)
        val exifInterface = ExifInterface(inputStream!!)
        val exifData = StringBuilder()
        val imageWidth = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)
        val imageHeight = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)
        val orientation = exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION)
        val size = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)
        val fileSizeInKB = size?.toLong()?.div(10.24)?.roundToLong()
        exifData.append("Image Width: $imageWidth\n")
        exifData.append("Image Height: $imageHeight\n")
        exifData.append("Orientation: $orientation\n")
        exifData.append("Size: $fileSizeInKB Kb")

        inputStream.close()
        return exifData.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }


}