package com.itszuvalex.femtocraft.proxy

import com.itszuvalex.femtocraft.GuiIDs
import com.itszuvalex.femtocraft.cyber.gui.{GuiCyberBase, GuiGrowthChamber, GuiMachineSelection}
import com.itszuvalex.femtocraft.cyber.tile.{TileCyberBase, TileGrowthChamber}
import com.itszuvalex.femtocraft.industry.gui.{GuiArcFurnace, GuiFrame, GuiMultiblockSelection}
import com.itszuvalex.femtocraft.industry.tile.{TileArcFurnace, TileFrame}
import com.itszuvalex.femtocraft.logistics.gui.GuiItemRepository
import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository
import com.itszuvalex.femtocraft.nanite.gui.GuiNaniteHive
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteHiveSmall
import com.itszuvalex.femtocraft.power.gui.GuiCrystalMount
import com.itszuvalex.femtocraft.power.tile.TileCrystalMount
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 11/21/14.
  */
class ProxyGuiClient extends ProxyGuiCommon {
  override def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
    (ID, world.getTileEntity(x, y, z)) match {
      case (GuiIDs.TileFrameMultiblockSelectorGuiID, _) => new GuiMultiblockSelection(player, player.getHeldItem)
      case (GuiIDs.TileFrameMultiblockGuiID, te: TileFrame) => new GuiFrame(player, player.inventory, te)
      case (GuiIDs.TileCyberBaseGuiID, te: TileCyberBase) => new GuiCyberBase(player, player.inventory, te)
      case (GuiIDs.TileCyberBaseBuildGuiID, te: TileCyberBase) => new GuiMachineSelection(player, player.inventory, te)
      case (GuiIDs.TileNaniteHiveGuiID, te: TileNaniteHiveSmall) => new GuiNaniteHive(player, player.inventory, te)
      case (GuiIDs.TileArcFurnaceGuiID, te: TileArcFurnace) => new GuiArcFurnace(player, player.inventory, te)
      case (GuiIDs.TileGrowthChamberGuiID, te: TileGrowthChamber) => new GuiGrowthChamber(player, player.inventory, te)
      case (GuiIDs.TileItemRepositoryGuiID, te: TileItemRepository) => new GuiItemRepository(player, player.inventory, te)
      case (GuiIDs.TileCrystalMountGuiID, te: TileCrystalMount) => new GuiCrystalMount(player, player.inventory, te)
      case (_, _) => null
    }
  }
}
