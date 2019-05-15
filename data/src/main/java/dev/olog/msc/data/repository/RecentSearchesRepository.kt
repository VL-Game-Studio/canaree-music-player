package dev.olog.msc.data.repository

import dev.olog.msc.core.entity.SearchResult
import dev.olog.msc.core.gateway.RecentSearchesGateway
import dev.olog.msc.core.gateway.podcast.PodcastAlbumGateway
import dev.olog.msc.core.gateway.podcast.PodcastArtistGateway
import dev.olog.msc.core.gateway.podcast.PodcastGateway
import dev.olog.msc.core.gateway.podcast.PodcastPlaylistGateway
import dev.olog.msc.core.gateway.track.*
import dev.olog.msc.data.db.AppDatabase
import dev.olog.msc.data.db.RecentSearchesDao
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.rx2.asObservable
import javax.inject.Inject

internal class RecentSearchesRepository @Inject constructor(
    appDatabase: AppDatabase,
    private val songGateway: SongGateway,
    private val albumGateway: AlbumGateway,
    private val artistGateway: ArtistGateway,
    private val playlistGateway: PlaylistGateway,
    private val genreGateway: GenreGateway,
    private val folderGateway: FolderGateway,

    private val podcastGateway: PodcastGateway,
    private val podcastPlaylistGateway: PodcastPlaylistGateway,
    private val podcastArtistGateway: PodcastArtistGateway,
    private val podcastAlbumGateway: PodcastAlbumGateway

) : RecentSearchesGateway {

    private val dao : RecentSearchesDao = appDatabase.recentSearchesDao()

    override fun getAll(): Observable<List<SearchResult>> = runBlocking{
        dao.getAll(songGateway.getAll().asObservable().firstOrError(),
                albumGateway.getAll().asObservable().firstOrError(),
                artistGateway.getAll().asObservable().firstOrError(),
                playlistGateway.getAll().asObservable().firstOrError(),
                genreGateway.getAll().asObservable().firstOrError(),
                folderGateway.getAll().asObservable().firstOrError(),
                podcastGateway.getAll().asObservable().firstOrError(),
                podcastPlaylistGateway.getAll().asObservable().firstOrError(),
                podcastAlbumGateway.getAll().asObservable().firstOrError(),
                podcastArtistGateway.getAll().asObservable().firstOrError()
        )
    }

    override fun insertSong(songId: Long): Completable = dao.insertSong(songId)
    override fun insertAlbum(albumId: Long): Completable = dao.insertAlbum(albumId)
    override fun insertArtist(artistId: Long): Completable = dao.insertArtist(artistId)
    override fun insertPlaylist(playlistId: Long): Completable = dao.insertPlaylist(playlistId)
    override fun insertGenre(genreId: Long): Completable = dao.insertGenre(genreId)
    override fun insertFolder(folderId: Long): Completable = dao.insertFolder(folderId)

    override fun insertPodcast(podcastId: Long): Completable = dao.insertPodcast(podcastId)
    override fun insertPodcastPlaylist(playlistid: Long): Completable = dao.insertPodcastPlaylist(playlistid)
    override fun insertPodcastAlbum(albumId: Long): Completable = dao.insertPodcastAlbum(albumId)
    override fun insertPodcastArtist(artistId: Long): Completable = dao.insertPodcastArtist(artistId)

    override fun deleteSong(itemId: Long): Completable = dao.deleteSong(itemId)
    override fun deleteAlbum(itemId: Long): Completable = dao.deleteAlbum(itemId)
    override fun deleteArtist(itemId: Long): Completable = dao.deleteArtist(itemId)
    override fun deletePlaylist(itemId: Long): Completable = dao.deletePlaylist(itemId)
    override fun deleteFolder(itemId: Long): Completable = dao.deleteFolder(itemId)
    override fun deleteGenre(itemId: Long): Completable = dao.deleteGenre(itemId)

    override fun deletePodcast(podcastId: Long): Completable = dao.deletePodcast(podcastId)
    override fun deletePodcastPlaylist(playlistId: Long): Completable = dao.deletePodcastPlaylist(playlistId)
    override fun deletePodcastAlbum(albumId: Long): Completable = dao.deletePodcastAlbum(albumId)
    override fun deletePodcastArtist(artistId: Long): Completable = dao.deletePodcastArtist(artistId)

    override fun deleteAll(): Completable = dao.deleteAll()
}