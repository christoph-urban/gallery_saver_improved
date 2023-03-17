package carnegietechnologies.gallery_saver

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class GallerySaverPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    private lateinit var channel: MethodChannel
    private var activity: Activity? = null
    private var gallerySaver: GallerySaver? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "gallery_saver")
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        if (android.os.Build.VERSION.SDK_INT < 29 && !isWritePermissionGranted()) {
            result.success(false)
            return
        }
        when (call.method) {
            "saveImage" -> gallerySaver?.saveFile(call, result, MediaType.image)
            "saveVideo" -> gallerySaver?.saveFile(call, result, MediaType.video)
            else -> result.notImplemented()
        }
    }

    private fun isWritePermissionGranted(): Boolean {
        activity?.let {
            return PackageManager.PERMISSION_GRANTED ==
                    ActivityCompat.checkSelfPermission(
                        it, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
        }
        return false
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        this.activity = binding.activity
        gallerySaver = GallerySaver(activity!!)
    }


    override fun onDetachedFromActivityForConfigChanges() {
        print("onDetachedFromActivityForConfigChanges")
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        print("onReattachedToActivityForConfigChanges")
    }

    override fun onDetachedFromActivity() {
        print("onDetachedFromActivity")
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
