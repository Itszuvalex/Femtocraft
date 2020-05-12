package com.itszuvalex.femtocraft.computation.block

import com.itszuvalex.femtocraft.FemtoBlocks
import com.itszuvalex.femtocraft.api.IConduitTier
import com.itszuvalex.femtocraft.common.block.BlockConduit
import com.itszuvalex.femtocraft.computation.tile.TileComputationConduitCrystal
import com.itszuvalex.femtocraft.power.tile.TilePowerConduitCrystal

class BlockComputationConduitCrystal extends BlockConduit(IConduitTier.CRYSTAL, FemtoBlocks.blockComputationConduitCrystal _, () => new TileComputationConduitCrystal)
