package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.api.IConduitTier
import com.itszuvalex.femtocraft.power.tile.TilePowerConduitCrystal

class BlockPowerConduitCrystal extends BlockPowerConduit(IConduitTier.CRYSTAL, FemtoBlocks.blockPowerConduitCrystal _, () => new TilePowerConduitCrystal)
