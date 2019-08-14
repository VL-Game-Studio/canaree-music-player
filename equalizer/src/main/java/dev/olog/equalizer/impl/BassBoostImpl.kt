package dev.olog.equalizer.impl

import android.media.audiofx.BassBoost
import dev.olog.core.prefs.EqualizerPreferencesGateway
import dev.olog.equalizer.IBassBoost
import javax.inject.Inject

class BassBoostImpl @Inject constructor(
    private val equalizerPrefsUseCase: EqualizerPreferencesGateway

) : IBassBoost {

    private var bassBoost: BassBoost? = null

    override fun getStrength(): Int {
        try {
            return bassBoost?.roundedStrength?.toInt() ?: 0
        } catch (ex: IllegalStateException){
            ex.printStackTrace()
            // throws getParameter() called on uninitialized AudioEffect.
            return 0
        }
    }

    override fun setStrength(value: Int) {
        bassBoost?.setStrength(value.toShort())?.also {
            val currentProperties = bassBoost?.properties?.toString()
            if (!currentProperties.isNullOrBlank()){
                equalizerPrefsUseCase.saveBassBoostSettings(currentProperties)
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        bassBoost?.enabled = enabled
    }

    override fun onAudioSessionIdChanged(audioSessionId: Int) {
        bassBoost?.release()

        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = equalizerPrefsUseCase.isEqualizerEnabled()
                val lastProperties = equalizerPrefsUseCase.getBassBoostSettings()
                if (lastProperties.isNotBlank()) {
                    properties = BassBoost.Settings(lastProperties)
                }
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }

    override fun onDestroy() {
        bassBoost?.release()
    }

}