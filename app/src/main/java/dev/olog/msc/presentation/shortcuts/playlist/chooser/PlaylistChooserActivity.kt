package dev.olog.msc.presentation.shortcuts.playlist.chooser

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import dev.olog.msc.R
import dev.olog.msc.core.AppShortcuts
import dev.olog.msc.presentation.base.activity.BaseActivity
import dev.olog.msc.presentation.base.extensions.asLiveData
import dev.olog.msc.presentation.base.extensions.subscribe
import dev.olog.msc.presentation.base.extensions.viewModelProvider
import dev.olog.msc.shared.extensions.isPortrait
import dev.olog.msc.shared.extensions.lazyFast
import kotlinx.android.synthetic.main.activity_playlist_chooser.*
import javax.inject.Inject

class PlaylistChooserActivity : BaseActivity() {

    @Inject lateinit var factory: ViewModelProvider.Factory
    @Inject lateinit var appShortcuts: AppShortcuts

    private val viewModel by lazyFast { viewModelProvider<PlaylistChooserActivityViewModel>(factory) }
    private val adapter by lazyFast { PlaylistChooserActivityAdapter(this, appShortcuts) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_chooser)

        viewModel.observeData()
                .subscribe(this, adapter::updateDataSet)

        list.adapter = adapter
        list.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, if (isPortrait) 2 else 3)
    }

    override fun onResume() {
        super.onResume()
        back.setOnClickListener { finish() }
    }

    override fun onPause() {
        super.onPause()
        back.setOnClickListener(null)
    }

}