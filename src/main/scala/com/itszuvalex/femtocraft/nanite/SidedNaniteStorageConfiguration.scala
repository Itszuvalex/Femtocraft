package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.api.nanite.INaniteTankOLD
import com.itszuvalex.itszulib.core.SidedStorageConfiguration
import net.minecraft.util.EnumFacing

class SidedNaniteStorageConfiguration(defaults: (EnumFacing) => String, storages: Map[String, INaniteTankOLD], front: () => EnumFacing) extends SidedStorageConfiguration[INaniteTankOLD](defaults, storages, front)
