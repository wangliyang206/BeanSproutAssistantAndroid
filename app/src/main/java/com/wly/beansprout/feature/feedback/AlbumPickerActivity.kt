package com.wly.beansprout.feature.feedback

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import io.trtc.tuikit.atomicx.albumpicker.AlbumPickerConfig
import io.trtc.tuikit.atomicx.albumpicker.AlbumPickerListener
import io.trtc.tuikit.atomicx.albumpicker.AlbumPickerMediaFilter
import io.trtc.tuikit.atomicx.albumpicker.AlbumPickerStyle
import io.trtc.tuikit.atomicx.albumpicker.AlbumPickerTheme
import io.trtc.tuikit.atomicx.albumpicker.AlbumPickerView
import io.trtc.tuikit.atomicx.albumpicker.AlbumMedia
import io.trtc.tuikit.albumpickercore.api.AlbumPickerCoreLanguage
import io.trtc.tuikit.albumpickercore.api.CompressQuality

/**
 * 相册选择器 Activity
 * 托管 AlbumPickerView，通过 Intent 返回选中的媒体文件
 */
class AlbumPickerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val maxCount = intent.getIntExtra(EXTRA_MAX_COUNT, 9)

        val picker = AlbumPickerView(this).apply {
            val config = AlbumPickerConfig().apply {
                mediaFilter = AlbumPickerMediaFilter.ALL
                maxSelectionCount = maxCount
                itemsPerRow = 4
                showsCameraItem = true
                style = AlbumPickerStyle.LIKE_WECHAT
                language = AlbumPickerCoreLanguage.Language.ZH_HANS
                compressQuality = CompressQuality.HIGH
                maxVideoDurationInSeconds = 60
                maxOutputFileSizeInMB = 100
            }
            initialize(config, AlbumPickerTheme(), object : AlbumPickerListener {
                override fun onPickConfirm(medias: List<AlbumMedia>, extra: String?) {
                    val uris = ArrayList<Uri>(medias.size)
                    val isVideoFlags = ArrayList<Int>(medias.size)
                    medias.forEach { m ->
                        m.uri?.let { uri ->
                            uris.add(uri)
                            isVideoFlags.add(if (m.mediaType.name == "VIDEO") 1 else 0)
                        }
                    }
                    val resultIntent = Intent().apply {
                        putParcelableArrayListExtra(EXTRA_URIS, uris)
                        putIntegerArrayListExtra(EXTRA_IS_VIDEO, isVideoFlags)
                    }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                }

                override fun onMediaProcessing(media: AlbumMedia, progress: Float, isVideo: Boolean) {}
                override fun onMediaProcessed() {}
                override fun onCancel() {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            })
        }

        setContentView(picker, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
    }

    companion object {
        const val EXTRA_MAX_COUNT = "max_count"
        const val EXTRA_URIS = "uris"
        const val EXTRA_IS_VIDEO = "is_video"
    }
}
