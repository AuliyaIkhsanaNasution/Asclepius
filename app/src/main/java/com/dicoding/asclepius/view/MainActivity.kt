package com.dicoding.asclepius.view

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.util.Locale

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
            threshold = 0.5f,
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

        if (currentImageUri != null) {
            mainBinding.progressIndicator.visibility = View.VISIBLE

            imageHelper.classifyStaticImage(currentImageUri!!)

        } else {
            showToast(getString(R.string.media_not_found))
        }
    }

    private fun moveToResult(label: String, score: Float, inferenceTime: Long, imageUri: Uri?) {
        // TODO: Memindahkan ke ResultActivity.
        val intent = Intent(this, ResultActivity::class.java).apply {
            putExtra("LABEL", label)
            putExtra("SCORE", score)
            putExtra("INFERENCE_TIME", inferenceTime)
            putExtra("IMAGE_URI", imageUri.toString())
        }
        startActivity(intent)
    }

    private fun showToast(message: String) {
        // TODO: Menampilkan Toast
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onImageError(error: String) {
        runOnUiThread {

            mainBinding.progressIndicator.visibility = View.GONE
            showToast(error)
        }
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onImageResults(results: List<Classifications>?, inferenceTime: Long) {
        runOnUiThread {
            mainBinding.progressIndicator.visibility = View.GONE

            results?.let { classifications ->
                if (classifications.isNotEmpty() && classifications[0].categories.isNotEmpty()) {
                    val category = classifications[0].categories[0]

                        if (category.label.lowercase(Locale.getDefault())== "cancer" && category.score >= 0.5) {
                        moveToResult(category.label, category.score, inferenceTime, currentImageUri)

                    } else {
                            showToast(getString(R.string.analize_not_found))
                    }
                } else {
                    showToast(getString(R.string.klasifikasi_not_found))
                }
            } ?: showToast("No results")
        }
    }
}