package com.itszuvalex.femtocraft

import net.minecraft.block.Block
import net.minecraft.block.material.Material

/**
  * Created by Christopher Harris (Itszuvalex) on 5/18/15.
  */

class BlockTest() extends Block(Material.IRON) {
  setCreativeTab(Femtocraft.tab)
  setHardness(1.0f)
  setTickRandomly(true)
}

