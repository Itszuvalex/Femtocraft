package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.femtocraft.logistics.storage.IIndexedItemStorage
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.utility.Loc4
import com.itszuvalex.itszulib.api.utility.LocationTracker
import net.neoforged.neoforge.common.util.ValueIOSerializable
import java.util.UUID

/**
 * A job run by an [IJobRunner], possibly depending on jobs of other runners. Port of v3's `IJob` (v3 had no
 * implementation). v3 wrapped the parent and child maps in `Option`s; here an empty map means none. v3's job, queue
 * and runner types were generic in each other, which Kotlin cannot bound; implementations narrow `runner`, `jobs`,
 * `jobQueue` and `runningJobs` with covariant overrides instead.
 */
interface IJob : ValueIOSerializable {
    val jobType: String

    val jobId: UUID

    /** Jobs this job waits on, by the location of their runner. */
    val parentJobs: Map<Loc4, Set<UUID>>

    /** Jobs waiting on this job, by the location of their runner. */
    val childJobs: Map<Loc4, Set<UUID>>

    fun addChildJob(loc: Loc4, id: UUID)

    fun addParentJob(loc: Loc4, id: UUID)

    fun removeChildJob(loc: Loc4, id: UUID)

    fun removeParentJob(loc: Loc4, id: UUID)

    val runner: IJobRunner

    fun canStartRunning(): Boolean

    fun startRunning()

    fun run()

    val deletesWhenFinished: Boolean

    val isInfinite: Boolean

    val iterationCount: Int
}

/**
 * The jobs waiting on an [IJobRunner]. Port of v3's `IJobQueue` (no implementation in v3; its `addJob` took an id,
 * here it takes the job).
 */
interface IJobQueue : ValueIOSerializable {
    val jobs: List<IJob>

    fun getJob(id: UUID): IJob?

    fun addJob(job: IJob)

    fun removeJob(id: UUID)

    fun cancelJob(id: UUID)

    /** v3's `isBlocking`/`setBlocking`; v3 never said what blocking means. */
    var isBlocking: Boolean
}

/**
 * Runs the jobs of its [jobQueue]. Port of v3's `IJobRunner`, which extended `TileEntity`; here it is an interface
 * that a block entity or fragment implements.
 */
interface IJobRunner {
    val jobQueue: IJobQueue

    val runningJobs: List<IJob>

    val runnerLoc: Loc4
}

/**
 * Tracks where providers of each resource type are. Port of v3's `ProviderManager`, which could only add and remove;
 * [providersInRange] and [isProvider] are the lookups it lacked. Server side; cleared when the server stops.
 */
object ProviderManager {
    private val providers = HashMap<Class<*>, LocationTracker>()

    fun addProvider(resourceType: Class<*>, loc: Loc4) = providers.getOrPut(resourceType, ::LocationTracker).trackLocation(loc)

    fun removeProvider(resourceType: Class<*>, loc: Loc4) {
        providers[resourceType]?.removeLocation(loc)
    }

    fun isProvider(resourceType: Class<*>, loc: Loc4): Boolean = providers[resourceType]?.isLocationTracked(loc) ?: false

    fun providersInRange(resourceType: Class<*>, loc: Loc4, range: Float): Sequence<Loc4> =
        providers[resourceType]?.getLocationsInRange(loc, range) ?: emptySequence()

    fun clear() = providers.clear()
}

/**
 * Items spread over indexed storages, grouped by key. Port of v3's `IItemLogisticsNetwork` (no implementation in v3).
 */
interface IItemLogisticsNetwork {
    val keys: Set<String>

    fun getInventories(key: String): Set<IIndexedItemStorage>

    fun containsItem(item: IItemStack, amount: Int, key: String): Boolean

    fun withdrawItem(item: IItemStack, amount: Int, key: String): Boolean

    fun insertItem(item: IItemStack, amount: Int, key: String): Boolean
}
