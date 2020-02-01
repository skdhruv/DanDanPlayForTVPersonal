package com.seiko.player.service

import android.content.Context
import android.net.Uri
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.MainThread
import com.seiko.common.util.extensions.lazyAndroid
import com.seiko.player.data.db.SlaveRepository
import com.seiko.player.util.AppScope
import com.seiko.player.util.VLCOptions
import com.seiko.player.util.getUri
import kotlinx.coroutines.*
import org.videolan.libvlc.FactoryManager
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.interfaces.ILibVLC
import org.videolan.libvlc.interfaces.IMedia
import org.videolan.libvlc.interfaces.IMediaFactory
import org.videolan.medialibrary.MLServiceLocator
import org.videolan.medialibrary.interfaces.Medialibrary
import org.videolan.medialibrary.interfaces.media.MediaWrapper
import timber.log.Timber
import java.util.*

class PlayListManager(
    private val context: Context,
    private val libVLC: ILibVLC,
    private val playerController: PlayerController,
    private val slaveRepository: SlaveRepository
) : IPlayerController by playerController {

    companion object {
        private const val TAG = "VLC/PlaylistManager"
    }

    private val mediaFactory by lazyAndroid {
        FactoryManager.getFactory(IMediaFactory.factoryId) as IMediaFactory
    }

    private var mediaList = MediaWrapperList()
    private var previous = Stack<Int>()

    private var newMedia = false

    var stopAfter = -1
    var savedTime = 0L

    var currentIndex = -1

    private val mediaPlayerEventListener = object : PlayerController.MediaPlayerEventListener {
        override fun onEvent(event: MediaPlayer.Event) {
            when(event.type) {
                MediaPlayer.Event.Playing -> {
                    Timber.d("MediaPlayer.Event.Playing")
                }
                MediaPlayer.Event.Paused -> {
                    Timber.d("MediaPlayer.Event.Paused")
                }
            }
        }
    }

    private val mediaListListener = object : MediaWrapperList.EventListener {
        override fun onItemAdded(index: Int, mrl: String) {

        }

        override fun onItemRemoved(index: Int, mrl: String) {

        }

        override fun onItemMoved(prevIndex: Int, nextIndex: Int, mrl: String) {

        }
    }

    init {
        AppScope.launch {
            playerController.init()
        }
    }

    fun hasMedia(): Boolean {
        return mediaList.size() != 0
    }

    fun stop(systemExit: Boolean = false) {
//        clearABRepeat()
        stopAfter = -1
//        videoBackground = false
        savePosition()
        mediaList.setEventListener(null)
        previous.clear()
        if (systemExit) {
            release()
        } else {
            restart()
        }
        mediaList.clear()
    }

    fun isValidPosition(position: Int): Boolean {
        return position in 0 until mediaList.size()
    }

    suspend fun load(list: List<MediaWrapper>, position: Int): Boolean {
        saveMediaList()
        savePosition()
        mediaList.setEventListener(null)
        previous.clear()
        mediaList.setItems(list)

        if (!hasMedia()) {
            Timber.tag(TAG).w("Warning: empty media list, nothing to play !")
            return false
        }

        mediaList.setEventListener(mediaListListener)
        stopAfter = -1
        setRate(1.0f, false)
        return playIndex(position)
    }

    private fun saveMediaList() {

    }

    private fun savePosition(reset: Boolean = false) {

    }

    private suspend fun playIndex(index: Int, flags: Int = 0): Boolean {

        if (mediaList.size() == 0) {
            Timber.tag(TAG).w("Warning: empty media list, nothing to play !")
            return false
        }
        currentIndex = if (isValidPosition(index)) {
            index
        } else {
            Timber.tag(TAG).w("Warning: index $index out of bounds")
            0
        }
        val media = mediaList.getMedia(index) ?: return false
        val isVideoPlaying =
            media.type == MediaWrapper.TYPE_VIDEO && playerController.isVideoPlaying()
        if (media.type != MediaWrapper.TYPE_VIDEO
            || isVideoPlaying
            || media.hasFlag(MediaWrapper.MEDIA_FORCE_AUDIO)
        ) {
            var uri = withContext(Dispatchers.IO) { getUri(context, media.uri) }
            if (uri == null) {
                skipMedia()
                return false
            }
            val title = media.getMetaLong(MediaWrapper.META_TITLE)
            if (title > 0) uri = Uri.parse("$uri#$title")
            val chapter = media.getMetaLong(MediaWrapper.META_CHAPTER)
            if (chapter > 0) uri = Uri.parse("$uri:$chapter")
            val start = getStartTime(media)
            val iMedia = mediaFactory.getFromUri(libVLC, uri)
            iMedia.addOption(":start-time=${start / 1000}")
//            VLCOptions.setMediaOptions(iMedia, context, flags or media.flags)
            playerController.startPlayback(iMedia, mediaPlayerEventListener, start)
            setSlaves(iMedia, media)
            newMedia = true
//            determinePrevAndNextIndices()
//            service.onNewPlayback()
            return true
        } else {
            if (playerController.isPlaying()) {
                playerController.stop()
            }
        }
        return false
    }

    private suspend fun setSlaves(media: IMedia, mw: MediaWrapper) {
        if (playerController.isReleased()) return
        val slaves = mw.slaves
        slaves?.forEach { slave ->
            media.addSlave(slave)
        }
        media.release()
        playerController.addSlaves(slaveRepository.getSlaves(mw.location)
            .filter { !slaves.contains(it) })
        if (slaves != null) {
            slaveRepository.saveSlaves(mw)
        }
    }

    private fun skipMedia() {

    }

    private suspend fun getStartTime(media: MediaWrapper): Long {
        val start = when {
            media.hasFlag(MediaWrapper.MEDIA_FROM_START) -> {
                media.removeFlags(MediaWrapper.MEDIA_FROM_START)
                0L
            }
            savedTime <= 0L -> when {
                media.time > 0L -> media.time
                media.type == MediaWrapper.TYPE_VIDEO
                        || media.isPodcast -> {
                    withContext(Dispatchers.IO) {
                        Medialibrary.getInstance().findMedia(media)
                            .getMetaLong(MediaWrapper.META_PROGRESS)
                    }
                }
                else -> 0L
            }
            else -> savedTime
        }
        savedTime = 0L
        return start
    }

    fun getCurrentMedia() = mediaList.getMedia(currentIndex)

}

private fun Array<IMedia.Slave>?.contains(item: IMedia.Slave) : Boolean {
    if (this == null) return false
    for (slave in this) if (slave.uri == item.uri) return true
    return false
}