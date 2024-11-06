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
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import com.dicoding.viewmodel.MainViewModel
import com.yalantis.ucrop.UCrop
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.io.File

class MainActivity : AppCompatActivity(), ImageClassifierHelper.ClassifierListener  {
//    deklarasi variabel dan juga binding, kemudian class helper
    private lateinit var imageHelper: ImageClassifierHelper
    // Mengakses ViewModel
    private var originalImageUri: Uri? = null
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var mainBinding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        ini adalah binding untuk activity_main.xml
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)
//        melakukan binding button
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

        // Observasi URI gambar di ViewModel
        mainViewModel.currentImageUri.observe(this, Observer { uri ->
            uri?.let {
                // Panggil fungsi untuk menampilkan gambar
                showImage(uri)
            }
        })
    }

//    mengambil gambar dari galeri
    private fun startGallery() {
        // TODO: Mendapatkan gambar dari Gallery.
//        pakai photo pick
        launcherGallery.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }
    // Launcher untuk memunculkan galeri
    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            // Simpan URI ke dalam ViewModel menggunakan fungsi yang sudah dibuat
            mainViewModel.setCurrentImageUri(uri)
            // Panggil fungsi crop
            imageCropLaunch(uri)
        } else {
            showToast(getString(R.string.media_not_found))
        }
    }


//    ini adalah fungsi untuk menangani crop gambar
    private fun imageCropLaunch(uri: Uri) {
        originalImageUri = uri

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
//    melakukan proses crop, jika berhasil,cancel dan gagal
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UCrop.REQUEST_CROP) {
            when (resultCode) {
                RESULT_OK -> {
                    // Jika cropping berhasil, gunakan gambar yang dicrop
                    val resultUri = UCrop.getOutput(data!!)
                    resultUri?.let {
                        mainViewModel.setCurrentImageUri(resultUri) // Gunakan fungsi setter
                        // Menampilkan gambar hasil cropping
                        showImage(resultUri) // Tampilkan gambar yang sudah dicrop
                    }
                }
                RESULT_CANCELED -> {
                    // Jika cropping dibatalkan, gunakan gambar asli
                    mainViewModel.setCurrentImageUri(originalImageUri) // Gunakan fungsi setter
                    // Menampilkan pesan cropping dibatalkan
                    showToast(getString(R.string.crop_canceled))
                    showImage(originalImageUri) // Tampilkan gambar asli
                }
                UCrop.RESULT_ERROR -> {
                    // Jika terjadi kesalahan saat cropping
                    val cropError = UCrop.getError(data!!)
                    cropError?.printStackTrace()
                    showToast(getString(R.string.crop_failed))
                }
            }
        }
    }


    //tampilkan gambar di fungsi ini
    private fun showImage(uri: Uri?) {
        mainBinding.previewImageView.setImageURI(uri)
    }

    private fun showToast(message: String) {
        // TODO: Menampilkan Toast
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
//analisis gambar
    private fun analyzeImage() {
        val uri = mainViewModel.currentImageUri.value
        if (uri != null) {
            // Gambar ditemukan, tampilkan progress dan analisis gambar
            mainBinding.progressIndicator.visibility = View.VISIBLE
            imageHelper.classifyStaticImage(uri)
        } else {
            // Gambar tidak ditemukan
            showToast(getString(R.string.media_not_found))
        }
    }


    //    hasil yang didapat kemudian dipindahkan ke halaman result
    private fun moveToResult(name: String, score: Float, inferenceTime: Long) {
        // TODO: Memindahkan ke ResultActivity.
        mainViewModel.currentImageUri.value?.let { imageUri ->

            val intent = Intent(this, ResultActivity::class.java)
//            ambil data kemudian kirimkan ke activity result
                intent.putExtra("NAME", name)
                intent.putExtra("SCORE", score)
                intent.putExtra("INFERENCE_TIME", inferenceTime)
                intent.putExtra("IMAGE_URI", imageUri.toString())  // Mengirim URI sebagai String
            //    mulai berpindah dengan animation
            startActivity(intent)
            @Suppress("DEPRECATION")
            overridePendingTransition(R.anim.enter_animation, R.anim.exit_animation)
        }

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
                    moveToResult(analize.label, analize.score, inferenceTime)

                } else {
                    showToast(getString(R.string.klasifikasi_not_found))
                }
            } ?: showToast("Hasil tidak ditemukan")
        }
    }
}