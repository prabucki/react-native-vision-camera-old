package com.mrousavy.old.camera.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.exifinterface.media.ExifInterface
import com.mrousavy.old.camera.CameraViewOld
import com.mrousavy.old.camera.InvalidFormatError
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.BufferedOutputStream
import java.nio.ByteBuffer
import kotlin.system.measureTimeMillis

// Optimized flip function with better memory management
private fun flipImageOptimized(imageBytes: ByteArray): ByteArray {
  return try {
    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    val matrix = Matrix().apply { preScale(-1f, 1f) }
    val flippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)

    val outputStream = ByteArrayOutputStream()
    flippedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
    val result = outputStream.toByteArray()

    // Clean up bitmaps
    bitmap.recycle()
    flippedBitmap.recycle()
    outputStream.close()

    result
  } catch (e: Exception) {
    Log.e(CameraViewOld.TAG, "Error flipping image: ${e.message}", e)
    imageBytes // Return original if flip fails
  }
}

// Legacy flip function (kept for compatibility but marked as deprecated)
@Deprecated("Use flipImageOptimized instead", ReplaceWith("flipImageOptimized(imageBytes)"))
fun flip(imageBytes: ByteArray, imageWidth: Int): ByteArray {
  // separate out the sub arrays
  var holder = ByteArray(imageBytes.size)
  var subArray = ByteArray(imageWidth)
  var subCount = 0
  for (i in imageBytes.indices) {
    subArray[subCount] = imageBytes[i]
    subCount++
    if (i % imageWidth == 0) {
      subArray.reverse()
      if (i == imageWidth) {
        holder = subArray
      } else {
        holder += subArray
      }
      subCount = 0
      subArray = ByteArray(imageWidth)
    }
  }
  subArray = ByteArray(imageWidth)
  System.arraycopy(imageBytes, imageBytes.size - imageWidth, subArray, 0, subArray.size)
  return holder + subArray
}

// Optimized flip function that directly manipulates the byte array for JPEG
private fun flipImage(imageBytes: ByteArray): ByteArray {
  return flipImageOptimized(imageBytes)
}

// Optimized save function with better memory management and performance
fun ImageProxy.save(file: File, flipHorizontally: Boolean) {
  val startTime = System.currentTimeMillis()

  try {
    when (format) {
      ImageFormat.JPEG -> {
        saveJpegImage(file, flipHorizontally)
      }
      ImageFormat.YUV_420_888 -> {
        saveYuvImage(file)
      }
      else -> throw InvalidFormatError(format)
    }

    val duration = System.currentTimeMillis() - startTime
    Log.d(CameraViewOld.TAG_PERF, "Image saved in ${duration}ms (format: $format, flipped: $flipHorizontally)")

  } catch (e: Exception) {
    Log.e(CameraViewOld.TAG, "Error saving image: ${e.message}", e)
    throw e
  }
}

private fun ImageProxy.saveJpegImage(file: File, flipHorizontally: Boolean) {
  val buffer = planes[0].buffer
  val bytes = ByteArray(buffer.remaining())
  buffer.get(bytes)

  val finalBytes = if (flipHorizontally) {
    val flipTime = measureTimeMillis {
      flipImage(bytes)
    }
    Log.d(CameraViewOld.TAG_PERF, "Image flipping took ${flipTime}ms")
    flipImage(bytes)
  } else {
    bytes
  }

  // Use buffered output stream for better performance
  BufferedOutputStream(FileOutputStream(file), 8192).use { output ->
    output.write(finalBytes)
    output.flush()
  }
}

private fun ImageProxy.saveYuvImage(file: File) {
  // Pre-allocate buffer for metadata
  val metadataBuffer = ByteBuffer.allocate(16)
  metadataBuffer.putInt(width)
    .putInt(height)
    .putInt(planes[1].pixelStride)
    .putInt(planes[1].rowStride)

  BufferedOutputStream(FileOutputStream(file), 8192).use { output ->
    // Write metadata
    output.write(metadataBuffer.array())

    // Write plane data efficiently
    for (i in 0..2) {
      val buffer = planes[i].buffer
      val bytes = ByteArray(buffer.remaining())
      buffer.get(bytes)
      output.write(bytes)
    }
    output.flush()
  }
}
