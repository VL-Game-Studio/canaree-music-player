package dev.olog.msc.apilastfm.data

import com.github.dmstocking.optional.java.util.Optional
import dev.olog.msc.apilastfm.LastFmService
import dev.olog.msc.apilastfm.annotation.Proxy
import dev.olog.msc.apilastfm.mapper.LastFmNulls
import dev.olog.msc.apilastfm.mapper.toDomain
import dev.olog.msc.apilastfm.mapper.toDomainPodcast
import dev.olog.msc.apilastfm.mapper.toModel
import dev.olog.msc.core.entity.LastFmPodcast
import dev.olog.msc.core.entity.podcast.Podcast
import dev.olog.msc.core.gateway.podcast.PodcastGateway
import dev.olog.msc.data.db.AppDatabase
import dev.olog.msc.data.entity.LastFmPodcastEntity
import dev.olog.msc.shared.TrackUtils
import dev.olog.msc.shared.utils.TextUtils
import dev.olog.msc.shared.utils.assertBackgroundThread
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

internal class LastFmRepoPodcast @Inject constructor(
        appDatabase: AppDatabase,
        @Proxy private val lastFmService: LastFmService,
        private val gateway: PodcastGateway
) {

    private val dao = appDatabase.lastFmDao()

    fun shouldFetch(podcastId: Long): Single<Boolean> {
        return Single.fromCallable { dao.getPodcast(podcastId) == null }
    }

    fun getOriginalItem(podcastId: Long): Single<Podcast> = runBlocking{
        Single.just(gateway.getByParam(podcastId))
    }

    fun get(podcastId: Long): Single<Optional<LastFmPodcast?>> {
        val cachedValue = getFromCache(podcastId)

        val fetch = getOriginalItem(podcastId)
                .flatMap { fetch(it) }
                .map { Optional.of(it) }

        return cachedValue.onErrorResumeNext(fetch)
                .subscribeOn(Schedulers.io())
    }

    private fun getFromCache(trackId: Long): Single<Optional<LastFmPodcast?>> {
        return Single.fromCallable { Optional.ofNullable(dao.getPodcast(trackId)) }
                .map {
                    if (it.isPresent){
                        Optional.of(it.get()!!.toDomain())
                    } else throw NoSuchElementException()
                }
    }

    private fun fetch(podcast: Podcast): Single<LastFmPodcast> {
        assertBackgroundThread()

        val trackId = podcast.id

        val trackTitle = TextUtils.addSpacesToDash(podcast.title)
        val trackArtist = if (podcast.artist == TrackUtils.UNKNOWN) "" else podcast.artist

        return lastFmService.getTrackInfo(trackTitle, trackArtist)
                .map { it.toDomainPodcast(trackId) }
                .doOnSuccess { cache(it) }
                .onErrorResumeNext { lastFmService.searchTrack(trackTitle, trackArtist)
                        .map { it.toDomainPodcast(trackId) }
                        .flatMap { result -> lastFmService.getTrackInfo(result.title, result.artist)
                                .map { it.toDomainPodcast(trackId) }
                                .onErrorReturnItem(result)
                        }
                        .doOnSuccess { cache(it) }
                        .onErrorResumeNext {
                            if (it is NoSuchElementException){
                                Single.fromCallable { cacheEmpty(trackId) }
                                        .map { it.toDomain() }
                            } else Single.error(it)
                        }
                }
    }

    private fun cache(model: LastFmPodcast): LastFmPodcastEntity{
        val entity = model.toModel()
        dao.insertPodcast(entity)
        return entity
    }

    private fun cacheEmpty(podcastId: Long): LastFmPodcastEntity{
        val entity = LastFmNulls.createNullPodcast(podcastId)
        dao.insertPodcast(entity)
        return entity
    }

    fun delete(podcastId: Long){
        dao.deletePodcast(podcastId)
    }

}