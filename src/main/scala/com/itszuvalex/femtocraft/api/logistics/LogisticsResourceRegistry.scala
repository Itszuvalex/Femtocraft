package com.itszuvalex.femtocraft.api.logistics

import java.util

import com.itszuvalex.femtocraft.api.nanite.NaniteStack
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

/**
  * Created by Chris on 2/19/2017.
  */
object LogisticsResourceRegistry {
  val RESOURCE_ITEMS   = new IResource[ItemStack] {
    override def resourceKey = "items"
  }
  val RESOURCE_NANITES = new IResource[NaniteStack] {
    override def resourceKey = "nanites"
  }
  val RESOURCE_FLUIDS  = new IResource[FluidStack] {
    override def resourceKey = "fluids"
  }

  val resources = new ArrayBuffer[IResource[_]]()

  def addResource(resource: IResource[_]): Unit = resources += resource

  def getResources: util.Collection[IResource[_]] = resources

  def init(): Unit = {
    addResource(RESOURCE_FLUIDS)
    addResource(RESOURCE_ITEMS)
    addResource(RESOURCE_NANITES)
  }

  def getResource(key: String): Option[IResource[_]] = resources.find(_.resourceKey == key)

}
