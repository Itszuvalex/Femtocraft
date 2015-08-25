package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.logistics.distributed.{IWorker, IWorkerProvider}
import com.itszuvalex.femtocraft.logistics.storage.item.IIndexedInventory
import com.itszuvalex.femtocraft.power.node.IPowerNode
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.DescriptionPacket
import net.minecraft.item.ItemStack

import scala.collection.{Set, mutable}

/**
 * Created by Christopher Harris (Itszuvalex) on 8/25/15.
 */
class TileNaniteHive extends TileEntityBase with IIndexedInventory with IPowerNode with INaniteHive with IWorkerProvider with DescriptionPacket {

}
