package com.appbin.textrecognitionassignment.ui.photo

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.appbin.textrecognitionassignment.databinding.PhotoFragmentBinding
import com.google.android.gms.vision.text.TextRecognizer
import android.widget.Toast
import android.os.Environment
import android.util.Pair
import android.widget.ImageView
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.appbin.textrecognitionassignment.R
import com.appbin.textrecognitionassignment.data.ImageData
import com.google.android.gms.tasks.OnFailureListener
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class PhotoFragment : Fragment() {

    private lateinit var viewModel: PhotoViewModel

    private var mImageMaxWidth: Int? = null
    private var mImageMaxHeight: Int? = null

    companion object {
        private const val IMAGE_SELECT_CODE = 2
        private const val REQUEST_IMAGE_CAPTURE = 1
    }

    private lateinit var imageUri: Uri

    private lateinit var textRecognizer: TextRecognizer

    private lateinit var imageBitmap: Bitmap

    private lateinit var mImageView: ImageView

    private val lineArray: ArrayList<String> = ArrayList()

    var isSuccess: Boolean = false

    lateinit var currentPhotoPath: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProvider(this).get(PhotoViewModel::class.java)

        val binding: PhotoFragmentBinding =
            DataBindingUtil.inflate(inflater, R.layout.photo_fragment, container, false)

        binding.photoViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        textRecognizer = TextRecognizer.Builder(activity).build()
        mImageView = binding.mImageView

        viewModel.galleryButton.observe(viewLifecycleOwner, Observer {
            if (it) {
                galleryImage()
                viewModel.doneSelectVideo()
            }
        })

        viewModel.cameraButton.observe(viewLifecycleOwner, Observer {
            if (it) {
                captureImage()
                viewModel.donecaptureImage()
            }
        })

        return binding.root
    }


    private fun captureImage() {
        /*Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }*/
        dispatchTakePictureIntent()
    }

    private fun galleryImage() {
        val i = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        i.type = "image/*"
        startActivityForResult(
            i,
            IMAGE_SELECT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            //imageBitmap = data?.extras?.get("data") as Bitmap
            galleryAddPic()
            val file = File(currentPhotoPath)
            imageBitmap = BitmapFactory.decodeFile(file.absolutePath)
            imageBitmap = handlingRotation()
            //
            /*imageBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, Uri.parse(currentPhotoPath))*/
            isSuccess = true

        }

        if (requestCode == IMAGE_SELECT_CODE && resultCode == RESULT_OK) {
            imageUri = data?.data!!
            imageBitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageUri)
            isSuccess = true

        }

        if (isSuccess) {
            val targetedSize = getTargetedWidthHeight()

            val targetWidth = targetedSize.first
            val maxHeight = targetedSize.second

            val scaleFactor = Math.max(
                imageBitmap.getWidth().toFloat() / targetWidth.toFloat(),
                imageBitmap.getHeight().toFloat() / maxHeight.toFloat()
            )

            val resizedBitmap = Bitmap.createScaledBitmap(
                imageBitmap,
                (imageBitmap.getWidth() / scaleFactor).toInt(),
                (imageBitmap.getHeight() / scaleFactor).toInt(),
                true
            )

            imageBitmap = resizedBitmap
            runTextRecognition(imageBitmap)
            isSuccess = false
        }
    }

    private fun getTargetedWidthHeight(): Pair<Int, Int> {
        val targetWidth: Int
        val targetHeight: Int
        val maxWidthForPortraitMode = getImageMaxWidth()!!
        val maxHeightForPortraitMode = getImageMaxHeight()!!
        targetWidth = maxWidthForPortraitMode
        targetHeight = maxHeightForPortraitMode
        return Pair(targetWidth, targetHeight)
    }

    private fun getImageMaxWidth(): Int? {
        if (mImageMaxWidth == null) {
            mImageMaxWidth = mImageView.getWidth()
        }

        return mImageMaxWidth
    }

    private fun getImageMaxHeight(): Int? {
        if (mImageMaxHeight == null) {
            mImageMaxHeight = mImageView.getHeight()
        }

        return mImageMaxHeight
    }

    private fun runTextRecognition(resultImage: Bitmap) {
        val image = InputImage.fromBitmap(resultImage, 0)
        val recognizer = TextRecognition.getClient()

        recognizer.process(image)
            .addOnSuccessListener { texts ->
                processTextRecognitionResult(texts)
            }
            .addOnFailureListener(
                object : OnFailureListener {
                    override fun onFailure(e: Exception) {
                        // Task failed with an exception
                        Toast.makeText(activity, "Error...", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                })
    }

    private fun processTextRecognitionResult(texts: Text) {
        val blocks = texts.textBlocks

        if (blocks.size == 0) {
            Toast.makeText(activity, "No Text Found...", Toast.LENGTH_SHORT).show()
            return
        }
        lineArray.clear()
        for (i in blocks.indices) {

            val lines = blocks[i].lines
            for(j:Int in 0..lines.size-1){
                lineArray.add(lines[j].text)
            }


        }

        val array = Array(1) {
            ImageData(imageBitmap, lineArray)
        }

        findNavController().navigate(
            PhotoFragmentDirections.actionPhotoFragmentToShowTextFragment(
                array
            )
        )
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(activity!!.packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully create
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        activity!!,
                        "com.appbin.textrecognitionassignment.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }


    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }


    }

    private fun galleryAddPic() {
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(currentPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            activity!!.sendBroadcast(mediaScanIntent)
        }
    }

    private fun handlingRotation() : Bitmap{
        var rotate = 0f
        try {
            val imageFile = File(currentPhotoPath)
            val exif : ExifInterface  =  ExifInterface(imageFile.getAbsolutePath())

            val orientation = exif.getAttributeInt (
                    ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL)

            when(orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270f

                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180f

                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90f

            }
        } catch (e : Exception ) {
            e.printStackTrace()
        }

        /****** Image rotation ****/
        val matrix = Matrix()
        matrix.postRotate(rotate)
        return Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.width, imageBitmap.height, matrix, true)
    }

}
