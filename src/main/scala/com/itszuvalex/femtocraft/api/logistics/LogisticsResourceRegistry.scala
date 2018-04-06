package com.itszuvalex.femtocraft.api.logistics

import java.util

import com.itszuvalex.femtocraft.api.nanite.NaniteStack
import com.itszuvalex.itszulib.util.Comparators
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

    /**
      * 'Empty' Resources MUST be sorted last.  Otherwise, we will always fill empty slots before merging.
      *
      * @param a Resource A
      * @param b Resource B
      *
      * @return True if A is less than B.
      */
    override def sort(a: ItemStack, b: ItemStack): Boolean = {
      val aEmpty = a == null || a.isEmpty
      val bEmpty = b == null || b.isEmpty
      // Invert so emptiness goes to rear
      if (aEmpty && !bEmpty) false
      else if (aEmpty && bEmpty) false
      else if (!aEmpty && bEmpty) true
      else Comparators.ItemStack.IDDamageNBTComparator.compare(a, b) < 0
    }
  }
  val RESOURCE_NANITES = new IResource[NaniteStack] {
    override def resourceKey = "nanites"

    /**
      * 'Empty' Resources MUST be sorted last.  Otherwise, we will always fill empty slots before merging.
      *
      * @param a Resource A
      * @param b Resource B
      *
      * @return True if A is less than B.
      */
    override def sort(a: NaniteStack, b: NaniteStack): Boolean = {
      val aEmpty = a == null || a.nanite == null
      val bEmpty = b == null || b.nanite == null
      if (aEmpty && !bEmpty) false
      else if (aEmpty && bEmpty) false
      else if (!aEmpty && bEmpty) true
      else a.nanite.strain.compareToIgnoreCase(b.nanite.strain) < 0
    }
  }
  val RESOURCE_FLUIDS  = new IResource[FluidStack] {
    override def resourceKey = "fluids"

    /**
      * 'Empty' Resources MUST be sorted last.  Otherwise, we will always fill empty slots before merging.
      *
      * @param a Resource A
      * @param b Resource B
      *
      * @return True if A is less than B.
      */
    override def sort(a: FluidStack, b: FluidStack): Boolean = {
      val aEmpty = a == null || a.getFluid == null
      val bEmpty = b == null || b.getFluid == null
      if (aEmpty && !bEmpty) false
      else if (aEmpty && bEmpty) false
      else if (!aEmpty && bEmpty) true
      else Comparators.FluidStack.IDNBTComparator.compare(a, b) < 0
    }
  }

  val resources = new ArrayBuffer[IResource[_]]()

  def getResources: util.Collection[IResource[_]] = resources

  def init(): Unit = {
    addResource(RESOURCE_FLUIDS)
    addResource(RESOURCE_ITEMS)
    addResource(RESOURCE_NANITES)
  }

  def addResource(resource: IResource[_]): Unit = resources += resource

  def getResource(key: String): Option[IResource[_]] = resources.find(_.resourceKey == key)

}
