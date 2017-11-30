package dev.olog.presentation.fragment_mini_player

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import dev.olog.presentation.fragment_mini_player.model.MiniPlayerMedatata
import dev.olog.presentation.fragment_mini_player.model.toMiniPlayerMetadata
import dev.olog.presentation.music_service.RxMusicServiceControllerCallback
import dev.olog.presentation.utils.extension.asLiveData

class MiniPlayerFragmentViewModel(
        private val controllerCallback: RxMusicServiceControllerCallback

) : ViewModel() {


    val onMetadataChangedLiveData: LiveData<MiniPlayerMedatata> = controllerCallback
            .onMetadataChanged()
                .map { it.toMiniPlayerMetadata() }
                .asLiveData()

    val animatePlayPauseLiveData: LiveData<Int> = controllerCallback
            .onPlaybackStateChanged()
            .map { it.state }
            .filter { state -> state == PlaybackStateCompat.STATE_PLAYING || state == PlaybackStateCompat.STATE_PAUSED }
            .distinctUntilChanged()
            .skip(1)
            .asLiveData()

    val animateSkipToLiveData: LiveData<Boolean> = controllerCallback.onPlaybackStateChanged()
            .map { it.state }
            .filter { state -> state == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT || state == PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS }
            .map { state -> state == PlaybackStateCompat.STATE_SKIPPING_TO_NEXT }
            .asLiveData()

    val onBookmarkChangedLiveData: LiveData<Long> = controllerCallback.onPlaybackStateChanged()
                .filter { playbackState ->
                    val state = playbackState.state
                    state == PlaybackStateCompat.STATE_PAUSED || state == PlaybackStateCompat.STATE_PLAYING
                }.map { it.position }
                .asLiveData()

    val onMaxChangedLiveData: LiveData<Long> = controllerCallback
            .onMetadataChanged()
            .map { metadata -> metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) }
            .asLiveData()

    val handleProgressBarLiveData: LiveData<Boolean> = controllerCallback.onPlaybackStateChanged()
            .map { it.state }
            .filter { state -> state == PlaybackStateCompat.STATE_PAUSED || state == PlaybackStateCompat.STATE_PLAYING }
            .map { state -> state == PlaybackStateCompat.STATE_PLAYING }
            .distinctUntilChanged()
            .asLiveData()

}