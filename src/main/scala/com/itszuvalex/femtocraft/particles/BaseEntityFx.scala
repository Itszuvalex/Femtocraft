package com.itszuvalex.femtocraft.particles

import net.minecraft.client.particle.Particle
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * Created by Christopher Harris (Itszuvalex) on 1/6/16.
  */

@SideOnly(Side.CLIENT)
abstract class BaseEntityFx(protected var args: ParticleArgs) extends
  Particle(args.worldObj, args.posX, args.posY, args.posZ, args.motionX, args.motionY, args.motionZ)

