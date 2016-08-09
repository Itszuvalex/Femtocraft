package com.itszuvalex.femtocraft.cyber.tile

import com.itszuvalex.femtocraft.cyber.machine.MachineGrowthChamber
import com.itszuvalex.femtocraft.cyber.recipe.GrowthChamberRecipe
import com.itszuvalex.femtocraft.cyber.{GrowthChamberRegistry, IRecipeRenderer}
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileFluidTank
import com.itszuvalex.itszulib.network.messages.MessageFluidTankUpdate
import com.itszuvalex.itszulib.render.{Point3D, Vector3}
import net.minecraft.client.Minecraft
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraftforge.fluids.{Fluid, FluidRegistry, FluidStack, FluidTank}

/**
  * Created by Alex on 30.09.2015.
  */
object TileGrowthChamber {
  val MACHINE_INDEX_KEY     = "MachineIndex"
  val BASE_POS_COMPOUND_KEY = "BasePos"
  val PROGRESS_TICKS_KEY    = "ProgressTicks"
  val INVENTORY_KEY         = "Inventory"
}

class TileGrowthChamber extends TileEntityBase with CyberMachineMultiblock with TileMultiblockIndexedInventory with TileFluidTank {
  var progress               : Int                 = 0
  var progressTicks          : Int                 = 0
  var lastGrowthStage        : Int                 = 0
  var currentRecipe          : GrowthChamberRecipe = null
  var updateRecipeOnStackDecr: Boolean             = true

  override def onSideActivate(player: EntityPlayer, side: EnumFacing): Boolean = {
    if (hasGUI) player.openGui(getMod, getGuiID, worldObj, info.cLoc.x, info.cLoc.y, info.cLoc.z)
    hasGUI
  }

  override def getMod: AnyRef = Femtocraft

  override def hasGUI: Boolean = isValidMultiBlock

  override def getGuiID: Int = GuiIDs.TileGrowthChamberGuiID

  override def onBlockBreak(): Unit = {
    if (!isValidMultiBlock) return
    basePos.getTileEntity() match {
      case Some(te: TileCyberBase) =>
        te.breakMachinesUpwardsFromSlot(machineIndex)
      case _ =>
    }
  }

  override def serverUpdate(): Unit = {
    if (!isController) return
    if (tank.getFluidAmount > 0) {
      tank.drain(1, true)
      if (tank.getFluidAmount == 0)
        setUpdate()
    }
    if (currentRecipe == null) return
    progressTicks += 1
    val newProgress = math.floor((progressTicks * 100) / currentRecipe.ticks.toDouble).toInt
    if (progress == newProgress) return
    progress = math.min(newProgress, 100)
    currentRecipe.renderObj match {
      case ar: Array[ResourceLocation] =>
        val ind = math.max(math.ceil(ar.length * (progress / 100d)).toInt - 1, 0)
        if (ind != lastGrowthStage) {
          setUpdate()
          lastGrowthStage = ind
        }
      case obj: IRecipeRenderer =>
        setUpdate()
      case _ =>
    }
    if (progress == 100) {
      if (outputItems(false)) {
        updateRecipeOnStackDecr = false
        indInventory.decrStackSize(0, currentRecipe.input.stackSize)
        updateRecipeOnStackDecr = true
        outputItems(true)
        indInventory.markDirty()
        progress = 0
        progressTicks = 0
        setUpdate()
      }
    }
  }

  def outputItems(doOut: Boolean): Boolean = {
    if (currentRecipe == null) return false
    currentRecipe.outputs.forall { item =>
      val remItem = item.copy()
      val minI = if (GrowthChamberRegistry.findMatchingRecipe(item).isDefined) 0 else 1
      for (i <- minI to 9) {
        if (remItem.stackSize > 0) remItem.stackSize -= putItemStack(i, remItem.copy(), doOut)
      }
      remItem.stackSize == 0
    }
  }

  private def putItemStack(id: Int, stack: ItemStack, doPut: Boolean): Int = {
    val curStack = indInventory.getStackInSlot(id)
    if (curStack == null) {
      if (doPut) indInventory.setInventorySlotContents(id, stack)
      if (doPut) indInventory.markDirty()
      stack.stackSize
    } else if (curStack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(curStack, stack)) {
      val amt = math.min(stack.stackSize, math.min(stack.getMaxStackSize, indInventory.getInventoryStackLimit) - curStack.stackSize)
      if (doPut && amt > 0) {
        curStack.stackSize += amt
        indInventory.setInventorySlotContents(id, curStack)
      }
      amt
    } else 0
  }

