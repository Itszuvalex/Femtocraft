package com.itszuvalex.femtocraft.api.logistics

import com.itszuvalex.femtocraft.api.nanite.NaniteStack
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack

/**
  * Created by Chris on 2/19/2017.
  */
object Connections {
  implicit def asItem(connection: IConnection[_]): IConnection[ItemStack] = {
    if (connection.resource == LogisticsResourceRegistry.RESOURCE_ITEMS)
      connection.asInstanceOf[IConnection[ItemStack]]
    else null
  }

  implicit def asFluid(connection: IConnection[_]): IConnection[FluidStack] = {
    if (connection.resource == LogisticsResourceRegistry.RESOURCE_FLUIDS)
      connection.asInstanceOf[IConnection[FluidStack]]
    else null
  }

  implicit def asNanite(connection: IConnection[_]): IConnection[NaniteStack] = {
    if (connection.resource == LogisticsResourceRegistry.RESOURCE_NANITES)
      connection.asInstanceOf[IConnection[NaniteStack]]
    else null
  }
}
