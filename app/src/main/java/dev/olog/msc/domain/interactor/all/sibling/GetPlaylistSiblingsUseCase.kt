package dev.olog.msc.domain.interactor.all.sibling

import dev.olog.msc.core.MediaId
import dev.olog.msc.core.entity.AutoPlaylistType
import dev.olog.msc.core.entity.Playlist
import dev.olog.msc.core.executor.IoScheduler
import dev.olog.msc.core.gateway.PlaylistGateway
import dev.olog.msc.core.interactor.base.ObservableUseCaseWithParam
import io.reactivex.Observable
import javax.inject.Inject

class GetPlaylistSiblingsUseCase @Inject internal constructor(
    schedulers: IoScheduler,
    private val gateway: PlaylistGateway

) : ObservableUseCaseWithParam<List<Playlist>, MediaId>(schedulers) {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun buildUseCaseObservable(mediaId: MediaId): Observable<List<Playlist>> {
        val playlistId = mediaId.categoryValue.toLong()

        val observable = if (AutoPlaylistType.isAutoPlaylist(playlistId)){
            gateway.getAllAutoPlaylists()
        } else gateway.getAll()

        return observable.map { playlists ->
            playlists.asSequence()
                    .filter { it.id != playlistId } // remove itself
                    .filter { it.size > 0 } // remove empty list
                    .toList()
        }
    }
}