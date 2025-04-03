package app.echoirx.data.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.ParcelFileDescriptor
import android.system.Os
import android.util.Log
import app.echoirx.data.remote.api.ApiService
import com.kyant.taglib.Picture
import com.kyant.taglib.TagLib
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataManager @Inject constructor(
    private val apiService: ApiService
) {
    /**
     * Returns the cover art data if available
     */
    suspend fun getCoverArtData(metadata: Map<String, String>): ByteArray? {
        return try {
            metadata["COVER"]?.let { coverUrl ->
                val imageData = apiService.downloadFile(coverUrl)
                if (isValidImageData(imageData)) imageData else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading cover art", e)
            null
        }
    }
    companion object {
        private const val TAG = "MetadataManager"
    }

    suspend fun embedMetadata(filePath: String, metadata: Map<String, String>) {
        try {
            val file = File(filePath)

            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_WRITE).use { pfd ->
                // Write basic metadata
                val propertyMap = HashMap<String, Array<String>>().apply {
                    metadata.forEach { (key, value) ->
                        if (key != "COVER") {
                            put(key, arrayOf(value))
                        }
                    }
                }
                TagLib.savePropertyMap(pfd.dup().detachFd(), propertyMap)

                // Write cover art if available
                metadata["COVER"]?.let { coverUrl ->
                    val imageData = apiService.downloadFile(coverUrl)
                    if (isValidImageData(imageData)) {
                        val picture = createPicture(imageData)
                        TagLib.savePictures(pfd.dup().detachFd(), arrayOf(picture))
                    }
                }

                Os.close(pfd.fileDescriptor)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error embedding metadata", e)
            throw e
        }
    }

    private fun isValidImageData(data: ByteArray): Boolean =
        try {
            BitmapFactory.decodeByteArray(data, 0, data.size) != null
        } catch (e: Exception) {
            false
        }

    private fun createPicture(imageData: ByteArray): Picture =
        ByteArrayOutputStream().use { stream ->
            val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            Picture(
                data = stream.toByteArray(),
                description = "Front Cover",
                pictureType = "Front Cover",
                mimeType = "image/jpeg"
            )
        }
}