package com.sarajane.spinpoker10.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Gestion des sons synthétiques via SoundPool. Les WAV sont générés par
 * tools/generate_sounds.py et placés dans res/raw.
 */
class SoundManager(context: Context) {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids: Map<String, Int> = NAMES.associateWith { name ->
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        if (resId != 0) pool.load(context, resId, 1) else 0
    }

    fun play(name: String) {
        val id = ids[name] ?: return
        if (id != 0) pool.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun release() = pool.release()

    companion object {
        private val NAMES = listOf(
            "click", "deal", "reel_stop", "hold", "coin", "win", "bigwin", "lose"
        )
    }
}
