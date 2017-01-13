package com.itszuvalex.femtocraft

import net.minecraft.util.SoundEvent

/**
  * Created by Chris on 1/12/2017.
  */
object FemtoSounds {
  var shiftSound: SoundEvent = _

  private var size = 0

  def preInit(): Unit = {
    size = SoundEvent.REGISTRY.getKeys.size()

    shiftSound = register("shiftsound")
  }

  def init(): Unit = {
  }

  def postInit(): Unit = {

  }

  private def register(name: String): SoundEvent = {
    val resource = Resources.Sound(name)
    val sound = new SoundEvent(resource)
    SoundEvent.REGISTRY.register(size, resource, sound)
    size += 1
    sound
  }
}
