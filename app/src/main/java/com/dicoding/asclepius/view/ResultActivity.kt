package com.dicoding.asclepius.view

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.dicoding.asclepius.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var resultBinding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resultBinding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(resultBinding.root)

        // TODO: Menampilkan hasil gambar, prediksi, dan confidence score.
        val label = intent.getStringExtra("NAME") ?: "Unknown"
        val score = intent.getFloatExtra("SCORE", 0f)
        val inferenceTime = intent.getLongExtra("INFERENCE_TIME", 0)
        val imageUriString = intent.getStringExtra("IMAGE_URI")

        displayResults(label, score, inferenceTime)
        displayImage(imageUriString)
    }

    @SuppressLint("DefaultLocale")
    private fun displayResults(label: String, score: Float, inferenceTime: Long) {
        val resultText = """
            Prediksi Menghasilkan $label
            Dengan Inference Time $inferenceTime ms
            Score Prediksi ${String.format("%.2f%%", score * 100)}
        """.trimIndent()

        resultBinding.resultText.text = resultText
    }

    private fun displayImage(imageUriString: String?) {
        imageUriString?.let { uriString ->
            val imageUri = Uri.parse(uriString)
            resultBinding.resultImage.setImageURI(imageUri)
        }
    }


}