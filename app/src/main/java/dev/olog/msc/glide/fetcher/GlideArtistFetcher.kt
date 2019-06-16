package dev.olog.msc.glide.fetcher

import android.content.Context
import com.bumptech.glide.Priority
import com.bumptech.glide.load.data.DataFetcher
import dev.olog.msc.core.MediaId
import dev.olog.msc.core.gateway.LastFmGateway
import kotlinx.coroutines.rx2.await
import java.io.InputStream

// for some reason last fm for some artists (maybe all) is returning a start instead of the artist image, this
// is the name of the image
private const val LAST_FM_PLACEHOLDER = "2a96cbd8b46e442fc41c2b86b821562f.png"

class GlideArtistFetcher(
    context: Context,
    mediaId: MediaId,
    private val lastFmGateway: LastFmGateway

) : BaseDataFetcher(context) {

    private val id = mediaId.resolveId

    override suspend fun execute(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>): String {
        val image = lastFmGateway.getArtist(id).await().get()!!.image
        if (image.endsWith(LAST_FM_PLACEHOLDER)) {
            return ""
        }
        return image
    }

    override suspend fun shouldFetch(): Boolean {
        return lastFmGateway.shouldFetchArtist(id).await()
    }

    override val threshold: Long = 250
}