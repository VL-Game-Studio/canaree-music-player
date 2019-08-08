package dev.olog.presentation.detail

import dev.olog.core.MediaId
import dev.olog.core.entity.AutoPlaylist
import dev.olog.core.entity.PlaylistType
import dev.olog.core.interactor.playlist.MoveItemInPlaylistUseCase
import dev.olog.core.interactor.playlist.RemoveFromPlaylistUseCase
import dev.olog.core.prefs.TutorialPreferenceGateway
import dev.olog.presentation.model.DisplayableTrack
import javax.inject.Inject

class DetailFragmentPresenter @Inject constructor(
    private val mediaId: MediaId,
    private val removeFromPlaylistUseCase: RemoveFromPlaylistUseCase,
    private val moveItemInPlaylistUseCase: MoveItemInPlaylistUseCase,
    private val tutorialPreferenceUseCase: TutorialPreferenceGateway

) {

    suspend fun removeFromPlaylist(item: DisplayableTrack) {
        mediaId.assertPlaylist()
        val playlistId = mediaId.categoryId
        val playlistType = if (item.mediaId.isPodcastPlaylist) PlaylistType.PODCAST else PlaylistType.TRACK
        if (playlistId == AutoPlaylist.FAVORITE.id){
            // favorites use songId instead of idInPlaylist
            removeFromPlaylistUseCase(
                RemoveFromPlaylistUseCase.Input(
                    playlistId, item.mediaId.leaf!!, playlistType
            ))
        } else {
            removeFromPlaylistUseCase(
                RemoveFromPlaylistUseCase.Input(
                playlistId, item.idInPlaylist.toLong(), playlistType
            ))
        }
    }

    fun moveInPlaylist(from: Int, to: Int){
        mediaId.assertPlaylist()
        val playlistId = mediaId.resolveId
        moveItemInPlaylistUseCase.execute(
            MoveItemInPlaylistUseCase.Input(playlistId, from, to,
                if (mediaId.isPodcastPlaylist) PlaylistType.PODCAST else PlaylistType.TRACK
        ))
    }

    fun showSortByTutorialIfNeverShown(): Boolean {
        return tutorialPreferenceUseCase.sortByTutorial()
    }

}