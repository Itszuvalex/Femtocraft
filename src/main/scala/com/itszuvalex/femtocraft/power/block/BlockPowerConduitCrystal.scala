package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.api.IConduitTier
import com.itszuvalex.femtocraft.common.block.BlockConduit
import com.itszuvalex.femtocraft.power.tile.TilePowerConduitCrystal

class BlockPowerConduitCrystal extends BlockConduit(IConduitTier.CRYSTAL, FemtoBlocks.blockPowerConduitCrystal _, () => new TilePowerConduitCrystal)
