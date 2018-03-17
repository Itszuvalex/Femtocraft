package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.api.nanite.INaniteTank
import com.itszuvalex.itszulib.core.SidedStorageConfiguration
import net.minecraft.util.EnumFacing

class SidedNaniteStorageConfiguration(defaults: (EnumFacing) => String, storages: Map[String, INaniteTank], front: () => EnumFacing) extends SidedStorageConfiguration[INaniteTank](defaults, storages, front)
