package com.rian.osu.beatmap

import com.rian.osu.GameMode
import com.rian.osu.mods.Mod

/**
 * Represents a [PlayableBeatmap] for [GameMode.Droid] game mode.
 */
class DroidPlayableBeatmap @JvmOverloads constructor(
    beatmap: Beatmap,
    mods: Iterable<Mod>? = null,
    customSpeedMultiplier: Float = 1f
) : PlayableBeatmap(beatmap, GameMode.Droid, mods, customSpeedMultiplier)