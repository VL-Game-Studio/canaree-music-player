package dev.olog.presentation.relatedartists

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dev.olog.core.MediaId
import dev.olog.core.entity.track.Artist
import dev.olog.core.entity.track.getMediaId
import dev.olog.core.interactor.GetItemTitleUseCase
import dev.olog.core.interactor.ObserveRelatedArtistsUseCase
import dev.olog.presentation.R
import dev.olog.presentation.model.DisplayableItem
import dev.olog.shared.extensions.asLiveData
import dev.olog.shared.extensions.mapToList
import dev.olog.shared.utils.TextUtils
import kotlinx.coroutines.rx2.asFlowable
import javax.inject.Inject

class RelatedArtistFragmentViewModel @Inject constructor(
    resources: Resources,
    mediaId: MediaId,
    useCase: ObserveRelatedArtistsUseCase,
    getItemTitleUseCase: GetItemTitleUseCase

) : ViewModel() {

    val itemOrdinal = mediaId.category.ordinal

    val data: LiveData<List<DisplayableItem>> = useCase(mediaId)
        .asFlowable().toObservable()
        .mapToList { it.toRelatedArtist(resources) }
        .asLiveData()

    val itemTitle = getItemTitleUseCase.execute(mediaId).asLiveData()

    private fun Artist.toRelatedArtist(resources: Resources): DisplayableItem {
        val songs =
            resources.getQuantityString(R.plurals.common_plurals_song, this.songs, this.songs)
        val albums = if (this.albums == 0) "" else {
            "${resources.getQuantityString(
                R.plurals.common_plurals_album,
                this.albums,
                this.albums
            )}${TextUtils.MIDDLE_DOT_SPACED}"
        }

        return DisplayableItem(
            R.layout.item_related_artist,
            getMediaId(),
            this.name,
            "$albums$songs"
        )
    }

}