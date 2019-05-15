package dev.olog.msc.presentation.offlinelyrics

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import dev.olog.msc.shared.ui.extensions.colorAccent
import dev.olog.msc.shared.ui.extensions.windowBackground
import dev.olog.msc.taptargetview.TapTarget
import dev.olog.msc.taptargetview.TapTargetSequence

object Tutorial {
    fun addLyrics(search: View, edit: View, sync: View) {
        val context = search.context

        val searchTarget = TapTarget.forView(search, context.getString(R.string.tutorial_search_lyrics))
                .tint(context)
                .icon(ContextCompat.getDrawable(context, R.drawable.vd_search))

        val editTarget = TapTarget.forView(edit, context.getString(R.string.tutorial_add_lyrics))
                .tint(context)
                .icon(ContextCompat.getDrawable(context, R.drawable.vd_edit))

        val syncLyrics = TapTarget.forView(sync, context.getString(R.string.tutorial_adjust_sync))
                .tint(context)
                .icon(ContextCompat.getDrawable(context, R.drawable.vd_sync))

        TapTargetSequence(search.context as Activity)
                .targets(editTarget, searchTarget, syncLyrics)
                .start()
    }

    private fun TapTarget.tint(context: Context): TapTarget {
        val accentColor = context.colorAccent()
        val backgroundColor = context.windowBackground()

        return this.tintTarget(true)
                .outerCircleColorInt(accentColor)
                .targetCircleColorInt(backgroundColor)
    }
}