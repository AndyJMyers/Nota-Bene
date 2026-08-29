package com.notabene.app

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

internal enum class AmbientTone(val label: String, val resource: Int) {
    WIND("WIND", R.raw.ambient_wind),
    FOUNTAIN("FOUNTAIN", R.raw.ambient_fountain),
    NATURE("NATURE", R.raw.ambient_nature),
    CHIMES("CHIMES", R.raw.ambient_chimes)
}

internal fun ambientToneForMood(mood: Float): AmbientTone = when {
    mood < .25f -> AmbientTone.WIND
    mood < .50f -> AmbientTone.FOUNTAIN
    mood < .75f -> AmbientTone.NATURE
    else -> AmbientTone.CHIMES
}

@Composable
internal fun AmbientSoundscape(mood: Float, volume: Float) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember { AmbientSoundController(context.applicationContext) }
    val tone = ambientToneForMood(mood)

    LaunchedEffect(tone) {
        controller.setTone(tone.resource)
    }
    LaunchedEffect(volume) {
        controller.setVolume(volume)
    }
    DisposableEffect(lifecycleOwner, controller) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> controller.resume()
                Lifecycle.Event.ON_STOP -> controller.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(controller) {
        onDispose { controller.release() }
    }
}

private class AmbientSoundController(private val context: Context) {
    private var current: MediaPlayer? = null
    private var outgoing: MediaPlayer? = null
    private var currentResource = 0
    private var requestedVolume = 0f
    private var active = true
    private var fade: ValueAnimator? = null

    fun setTone(resource: Int) {
        if (resource == currentResource) return
        fade?.cancel()
        outgoing?.release()
        outgoing = current
        current = MediaPlayer.create(context, resource)?.apply {
            isLooping = true
            setVolume(0f, 0f)
            if (active && requestedVolume > 0f) start()
        }
        currentResource = resource
        val old = outgoing
        val fresh = current
        fade = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 900
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                val level = audibleVolume()
                old?.setVolume(level * (1f - progress), level * (1f - progress))
                fresh?.setVolume(level * progress, level * progress)
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    old?.release()
                    if (outgoing === old) outgoing = null
                }
            })
            start()
        }
    }

    fun setVolume(volume: Float) {
        requestedVolume = volume.coerceIn(0f, 1f)
        val level = audibleVolume()
        current?.setVolume(level, level)
        if (active && requestedVolume > 0f) {
            if (current?.isPlaying == false) current?.start()
        } else {
            if (current?.isPlaying == true) current?.pause()
        }
    }

    fun pause() {
        active = false
        if (current?.isPlaying == true) current?.pause()
        if (outgoing?.isPlaying == true) outgoing?.pause()
    }

    fun resume() {
        active = true
        if (requestedVolume > 0f && current?.isPlaying == false) current?.start()
    }

    fun release() {
        fade?.cancel()
        current?.release()
        outgoing?.release()
        current = null
        outgoing = null
    }

    private fun audibleVolume(): Float = requestedVolume * requestedVolume * .62f
}
