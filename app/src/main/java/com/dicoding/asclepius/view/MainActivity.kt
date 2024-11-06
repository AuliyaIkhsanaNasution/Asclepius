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
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

class MainActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener  {
//    deklarasi variabel dan juga binding, kemudian class helper
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

//    mengambil gambar dari galeri
    private fun startGallery() {
        // TODO: Mendapatkan gambar dari Gallery.
//        pakai photo pick
        launcherGallery.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

//    munculkan gallery
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()) {
        uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
//            panggil fungsi crop
            imageCropLaunch(uri)
        } else {
            showToast(getString(R.string.media_not_found))
        }
    }

//    ini adalah fungsi untuk menangani crop gambar
    private fun imageCropLaunch(uri: Uri) {
        val destinationUri = Uri.fromFile(File(cacheDir, "croppedImage.jpg"))
        val options = UCrop.Options()
            .apply {
            setCompressionQuality(100)
        }
//    mengatur ratio dan juga maxresult
        UCrop.of(uri, destinationUri)
            .withAspectRatio(1f, 1f).withMaxResultSize(1080, 1080)
            .withOptions(options)
            .start(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        jika crop bisa maka jalankan
        if (resultCode == RESULT_OK) {
            if (requestCode == UCrop.REQUEST_CROP) {
                val resultUri = UCrop.getOutput(data!!)
                resultUri?.let {
                    currentImageUri = resultUri
//                    jika berhasil di crop maka panggil fungsi show image
                    showImage()
                }
            }
//            menangani jika crop gagal
        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            cropError?.printStackTrace()
            showToast(getString(R.string.crop_failed))
        }
    }

//tampilkan gambar di fungsi ini
    private fun showImage() {
        currentImageUri?.let { uri ->
            // Membuat URI baru dengan menambahkan query parameter unik
            val newUri = Uri.parse("$uri?${System.currentTimeMillis()}")
            mainBinding.previewImageView.setImageURI(newUri)
        } ?: run {
            showToast(getString(R.string.image_not_found)) // Jika URI tidak ada
        }
    }

//    analisa gambar disini
    private fun analyzeImage() {
        // TODO: Menganalisa gambar yang berhasil ditampilkan.

        if (currentImageUri != null) {
            mainBinding.progressIndicator.visibility = View.VISIBLE
            imageHelper.classifyStaticImage(currentImageUri!!)
        } else {
            showToast(getString(R.string.media_not_found))
        }
    }

    private fun showToast(message: String) {
        // TODO: Menampilkan Toast
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

//    hasil yang didapat kemudian dipindahkan ke halaman result
    private fun moveToResult(name: String, score: Float, inferenceTime: Long, imageUri: Uri?) {
        // TODO: Memindahkan ke ResultActivity.
        val intent = Intent(this, ResultActivity::class.java).apply {
//            ambil data kemudian kirimkan ke activity result
            putExtra("NAME", name)
            putExtra("SCORE", score)
            putExtra("INFERENCE_TIME", inferenceTime)
            putExtra("IMAGE_URI", imageUri.toString())
        }
        startActivity(intent)
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation)
    }

//    jika gambar error maka tampilkan toast
    override fun onImageError(error: String) {
        runOnUiThread {
        // binding untuk proses indikator loading
            mainBinding.progressIndicator.visibility = View.GONE
            showToast(error)
        }
    }

//    ini untuk hasil analisa dari gambar
    @SuppressLint("SuspiciousIndentation")
    override fun onImageResults(result: List<Classifications>?, inferenceTime: Long) {
        runOnUiThread {
            mainBinding.progressIndicator.visibility = View.GONE
//di klasifikasikan apakah cancer atau tidak cancer
            result?.let { classifications ->
                if (classifications.isNotEmpty() && classifications[0].categories.isNotEmpty()) {
                    val analize = classifications[0].categories[0]
                    moveToResult(analize.label, analize.score, inferenceTime, currentImageUri)

                } else {
                    showToast(getString(R.string.klasifikasi_not_found))
                }
            } ?: showToast("Hasil tidak ditemukan")
        }
    }
}