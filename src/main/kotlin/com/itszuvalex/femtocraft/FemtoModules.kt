package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.logistics.distributed.ITaskProvider
import com.itszuvalex.femtocraft.logistics.distributed.IWorkerProvider
import com.itszuvalex.femtocraft.nanite.INaniteHive
import com.itszuvalex.femtocraft.nanite.INaniteNode
import com.itszuvalex.femtocraft.power.IPowerNode
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.api.adapters.Module
import net.minecraft.resources.Identifier

/**
 * Femtocraft modules: behaviours block entities expose through fragments. None are NeoForge capabilities; they are
 * Femtocraft-internal and looked up with `getModule`.
 */
object FemtoModules {
    private fun id(path: String) = Identifier.fromNamespaceAndPath(Femtocraft.ID, path)

    @JvmField
    val POWER_NODE: IModule<IPowerNode> = Module.registerModule(id("power_node"), null)

    @JvmField
    val TASK_PROVIDER: IModule<ITaskProvider> = Module.registerModule(id("task_provider"), null)

    @JvmField
    val WORKER_PROVIDER: IModule<IWorkerProvider> = Module.registerModule(id("worker_provider"), null)

    @JvmField
    val NANITE_HIVE: IModule<INaniteHive> = Module.registerModule(id("nanite_hive"), null)

    @JvmField
    val NANITE_NODE: IModule<INaniteNode> = Module.registerModule(id("nanite_node"), null)

    /**
     * Forces registration during mod construction.
     */
    fun init() {}
}
