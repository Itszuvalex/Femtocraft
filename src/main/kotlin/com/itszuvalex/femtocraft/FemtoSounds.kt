package com.itszuvalex.femtocraft

import net.minecraft.sounds.SoundEvent
import net.neoforged.neoforge.registries.DeferredHolder

/**
 * Femtocraft's sounds (`assets/femtocraft/sounds.json`). Port of v3's `FemtoSounds`.
 */
object FemtoSounds {
    @JvmField
    val SHIFT: DeferredHolder<SoundEvent, SoundEvent> = FemtoRegistries.sound("shiftsound")

    @JvmField
    val CRYSTAL_BREAK: DeferredHolder<SoundEvent, SoundEvent> = FemtoRegistries.sound("crystalbreak")

    @JvmField
    val RIFT_LOOP: DeferredHolder<SoundEvent, SoundEvent> = FemtoRegistries.sound("riftloop")

    fun init() {}
}
