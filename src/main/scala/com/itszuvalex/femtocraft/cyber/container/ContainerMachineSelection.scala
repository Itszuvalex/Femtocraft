package com.itszuvalex.femtocraft.cyber.container

import com.itszuvalex.itszulib.container.ContainerBase
import net.minecraft.entity.player.EntityPlayer

/**
 * Created by Alex on 15.10.2015.
 */
class ContainerMachineSelection extends ContainerBase {
  override def canInteractWith(player : EntityPlayer): Boolean = true
}
