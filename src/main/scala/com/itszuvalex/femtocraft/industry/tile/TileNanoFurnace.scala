package com.itszuvalex.femtocraft.industry.tile

import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace.SmeltTask._
import com.itszuvalex.femtocraft.industry.tile.TileNanoFurnace.{SmeltTask, TASK_NBT}
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.femtocraft.power.node.{IPowerNode, PowerNode}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.{Converter, IItemStack}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileInventory
import com.itszuvalex.itszulib.util.Task
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.FurnaceRecipes
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

/**
  * Created by Chris on 8/14/2016.
  */

object TileNanoFurnace {
  val TICKS_REQ      = 20 * 8
  val POWER_PER_TICK = 10
  val POWER_REQ      = TICKS_REQ * POWER_PER_TICK

  val TASK_NBT = "Task"

  object SmeltTask {
    val SMELTING_STACK_NBT = "Smelt"
  }

  class SmeltTask(var stack: IItemStack) extends Task(POWER_REQ, TICKS_REQ) {
    override def deserializeNBT(t: NBTTagCompound): Unit = {
      super.deserializeNBT(t)
      if (t.hasKey(SMELTING_STACK_NBT))
        stack = IItemStack.createFromNBT(t.getCompoundTag(SMELTING_STACK_NBT))
    }

    override def serializeNBT(): NBTTagCompound = {
      val ret = super.serializeNBT()
      if (stack != null)
        ret.setTag(SMELTING_STACK_NBT, stack.serializeNBT())
      ret
    }
  }

}

class TileNanoFurnace extends TileEntityBase with TileInventory with PowerNode {
  private val task: SmeltTask = new SmeltTask(IItemStack.Empty)
  powerMax = 4000

  override def defaultStorage: IItemStorage = new ItemStorageArray(2)

  override def getFieldCount: Int = 0

  override def getField(id: Int): Int = 0

  override def setField(id: Int, value: Int): Unit = {}

  override def getMod: AnyRef = Femtocraft

  override def hasDescription: Boolean = true

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean = {
    if (slot == 0) FurnaceRecipes.instance().getSmeltingResult(item) != null
    else false
  }

  override def hasGUI = true

  override def getGuiID = GuiIDs.TileFurnaceGuiID

  /**
    *
    * @return The type of PowerNode this is.
    */
  override def getType: String = IPowerNode.DIFFUSION_TARGET_NODE

  override def onSideActivate(player: EntityPlayer, side: EnumFacing): Boolean = {
    if (hasGUI) player.openGui(getMod, getGuiID, worldObj, pos.getX, pos.getY, pos.getZ)
    hasGUI
  }

  override def serverUpdate(): Unit = {
    if (task.stack == null || task.stack.isEmpty) {
      val item = storage(0)
      if (!item.isEmpty) {
        val ins = storage.split(0, 1)
        task.reset()
        task.stack = ins
      }
    }
    else {
      usePower(task.contribute(Math.min(task.powerPerTick(0, 0), getPowerCurrent), 0, 0), doUse = true)
      if (task.completed(0)) {
        val item = task.stack
        if (item == null || item.isEmpty) {
          task.reset()
          task.stack = null
          return
        }
        val resultItem = FurnaceRecipes.instance().getSmeltingResult(item.toMinecraft)
        if (resultItem == null) {
          task.reset()
          task.stack = null
          return
        }
        // Will clear the stack once we successfully insert the result item
        task.stack = storage.insert(1, Converter.IItemStackFromItemStack(resultItem.copy()))
        if (task.stack == null || task.stack.isEmpty)
          task.reset()
      }
    }
  }

  def setProgress(progress: Double): Unit = {
    task.progress = progress
  }

  def getProgress = task.progress


  /* Tile Entity */
  override def validate(): Unit = {
    super.validate()
    if (!getWorld.isRemote) PowerManager.addNode(this)
  }

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    super.deserializeNBT(nbt)
    task.deserializeNBT(nbt.getCompoundTag(TASK_NBT))
  }

  override def serializeNBT(): NBTTagCompound = {
    val ret = super.serializeNBT()
    ret.setTag(TASK_NBT, task.serializeNBT())
    ret
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    savePowerConnectionInfo(compound)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    loadPowerConnectionInfo(compound)
  }

  /**
    *
    * @param parent Parent being set.
    *
    * @return True if parent is successfully set to input parent.
    */
  override def setParent(parent: IPowerNode): Boolean = {
    val ret = super.setParent(parent)
    setUpdate()
    ret
  }

  /**
    *
    * @param child
    *
    * @return True if child is capable of being a child of this node.
    */
  override def canAddChild(child: IPowerNode): Boolean = false

  /**
    *
    * @param parent IPowerNode that is being checked.
    *
    * @return True if this node is capable of having that node as a parent.
    */
  override def canSetParent(parent: IPowerNode): Boolean = super.canSetParent(parent) && parent.getType == IPowerNode.CRYSTAL_MOUNT
}