  override def clientUpdate(): Unit = {
    if (!isController) return
    // Update progress and water amount client-side, will be synced once container is opened
    if (tank.getFluidAmount > 0) {
      tank.drain(1, true)
    }
    if (currentRecipe != null) {
      progressTicks += 1
      val newProgress = math.floor((progressTicks * 100) / currentRecipe.ticks.toDouble).toInt
      if (progress != newProgress) progress = math.min(newProgress, 100)
    }
    // Spawn particles, if water exists and particle setting is "All"
    if (Minecraft.getMinecraft.gameSettings.particleSetting > 0) return
    if (tank.getFluidAmount == 0) return
    val time = worldObj.getTotalWorldTime
    if (time % 3 != 0) return
    particles1(time)
    particles2(time)
    particles3(time)
  }

  def particles1(time: Long): Unit = {
    val cx = 1
    val cy = 1.775 + (1 + math.sin(time * .05)) * .5 * .02924444461012775
    val cz = 1.6 - (1 + math.sin(time * .05)) * .5 * .08034845121081742
    val x1 = cx + .140625
    val x2 = cx + .046875
    val x3 = cx - .046375
    val x4 = cx - .140625
    val dirVec = new Vector3(new Point3D(1, 1.9f, 1.6f), new Point3D(cx.toFloat, cy.toFloat, cz.toFloat)) * -.8f
    //    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x1, yCoord + cy, zCoord + cz, dirVec.x + .025, dirVec.y, dirVec.z, .04f, .2f, yCoord + .21))
    //    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x2, yCoord + cy, zCoord + cz, dirVec.x + .00833333333333333, dirVec.y, dirVec.z, .04f, .2f, yCoord + .21))
    //    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x3, yCoord + cy, zCoord + cz, dirVec.x - .00833333333333333, dirVec.y, dirVec.z, .04f, .2f, yCoord + .21))
    //    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x4, yCoord + cy, zCoord + cz, dirVec.x - .025, dirVec.y, dirVec.z, .04f, .2f, yCoord + .21))
  }

  def particles2(time: Long): Unit = {
    val d_xz = (1 + math.sin(time * .05 + 1)) * .5 * .08034845121081742
    val cx =.4804 +.86602540378443864 * d_xz
    val cy = 1.775 + (1 + math.sin(time * .05 + 1)) * .5 * .02924444461012775
    val cz =.7 +.5 * d_xz
    val x1 = cx + .07031249999999999
    val z1 = cz - .12178482240718669
    val x2 = cx + .023437499999999997
    val z2 = cz - .04059494080239556
    val x3 = cx - .023437499999999997
    val z3 = cz + .04059494080239556
    val x4 = cx - .07031249999999999
    val z4 = cz + .12178482240718669
    val dirVec = new Vector3(new Point3D(.4804f, 1.9f, .7f), new Point3D(cx.toFloat, cy.toFloat, cz.toFloat)) * -.8f
    //    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x1, yCoord + cy, zCoord + z1, dirVec.x + .012500000000000004, dirVec.y, dirVec.z - .021650635094610966, .04f, .2f, yCoord + .21))
    //    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x2, yCoord + cy, zCoord + z2, dirVec.x + .004166666500000001, dirVec.y, dirVec.z - .007216878076195187, .04f, .2f, yCoord + .21))
    //    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x3, yCoord + cy, zCoord + z3, dirVec.x - .004166666500000001, dirVec.y, dirVec.z + .007216878076195187, .04f, .2f, yCoord + .21))
    //    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x4, yCoord + cy, zCoord + z4, dirVec.x - .012500000000000004, dirVec.y, dirVec.z + .021650635094610966, .04f, .2f, yCoord + .21))
  }

  def particles3(time: Long): Unit = {
    val d_xz = (1 + math.sin(time * .05 + 2)) * .5 * .08034845121081742
    val cx = 1.5196 -.86602540378443864 * d_xz
    val cy = 1.775 + (1 + math.sin(time * .05 + 2)) * .5 * .02924444461012775
    val cz =.7 +.5 * d_xz
    val x1 = cx + .07031249999999999
    val z1 = cz + .12178482240718669
    val x2 = cx + .023437499999999997
    val z2 = cz + .04059494080239556
    val x3 = cx - .023437499999999997
    val z3 = cz - .04059494080239556
    val x4 = cx - .07031249999999999
    val z4 = cz - .12178482240718669
    val dirVec = new Vector3(new Point3D(1.5196f, 1.9f, .7f), new Point3D(cx.toFloat, cy.toFloat, cz.toFloat)) * -.8f
    //    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x1, yCoord + cy, zCoord + z1, dirVec.x + .012500000000000004, dirVec.y, dirVec.z + .021650635094610966, .04f, .2f, yCoord + .21))
    //    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x2, yCoord + cy, zCoord + z2, dirVec.x + .004166666500000001, dirVec.y, dirVec.z + .007216878076195187, .04f, .2f, yCoord + .21))
    //    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x3, yCoord + cy, zCoord + z3, dirVec.x - .004166666500000001, dirVec.y, dirVec.z - .007216878076195187, .04f, .2f, yCoord + .21))
    //    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x4, yCoord + cy, zCoord + z4, dirVec.x - .012500000000000004, dirVec.y, dirVec.z - .021650635094610966, .04f, .2f, yCoord + .21))
  }

