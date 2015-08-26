package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.core.Initializable
import com.itszuvalex.femtocraft.core.test.ItemPreviewable
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.item.Item

/**
 * Created by Christopher Harris (Itszuvalex) on 5/3/15.
 */
object FemtoItems extends Initializable {
  var testPreviewableItem : Item = null

  override def preInit(): Unit = {
    testPreviewableItem = new ItemPreviewable
    testPreviewableItem.setCreativeTab(Femtocraft.tab)
    testPreviewableItem.setUnlocalizedName("testPreviewable")
    GameRegistry.registerItem(testPreviewableItem, "testPreviewable")
  }

  override def init(): Unit = {

  }

  override def postInit(): Unit = {

  }

}
