package com.itszuvalex.femtocraft.nanite.items

import com.itszuvalex.femtocraft.nanite.entity.EntityNanoLash
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.{ActionResult, EnumActionResult, EnumHand}
import net.minecraft.world.World

class ItemNanolash extends Item {
  override def onItemRightClick(worldIn: World, playerIn: EntityPlayer, handIn: EnumHand): ActionResult[ItemStack] = {
    val itemstack = playerIn.getHeldItem(handIn)
    //    worldIn.playSound(null.asInstanceOf[EntityPlayer], playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_ENDERPEARL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat * 0.4F + 0.8F))
    playerIn.getCooldownTracker.setCooldown(this, 20)
    if (!worldIn.isRemote) {
      val entityLash = new EntityNanoLash(worldIn, playerIn)
      entityLash.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F)
      worldIn.spawnEntity(entityLash)
    }
    new ActionResult[ItemStack](EnumActionResult.SUCCESS, itemstack)
  }
}