  override def getRenderBoundingBox = new AxisAlignedBB(getPos, getPos.add(2, 2, 2))

  override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(compound)
    compound.setInteger(TileGrowthChamber.PROGRESS_TICKS_KEY, progressTicks)
    compound
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    progressTicks = compound.getInteger(TileGrowthChamber.PROGRESS_TICKS_KEY)
    currentRecipe = GrowthChamberRegistry.findMatchingRecipe(indInventory.getStackInSlot(0)).orNull
    if (currentRecipe != null) progress = math.floor((progressTicks * 100) / currentRecipe.ticks.toDouble).toInt
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    compound.setInteger(TileGrowthChamber.PROGRESS_TICKS_KEY, progressTicks)
    val comp = new NBTTagCompound
    compound.setTag(TileGrowthChamber.INVENTORY_KEY, indInventory.serializeNBT())
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    progressTicks = compound.getInteger(TileGrowthChamber.PROGRESS_TICKS_KEY)
    val comp = compound.getCompoundTag(TileGrowthChamber.INVENTORY_KEY)
    indInventory.deserializeNBT(comp)
    currentRecipe = GrowthChamberRegistry.findMatchingRecipe(indInventory.getStackInSlot(0)).orNull
    if (currentRecipe != null) progress = math.floor((progressTicks * 100) / currentRecipe.ticks.toDouble).toInt
  }

  override def defaultInventory: IndexedInventory = new IndexedInventory(10) {

    override def isItemValidForSlot(id: Int, stack: ItemStack): Boolean = {
      if (id == 0 && GrowthChamberRegistry.findMatchingRecipe(stack).isEmpty) false
      else super.isItemValidForSlot(id, stack)
    }

    override def setInventorySlotContents(id: Int, stack: ItemStack): Unit = {
      super.setInventorySlotContents(id, stack)
      if (id == 0) {
        currentRecipe = GrowthChamberRegistry.findMatchingRecipe(getStackInSlot(0)).orNull
        if (currentRecipe == null) {
          progress = 0
          progressTicks = 0
        }
      }
    }

    override def decrStackSize(id: Int, amt: Int): ItemStack = {
      val stack = super.decrStackSize(id, amt)
      if (updateRecipeOnStackDecr && id == 0 && getStackInSlot(id) == null) {
        currentRecipe = null
        progress = 0
        progressTicks = 0
      }
      stack
    }

  }

  override def hasDescription: Boolean = true

  override def defaultTank: FluidTank = new FluidTank(5000)

  override def fill(from: EnumFacing, resource: FluidStack, doFill: Boolean): Int = {
    if (resource == null) 0
    else if (resource.getFluid != FluidRegistry.WATER) 0
    else {
      val amt = super.fill(from, resource, doFill)
      //todo: pull out packets
      if (doFill) FemtoPacketHandler.INSTANCE.sendToDimension(new MessageFluidTankUpdate(info.cLoc.x, info.cLoc.y, info.cLoc.z, if (tank.getFluid == null) null else FluidRegistry.getFluidName(tank.getFluid), tank.getFluidAmount), worldObj.provider.getDimension)
      amt
    }
  }

  override def drain(from: EnumFacing, maxDrain: Int, doDrain: Boolean): FluidStack = {
    val stack = super.drain(from, maxDrain, doDrain)
    //todo: pull out packets
    if (doDrain) FemtoPacketHandler.INSTANCE.sendToDimension(new MessageFluidTankUpdate(info.cLoc.x, info.cLoc.y, info.cLoc.z, if (tank.getFluid == null) null else FluidRegistry.getFluidName(tank.getFluid), tank.getFluidAmount), worldObj.provider.getDimension)
    stack
  }

  override def drain(from: EnumFacing, resource: FluidStack, doDrain: Boolean): FluidStack = {
    if (resource == null || !resource.isFluidEqual(tank.getFluid)) return null
    val stack = super.drain(from, resource, doDrain)
    //todo: pull out packets
    if (doDrain) FemtoPacketHandler.INSTANCE.sendToDimension(new MessageFluidTankUpdate(info.cLoc.x, info.cLoc.y, info.cLoc.z, if (tank.getFluid == null) null else FluidRegistry.getFluidName(tank.getFluid), tank.getFluidAmount), worldObj.provider.getDimension)
    stack
  }

  override def canFill(from: EnumFacing, fluid: Fluid): Boolean = false

  override def canDrain(from: EnumFacing, fluid: Fluid): Boolean = false

  override def formMultiBlock(loc: Loc4): Boolean = {
    setModified()
    super.formMultiBlock(loc)
  }

  override def getCyberMachine = MachineGrowthChamber.NAME
}
