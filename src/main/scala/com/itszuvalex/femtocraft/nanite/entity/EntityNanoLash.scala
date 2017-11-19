package com.itszuvalex.femtocraft.nanite.entity

import com.itszuvalex.itszulib.render.Vector3
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.projectile.EntityThrowable
import net.minecraft.entity.{Entity, EntityLivingBase}
import net.minecraft.util.DamageSource
import net.minecraft.util.math.RayTraceResult
import net.minecraft.world.World

class EntityNanoLash(world: World, thrower: EntityLivingBase) extends EntityThrowable(world, thrower) {
  var valid = true

  def this(world: World) = this(world, null)

  def this(worldIn: World, x: Double, y: Double, z: Double) = {
    this(worldIn)
    setPosition(x, y, z)
  }

  override def onImpact(result: RayTraceResult): Unit = {
    result.typeOfHit match {
      case RayTraceResult.Type.ENTITY if result.entityHit != thrower && thrower != null && result.entityHit != null && valid =>
        result.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, thrower), 0.0F)
        if (!world.isRemote) {
          val throwerPos = Vector3(thrower.posX, thrower.posY, thrower.posZ)
          val hitPos = Vector3(result.entityHit.posX, result.entityHit.posY, result.entityHit.posZ)
          val dirVec = throwerPos - hitPos
          val duration = 1.5f
          dirVec /= duration
          dirVec /= 2f
          dirVec.y = Math.min(dirVec.y, 1.0)
          //          dirVec.y += .5
          result.entityHit.addVelocity(dirVec.x, dirVec.y, dirVec.z)
          setDead()
        }
      case RayTraceResult.Type.ENTITY if result.entityHit == thrower =>
      case _ if !world.isRemote =>
        setDead()
      case _ =>
    }
  }

  override def onUpdate(): Unit = {
    val entitylivingbase = getThrower
    if (entitylivingbase != null && entitylivingbase.isInstanceOf[EntityPlayer] && !entitylivingbase.isEntityAlive) {
      setDead()
      valid = false
    }
    else super.onUpdate()
  }

  override def changeDimension(dimensionIn: Int): Entity = {
    if (thrower.dimension != dimensionIn) valid = false
    super.changeDimension(dimensionIn)
  }
}
