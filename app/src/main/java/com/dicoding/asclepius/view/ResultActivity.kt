package com.dicoding.asclepius.view

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
//    binding activity result
    private lateinit var resultBinding: ActivityResultBinding

//    fungsi oncreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//    set layout result activity
        resultBinding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(resultBinding.root)

        // TODO: Menampilkan hasil gambar, prediksi, dan confidence score.
//    menerima data dari main activity ke result activity
        val label = intent.getStringExtra("NAME") ?: "Unknown"
        val score = intent.getFloatExtra("SCORE", 0f)
        val inferenceTime = intent.getLongExtra("INFERENCE_TIME", 0)
        val imageUriString = intent.getStringExtra("IMAGE_URI")

        resultAnalize(label, score, inferenceTime)
        imageAnalize(imageUriString)
    }

//ini adalah fungsi untuk menampilkan gambar hasil analisis
    private fun imageAnalize(imageUriString: String?) {
        imageUriString?.let { uriString ->
            val imageUri = Uri.parse(uriString)
            resultBinding.resultImage.setImageURI(imageUri)
        }
    }

//    fungsi untuk menampilkan hasil yang telah di analisis
    @SuppressLint("DefaultLocale")
    private fun resultAnalize(label: String, score: Float, inferenceTime: Long) {
        val resultText = """
            Prediksi Menghasilkan $label
            Dengan Inference Time $inferenceTime ms
            Score Prediksi ${String.format("%.2f%%", score * 100)}
        """.trimIndent()

        resultBinding.resultText.text = resultText
    }



}