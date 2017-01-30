package com.itszuvalex.femtocraft

import net.minecraft.util.SoundEvent

/**
  * Created by Chris on 1/12/2017.
  */
object FemtoSounds {
  var shiftSound       : SoundEvent = _
  var crystalBreakSound: SoundEvent = _

  def preInit(): Unit = {
    FemtoSoundHelper.size = SoundEvent.REGISTRY.getKeys.size()

    shiftSound = FemtoSoundHelper.registerSound("shiftsound")
    crystalBreakSound = FemtoSoundHelper.registerSound("crystalbreak")
  }

  def init(): Unit = {
  }

  def postInit(): Unit = {

  }
}
