package com.itszuvalex.femtocraft

import net.minecraft.util.SoundEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.registries.IForgeRegistry

/**
  * Created by Chris on 1/12/2017.
  */
object FemtoSounds {
  var shiftSound       : SoundEvent = _
  var crystalBreakSound: SoundEvent = _

  @SubscribeEvent def RegisterSound(event: RegistryEvent.Register[SoundEvent]): Unit = {
    val registry = event.getRegistry
    shiftSound = registerSound(registry, "shiftsound")
    crystalBreakSound = registerSound(registry, "crystalbreak")
  }

  def registerSound(registry: IForgeRegistry[SoundEvent], name: String): SoundEvent = {
    val sound = new SoundEvent(Resources.Sound(name)).setRegistryName(name)
    registry.register(sound)
    sound
  }
}
