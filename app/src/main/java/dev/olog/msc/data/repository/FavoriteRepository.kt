package dev.olog.msc.data.repository

import android.annotation.SuppressLint
import dev.olog.msc.data.db.AppDatabase
import dev.olog.msc.domain.entity.FavoriteEnum
import dev.olog.msc.domain.entity.FavoriteStateEntity
import dev.olog.msc.domain.entity.Song
import dev.olog.msc.domain.gateway.FavoriteGateway
import dev.olog.msc.domain.gateway.SongGateway
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import java.text.Collator
import javax.inject.Inject

class FavoriteRepository @Inject constructor(
    appDatabase: AppDatabase,
    private val songGateway: SongGateway,
    private val collator: Collator

) : FavoriteGateway {

    private val favoriteDao = appDatabase.favoriteDao()

    private val favoriteStatePublisher = BehaviorSubject.create<FavoriteStateEntity>()

    override fun observeToggleFavorite(): Observable<FavoriteEnum> = favoriteStatePublisher.map { it.enum }

    override fun updateFavoriteState(state: FavoriteStateEntity) {
        favoriteStatePublisher.onNext(state)
        if (state.enum == FavoriteEnum.ANIMATE_NOT_FAVORITE){
            favoriteStatePublisher.onNext(FavoriteStateEntity(state.songId, FavoriteEnum.NOT_FAVORITE))
        } else if (state.enum == FavoriteEnum.ANIMATE_TO_FAVORITE) {
            favoriteStatePublisher.onNext(FavoriteStateEntity(state.songId, FavoriteEnum.FAVORITE))
        }
    }

    override fun getAll(): Observable<List<Song>> {
        return favoriteDao.getAllImpl()
                .toObservable()
                .flatMap { favorites -> songGateway.getAll().map { songList ->
                    favorites.mapNotNull { favoriteId -> songList.firstOrNull { it.id == favoriteId } }
                            .sortedWith(Comparator { o1, o2 -> collator.compare(o1.title, o2.title) })
                } }
    }

    override fun addSingle(songId: Long): Completable {
        return favoriteDao.addToFavoriteSingle(songId)
                .andThen({
                    val id = favoriteStatePublisher.value?.songId ?: return@andThen
                    if (songId == id){
                        updateFavoriteState(FavoriteStateEntity(songId, FavoriteEnum.FAVORITE))
                    }
                    it.onComplete()
                })
    }

    override fun addGroup(songListId: List<Long>): Completable {
        return favoriteDao.addToFavorite(songListId)
                .andThen({
                    val songId = favoriteStatePublisher.value?.songId ?: return@andThen
                    if (songListId.contains(songId)){
                        updateFavoriteState(FavoriteStateEntity(songId, FavoriteEnum.FAVORITE))
                    }
                    it.onComplete()
                })
    }

    override fun deleteSingle(songId: Long): Completable {
        return favoriteDao.removeFromFavorite(listOf(songId))
                .andThen({
                    val id = favoriteStatePublisher.value?.songId ?: return@andThen
                    if (songId == id){
                        updateFavoriteState(FavoriteStateEntity(songId, FavoriteEnum.NOT_FAVORITE))
                    }
                    it.onComplete()
                })
    }

    override fun deleteGroup(songListId: List<Long>): Completable {
        return favoriteDao.removeFromFavorite(songListId)
                .andThen({
                    val songId = favoriteStatePublisher.value?.songId ?: return@andThen
                    if (songListId.contains(songId)){
                        updateFavoriteState(FavoriteStateEntity(songId, FavoriteEnum.NOT_FAVORITE))
                    }
                    it.onComplete()
                })
    }

    override fun deleteAll(): Completable {
        return Completable.fromCallable { favoriteDao.deleteAll() }
                .andThen({
                    val songId = favoriteStatePublisher.value?.songId ?: return@andThen
                    updateFavoriteState(FavoriteStateEntity(songId, FavoriteEnum.NOT_FAVORITE))
                    it.onComplete()
                })
    }

    override fun isFavorite(songId: Long): Single<Boolean> {
        return Single.fromCallable { favoriteDao.isFavorite(songId) != null }
    }

    // leaks for very small amount of time
    @SuppressLint("RxLeakedSubscription")
    override fun toggleFavorite() {
        val value = favoriteStatePublisher.value ?: return
        val id = value.songId
        val state = value.enum

        var action : Completable? = null

        when (state) {
            FavoriteEnum.NOT_FAVORITE -> {
                updateFavoriteState(FavoriteStateEntity(id, FavoriteEnum.ANIMATE_TO_FAVORITE))
                action = favoriteDao.addToFavoriteSingle(id)
            }
            FavoriteEnum.FAVORITE -> {
                updateFavoriteState(FavoriteStateEntity(id, FavoriteEnum.ANIMATE_NOT_FAVORITE))
                action = favoriteDao.removeFromFavorite(listOf(id))
            }
            else -> Completable.complete()
        }

        action?.subscribeOn(Schedulers.io())
                ?.subscribe({}, Throwable::printStackTrace)
    }
}