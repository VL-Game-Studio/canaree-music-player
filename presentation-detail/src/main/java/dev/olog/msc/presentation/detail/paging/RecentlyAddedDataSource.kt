package dev.olog.msc.presentation.detail.paging

import androidx.lifecycle.Lifecycle
import androidx.paging.DataSource
import dev.olog.msc.core.MediaId
import dev.olog.msc.core.dagger.qualifier.FragmentLifecycle
import dev.olog.msc.core.entity.ChunkRequest
import dev.olog.msc.core.interactor.played.GetRecentlyAddedSongsUseCase
import dev.olog.msc.presentation.base.model.DisplayableItem
import dev.olog.msc.presentation.base.paging.BaseDataSource
import dev.olog.msc.presentation.detail.mapper.toRecentDetailDisplayableItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

internal class RecentlyAddedDataSource @Inject constructor(
    @FragmentLifecycle lifecycle: Lifecycle,
    private val recentlyAddedUseCase: GetRecentlyAddedSongsUseCase,
    private val mediaId: MediaId
) : BaseDataSource<DisplayableItem>() {

    private val chunked by lazy { recentlyAddedUseCase.getChunk(mediaId) }

    init {
        launch(Dispatchers.Main) { lifecycle.addObserver(this@RecentlyAddedDataSource) }
        launch {
            if (canLoadData) {
                chunked.observeChanges()
                    .take(1)
                    .collect {
                        invalidate()
                    }
            }
        }
    }

    override val canLoadData: Boolean
        get() = recentlyAddedUseCase.canShow(mediaId)

    override fun getMainDataSize(): Int {
        return chunked.allDataSize
    }

    override fun getHeaders(mainListSize: Int): List<DisplayableItem> = listOf()

    override fun getFooters(mainListSize: Int): List<DisplayableItem> = listOf()

    override fun loadInternal(chunkRequest: ChunkRequest): List<DisplayableItem> {
        return chunked.chunkOf(chunkRequest)
            .map { it.toRecentDetailDisplayableItem(mediaId) }
    }
}

internal class RecentlyAddedDataSourceFactory @Inject constructor(
    private val dataSource: Provider<RecentlyAddedDataSource>
) : DataSource.Factory<Int, DisplayableItem>() {

    override fun create(): DataSource<Int, DisplayableItem> {
        return dataSource.get()
    }
}