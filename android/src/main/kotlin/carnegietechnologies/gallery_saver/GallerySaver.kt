package carnegietechnologies.gallery_saver

import android.app.Activity
import io.flutter.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.*

enum class MediaType { image, video }
/**
 * Class holding implementation of saving images and videos
 */
class GallerySaver internal constructor(private val activity: Activity) {

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    /**
     * Saves image or video to device
     *
     * @param methodCall - method call
     * @param result     - result to be set when saving operation finishes
     * @param mediaType    - media type
     */
    internal fun saveFile(
        methodCall: MethodCall,
        result: MethodChannel.Result,
        mediaType: MediaType
    ) {
        val filePath = methodCall.argument<Any>(KEY_PATH)?.toString() ?: ""
        val albumName = methodCall.argument<Any>(KEY_ALBUM_NAME)?.toString() ?: ""
        val toDcim = methodCall.argument<Any>(KEY_TO_DCIM) as Boolean
        Log.d("GallerySaver", "saveFile: path($filePath), album($albumName), toDcim($toDcim), mediaType($mediaType)")
        uiScope.launch {
            val successDeferred = async(Dispatchers.IO) {
                try {
                    if (mediaType == MediaType.video) {
                        FileUtils.insertVideo(activity.contentResolver, filePath, albumName, toDcim)
                    } else {
                        FileUtils.insertImage(activity.contentResolver, filePath, albumName, toDcim)
                    }
                } catch (e: Exception) {
                    Log.e("GallerySaver", "Exception while saving video")
                    uiScope.run { result.success(false) }
                }
            }
            val success = successDeferred.await()
            result.success(success)
        }
    }

    companion object {
        private const val KEY_PATH = "path"
        private const val KEY_ALBUM_NAME = "albumName"
        private const val KEY_TO_DCIM = "toDcim"
    }
}