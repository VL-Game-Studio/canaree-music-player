package dev.olog.msc.musicservice.helper

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import dev.olog.msc.core.MediaId
import dev.olog.msc.core.MediaIdCategory
import dev.olog.msc.core.entity.track.*
import dev.olog.msc.core.interactor.GetSongListByParamUseCase
import dev.olog.msc.core.interactor.all.*
import dev.olog.msc.shared.extensions.mapToList
import io.reactivex.Single
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx2.asObservable
import javax.inject.Inject

internal class MediaItemGenerator @Inject constructor(
        private val getAllFoldersUseCase: GetAllFoldersUseCase,
        private val getAllPlaylistsUseCase: GetAllPlaylistsUseCase,
        private val getAllSongsUseCase: GetAllSongsUseCase,
        private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
        private val getAllArtistsUseCase: GetAllArtistsUseCase,
        private val getAllGenresUseCase: GetAllGenresUseCase,
        private val getSongListByParamUseCase: GetSongListByParamUseCase
) {


    fun getCategoryChilds(category: MediaIdCategory): Single<List<MediaBrowserCompat.MediaItem>> = runBlocking{
        when (category){
            MediaIdCategory.FOLDERS -> getAllFoldersUseCase.execute().asObservable().firstOrError()
                    .mapToList { it.toMediaItem() }
            MediaIdCategory.PLAYLISTS -> getAllPlaylistsUseCase.execute().asObservable().firstOrError()
                    .mapToList { it.toMediaItem() }
            MediaIdCategory.SONGS -> getAllSongsUseCase.execute().asObservable().firstOrError()
                    .mapToList { it.toMediaItem() }
            MediaIdCategory.ALBUMS -> getAllAlbumsUseCase.execute().asObservable().firstOrError()
                    .mapToList { it.toMediaItem() }
            MediaIdCategory.ARTISTS -> getAllArtistsUseCase.execute().asObservable().firstOrError()
                    .mapToList { it.toMediaItem() }
            MediaIdCategory.GENRES -> getAllGenresUseCase.execute().asObservable().firstOrError()
                    .mapToList { it.toMediaItem() }
            else -> Single.error(IllegalArgumentException("invalid category $category"))
        }
    }

    fun getCategoryValueChilds(parentId: MediaId): Single<MutableList<MediaBrowserCompat.MediaItem>>{
        return getSongListByParamUseCase.execute(parentId)
                .firstOrError()
                .mapToList { it.toChildMediaItem(parentId) }
                .map { it.toMutableList() }

    }

    private fun Folder.toMediaItem() : MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.folderId(this.path).toString())
                .setTitle(this.title)
                .build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }

    private fun Playlist.toMediaItem() : MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.playlistId(this.id).toString())
                .setTitle(this.title)
                .build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }

    private fun Song.toMediaItem() : MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.songId(this.id).toString())
                .setTitle(this.title)
                .setSubtitle(this.artist)
                .setDescription(this.album)
                .build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }

    private fun Song.toChildMediaItem(parentId: MediaId) : MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.playableItem(parentId, this.id).toString())
                .setTitle(this.title)
                .setSubtitle(this.artist)
                .setDescription(this.album)
                .build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
    }

    private fun Album.toMediaItem() : MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.albumId(this.id).toString())
                .setTitle(this.title)
                .build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }

    private fun Artist.toMediaItem() : MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.artistId(this.id).toString())
                .setTitle(this.name)
                .build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }

    private fun Genre.toMediaItem() : MediaBrowserCompat.MediaItem {
        val description = MediaDescriptionCompat.Builder()
                .setMediaId(MediaId.genreId(this.id).toString())
                .setTitle(this.name)
                .build()
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }

}