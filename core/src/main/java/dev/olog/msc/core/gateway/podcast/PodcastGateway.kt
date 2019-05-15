package dev.olog.msc.core.gateway.podcast

import dev.olog.msc.core.entity.podcast.Podcast
import dev.olog.msc.core.gateway.base.BaseGateway
import io.reactivex.Completable
import io.reactivex.Observable

interface PodcastGateway : BaseGateway<Podcast, Long> {

    fun getAllUnfiltered(): Observable<List<Podcast>>

    fun getByAlbumId(albumId: Long): Observable<Podcast>

    fun deleteSingle(podcastId: Long): Completable

    fun deleteGroup(podcastList: List<Podcast>): Completable

    fun getUneditedByParam(podcastId: Long): Observable<Podcast>

    fun getCurrentPosition(podcastId: Long, duration: Long): Long
    fun saveCurrentPosition(podcastId: Long, position: Long)

}