package com.dicoding.asclepius.helper

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import com.dicoding.asclepius.R
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.IOException


class ImageClassifierHelper(
    private var threshold: Float = 0.1f,
    private var maxResults: Int = 3,
    private val modelName: String = "cancer_classification.tflite",
    private val context: Context,
    private val classifierListener: ClassifierListener?) {

    interface ClassifierListener {
        fun onError(error: String)
        fun onResults(
            results: List<Classifications>?,
            inferenceTime: Long
        )
    }

    private var imageClassifier: ImageClassifier? = null

    companion object {
        private const val TAG = "ImageClassifierHelper"
    }

//    ini untuk menginisialisasi ImageClassifier
    init {
        setupImageClassifier()
    }

    private fun setupImageClassifier() {
        // TODO: Menyiapkan Image Classifier untuk memproses gambar.
        val optionsBuilder = ImageClassifier
            .ImageClassifierOptions
            .builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)
        val baseOptionsBuilder = BaseOptions.builder().setNumThreads(4)
        optionsBuilder.setBaseOptions(baseOptionsBuilder.build())

        try {
//            untuk menangani error atau situasi yang tidak diinginkan
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context, modelName,
                optionsBuilder.build()
            )

        }catch (e: IllegalStateException) {
            classifierListener?.onError(context.getString(R.string.failed_image))
            Log.e(TAG, e.message.toString())
        }

    }

    fun classifyStaticImage(imageUri: Uri) {
        // TODO: mengklasifikasikan imageUri dari gambar statis.

        if (imageClassifier == null) {
            setupImageClassifier()
        }

        try {
            @Suppress("DEPRECATION")
//            lkonversi gambar statis ke bitmap
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            val imageProcessor = ImageProcessor.Builder()
                .add(ResizeOp(224, 224, ResizeOp
                    .ResizeMethod
                    .NEAREST_NEIGHBOR)).build()

            var inferenceTime = SystemClock.uptimeMillis()
            inferenceTime = SystemClock.uptimeMillis() - inferenceTime
            val tensorImage = imageProcessor.process(TensorImage.fromBitmap(bitmap))
            val results = imageClassifier?.classify(tensorImage)
            classifierListener?.onResults(results, inferenceTime)


        } catch (e: IOException) {
            classifierListener?.onError("gagal untuk memproses gambar sesuai code ${e.message}")
            Log.e(TAG, "gagal untuk memproses gambar", e)
        }

    }

}