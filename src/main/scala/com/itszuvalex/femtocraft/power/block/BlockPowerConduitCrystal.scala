package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.api.power.IWiredPowerTier
import com.itszuvalex.femtocraft.power.tile.TilePowerConduitCrystal

class BlockPowerConduitCrystal extends BlockPowerConduit(IWiredPowerTier.CRYSTAL, FemtoBlocks.blockPowerConduitCrystal _, () => new TilePowerConduitCrystal)
