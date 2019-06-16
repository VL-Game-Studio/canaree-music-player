package dev.olog.msc.glide.fetcher

import android.content.Context
import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import dev.olog.msc.core.MediaId
import dev.olog.msc.core.gateway.LastFmGateway
import kotlinx.coroutines.rx2.await
import java.io.InputStream

class GlideAlbumFetcher(
    context: Context,
    mediaId: MediaId,
    private val lastFmGateway: LastFmGateway

) : BaseDataFetcher(context) {

    private val id = mediaId.resolveId

    override suspend fun execute(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>): String {
        return lastFmGateway.getAlbum(id).await().get()!!.image
    }


    override suspend fun shouldFetch(): Boolean {
        return lastFmGateway.shouldFetchAlbum(id).await()
    }

    override val threshold: Long = 600
}