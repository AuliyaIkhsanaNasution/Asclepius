package com.dicoding.asclepius.view

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

class MainActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener  {
    private lateinit var imageHelper: ImageClassifierHelper
    private var currentImageUri: Uri? = null
    private lateinit var mainBinding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        ini adalah binding untuk activity_main.xml dan binding button
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
        mainBinding.analyzeButton.setOnClickListener { analyzeImage() }
        mainBinding.galleryButton.setOnClickListener { startGallery() }

//        ini adalah image classifier yang akan digunakan
        imageHelper = ImageClassifierHelper(
            threshold = 0.6f,
            maxResults = 1,
            modelName = "cancer_classification.tflite",
            context = this,
            classifierListener = this
        )
    }

    private fun startGallery() {
        // TODO: Mendapatkan gambar dari Gallery.
        launcherGallery.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()) {
        uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            showToast(getString(R.string.media_not_found))
        }
    }


    private fun showImage() {
        // TODO: Menampilkan gambar sesuai Gallery yang dipilih.
        currentImageUri?.let {
            mainBinding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage() {
        // TODO: Menganalisa gambar yang berhasil ditampilkan.
    }

    private fun moveToResult() {
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onImageError(error: String) {
        TODO("Not yet implemented")
    }

    override fun onImageResults(results: List<Classifications>?, inferenceTime: Long) {
        TODO("Not yet implemented")
    }
}