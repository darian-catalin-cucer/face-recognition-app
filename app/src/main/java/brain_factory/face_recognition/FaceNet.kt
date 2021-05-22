package brain_factory.face_recognition

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.channels.FileChannel


class FaceNet(assetManager: AssetManager)
{

    private val MODEL_FILE = "facenet.tflite"

    private val EMBEDDING_SIZE = 512

    private val INPUT_SIZE = 160

    private val BYTE_SIZE_OF_FLOAT = 4

    private var intValues: IntArray
    private var rgbValues: FloatArray

    private var inputBuffer: FloatBuffer
    private var outputBuffer: FloatBuffer

    private var bitmap: Bitmap

    private var interpreter: Interpreter


    @Throws(IOException::class)
    private fun loadModelFile(assets: AssetManager): ByteBuffer?
    {
        val fileDescriptor = assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    init
    {
        try
        {
            interpreter = Interpreter(loadModelFile(assetManager)!!)
        }
        catch (exp: Exception)
        {
            throw RuntimeException(exp)
        }

        // Pre-allocate buffers.
        intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        rgbValues = FloatArray(INPUT_SIZE * INPUT_SIZE * 3)
        inputBuffer = ByteBuffer.allocateDirect(INPUT_SIZE * INPUT_SIZE * 3 * BYTE_SIZE_OF_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer()
        outputBuffer = ByteBuffer.allocateDirect(EMBEDDING_SIZE * BYTE_SIZE_OF_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer()
        bitmap = Bitmap.createBitmap(INPUT_SIZE, INPUT_SIZE, Bitmap.Config.ARGB_8888)
    }

    fun getEmbeddings(originalBitmap: Bitmap, rect: Rect): FloatBuffer
    {
        val canvas = Canvas(bitmap)
        canvas.drawBitmap(originalBitmap, rect, Rect(0, 0, INPUT_SIZE, INPUT_SIZE), null)
        bitmap.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);
        for (i in 0 until INPUT_SIZE * INPUT_SIZE)
        {
            val pixelValue = intValues[i]
            rgbValues[i * 3 + 2] = (pixelValue and 0xFF).toFloat()
            rgbValues[i * 3 + 1] = (pixelValue shr 8 and 0xFF).toFloat()
            rgbValues[i * 3 + 0] = (pixelValue shr 16 and 0xFF).toFloat()
        }
        ImageUtils.prewhiten(rgbValues, inputBuffer)
        outputBuffer.rewind()
        interpreter.run(inputBuffer, outputBuffer)
        outputBuffer.flip()
        return outputBuffer
    }

    fun close()
    {
        interpreter.close()
    }

}