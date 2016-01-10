package com.itszuvalex.femtocraft.particles

import net.minecraft.client.particle.EntityFX

/**
  * Created by Christopher Harris (Itszuvalex) on 1/6/16.
  */

abstract class BaseEntityFx(protected var args: ParticleArgs) extends
EntityFX(args.worldObj, args.posX, args.posY, args.posZ, args.motionX, args.motionY, args.motionZ)

