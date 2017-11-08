package com.itszuvalex.femtocraft

import net.minecraft.util.SoundEvent
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.registries.IForgeRegistry

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Chris on 1/12/2017.
  */
object FemtoSounds {
  var shiftSound       : SoundEvent = _
  var crystalBreakSound: SoundEvent = _

  val soundCallbacks = new ArrayBuffer[() => Unit]()

  @SubscribeEvent def RegisterSound(event: RegistryEvent.Register[SoundEvent]): Unit = {
    val registry = event.getRegistry
    shiftSound = registerSound(registry, "shiftsound")
    crystalBreakSound = registerSound(registry, "crystalbreak")

    soundCallbacks.foreach(_ ())
    soundCallbacks.clear()
  }

  def addCallback(callback: () => Unit): Unit = soundCallbacks += callback

  def registerSound(registry: IForgeRegistry[SoundEvent], name: String): SoundEvent = {
    val sound = new SoundEvent(Resources.Sound(name)).setRegistryName(name)
    registry.register(sound)
    SoundEvent.REGISTRY.getObject(Resources.Sound(name))
  }
}
