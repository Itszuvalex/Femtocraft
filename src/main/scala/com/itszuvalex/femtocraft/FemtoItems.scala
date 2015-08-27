package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.core.Initializable
import com.itszuvalex.femtocraft.core.Power.ItemPowerCrystal
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.item.Item

/**
 * Created by Christopher Harris (Itszuvalex) on 5/3/15.
 */
object FemtoItems extends Initializable {
  var itemPowerCrystal: Item = null

  override def preInit(): Unit = {
    itemPowerCrystal = new ItemPowerCrystal().setCreativeTab(Femtocraft.tab)
    GameRegistry.registerItem(itemPowerCrystal, "itemPowerCrystal")
  }

  override def init(): Unit = {

  }

  override def postInit(): Unit = {

  }

}
