package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.FemtoBlockEntities
import com.itszuvalex.femtocraft.FemtoModules
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.core.FragExpose
import com.itszuvalex.femtocraft.core.blockEntity
import com.itszuvalex.femtocraft.core.getLoc
import com.itszuvalex.femtocraft.core.getLocs
import com.itszuvalex.femtocraft.core.putLoc
import com.itszuvalex.femtocraft.core.putLocs
import com.itszuvalex.femtocraft.logistics.distributed.DistributedManager
import com.itszuvalex.femtocraft.logistics.distributed.ITask
import com.itszuvalex.femtocraft.logistics.distributed.ITaskProvider
import com.itszuvalex.femtocraft.logistics.distributed.IWorker
import com.itszuvalex.femtocraft.logistics.distributed.IWorkerProvider
import com.itszuvalex.femtocraft.power.FragPowerNode
import com.itszuvalex.femtocraft.power.IPowerNode
import com.itszuvalex.femtocraft.power.PowerNodeRules
import com.itszuvalex.femtocraft.power.PowerStorage
import com.itszuvalex.femtocraft.power.item.PowerCrystalItem
import com.itszuvalex.femtocraft.power.menu.CrystalMountMenu
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.utility.Loc4
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.api.wrappers.WrapperResourceHandlerIItemStorage
import com.itszuvalex.itszulib.core.frag.FragDropInventory
import com.itszuvalex.itszulib.util.Color
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.Capabilities
import kotlin.math.min
import kotlin.random.Random

/**
 * Holds one power crystal and acts as a power node backed by it: distributes the crystal's power to connected nodes
 * with a lower fill ratio, and links to power pedestals directly above/below. Port of 1.7.10 `TileCrystalMount`.
 */
class CrystalMountBlockEntity(pos: BlockPos, state: BlockState) :
    FemtoBlockEntity(FemtoBlockEntities.CRYSTAL_MOUNT.get(), pos, state), MenuProvider {

    val inventory = object : ItemStorageArray(1, Runnable { onInventoryChanged() }) {
        override fun canInsert(index: Int, stack: IItemStack): Boolean = stack.isEmpty() || stack.toMinecraft().item is PowerCrystalItem
        override fun maxStackSize(index: Int): Int = 1
    }
    val itemHandler = WrapperResourceHandlerIItemStorage.of(inventory)
    val node = FragPowerNode(PowerNodeRules.MOUNT, CrystalStorage(), childRadius = PEDESTAL_RANGE)
    private val pedestalLocs = LinkedHashSet<Loc4>()
    private var hadCrystal = false

    init {
        node.colorSource = { crystal()?.let { (it.item as PowerCrystalItem).getColor(it) } ?: node.baseColor }
        fragList.addFragment(node)
        fragList.addInternalFragment(FragDropInventory(inventory))
        fragList.addInternalFragment(FragData("Mount", FragData.LEVEL_AND_DESCRIPTION, { scope, out ->
            out.child(MOUNT_COMPOUND).putLocs(PEDESTALS_KEY, pedestalLocs)
            // Clients render the crystal; the inventory itself is LEVEL-only.
            if (scope == NBTSerializationScope.DESCRIPTION) inventory.serialize(out.child(CRYSTAL_KEY))
        }, { scope, input ->
            input.child(MOUNT_COMPOUND).ifPresent { pedestalLocs.clear(); pedestalLocs += it.getLocs(PEDESTALS_KEY) }
            if (scope == NBTSerializationScope.DESCRIPTION) input.child(CRYSTAL_KEY).ifPresent { inventory.deserialize(it) }
        }))
        fragList.addInternalFragment(FragData("Inventory", FragData.LEVEL, { _, out -> inventory.serialize(out) }, { _, input ->
            inventory.deserialize(input)
            hadCrystal = crystal() != null
        }))
        fragList.addCapability(Capabilities.Item.BLOCK) { itemHandler }
    }

    fun crystal(): ItemStack? = inventory.get(0).toMinecraft().takeIf { it.item is PowerCrystalItem }

    fun getCrystalStack(): ItemStack? = crystal()

    fun getPedestalLocations(): Set<Loc4> = pedestalLocs

    private fun onInventoryChanged() {
        setChanged()
        if (!isServer) return
        val has = crystal() != null
        if (has == hadCrystal) {
            sync()
            return
        }
        hadCrystal = has
        if (has) node.onServerLoad() else node.disconnectAll()
        sync()
    }

    override fun onServerLoad() {
        if (crystal() != null) node.onServerLoad()
    }

    override fun onServerUnload() {
        if (crystal() != null) node.onServerUnload()
    }

    override fun serverTick() {
        val stack = crystal() ?: return
        val item = stack.item as PowerCrystalItem
        item.onTick(stack)
        distributePower(stack, item)
        setChanged()
    }

    /**
     * Pushes up to the crystal's transfer rate into connected nodes that are less full than this one, in proportion
     * to how much less full they are.
     */
    fun distributePower(stack: ItemStack, crystal: PowerCrystalItem) {
        val rate = crystal.getTransferRate(stack)
        val amount = min(node.getPowerCurrent(), rate.toDouble())
        if (amount < 0 || amount.isNaN()) return
        val lvl = level ?: return
        val myFill = node.getPowerCurrent() / node.getPowerMax()
        val connections = (node.getChildrenLocs().orEmpty() + listOfNotNull(node.getParentLoc()))
            .mapNotNull { it.blockEntity(lvl, false) }
            .mapNotNull { (it as? com.itszuvalex.itszulib.api.adapters.IBlockEntity)?.getModule(FemtoModules.POWER_NODE, null) }
            .filter { it.getPowerMax() > 0 && it.getPowerCurrent() / it.getPowerMax() < myFill }
        val filPerc = connections.map { it to it.getPowerCurrent() / it.getPowerMax() }.filter { (_, fill) -> fill > 0 && !fill.isNaN() }
        val difPerc = filPerc.map { (n, fill) -> n to (myFill - fill) }
        val total = difPerc.sumOf { it.second }
        if (total > 0 && !total.isNaN()) {
            difPerc.forEach { (target, perc) ->
                val amt = min(amount * (perc / total), target.getPowerMax() * perc)
                if (amt > 0 && !amt.isNaN()) node.usePower(target.addPower(amt, true), true)
            }
        }
    }

    fun canAcceptPedestal(loc: Loc4): Boolean = loc == this.loc.getOffset(0, 1, 0) || loc == this.loc.getOffset(0, -1, 0)

    fun addPedestal(loc: Loc4) {
        if (!canAcceptPedestal(loc)) return
        pedestalLocs += loc
        sync()
    }

    fun removePedestal(loc: Loc4) {
        pedestalLocs -= loc
        sync()
    }

    override fun onPlaced(placer: LivingEntity?, stack: ItemStack) {
        if (!isServer) return
        checkAndAddPedestal(Direction.UP)
        checkAndAddPedestal(Direction.DOWN)
    }

    private fun checkAndAddPedestal(dir: Direction) {
        val ped = level!!.getBlockEntity(blockPos.relative(dir)) as? PowerPedestalBlockEntity ?: return
        val pedLoc = loc.getOffset(dir.stepX, dir.stepY, dir.stepZ)
        if (ped.mountLoc == null && ped.canSetMount(loc) && canAcceptPedestal(pedLoc)) {
            addPedestal(pedLoc)
            ped.setMount(loc)
        }
    }

    override fun onRemove(level: com.itszuvalex.itszulib.api.adapters.ILevel, pos: BlockPos, blockStatePrev: BlockState) {
        val lvl = level.toMinecraft()
        pedestalLocs.mapNotNull { it.blockEntity(lvl, true) as? PowerPedestalBlockEntity }.forEach { it.setMount(null) }
        super.onRemove(level, pos, blockStatePrev)
    }

    override fun onUse(player: Player): InteractionResult {
        if (isServer) player.openMenu(this, blockPos)
        return InteractionResult.SUCCESS
    }

    override fun getDisplayName(): Component = Component.translatable("block.femtocraft.crystal_mount")

    override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
        CrystalMountMenu(containerId, inventory, this)

    private inner class CrystalStorage : PowerStorage {
        private fun item() = crystal()?.let { it to it.item as PowerCrystalItem }
        override fun current(): Double = item()?.let { (s, i) -> i.getStorageCurrent(s) } ?: 0.0
        override fun max(): Double = item()?.let { (s, i) -> i.getStorageMax(s) } ?: 0.0
        override fun set(amount: Double) {
            item()?.let { (s, i) -> i.setStorageCurrent(s, amount) }
            setChanged()
        }
        override fun add(amount: Double, doFill: Boolean): Double {
            val r = item()?.let { (s, i) -> i.store(s, amount, doFill) } ?: 0.0
            if (doFill) setChanged()
            return r
        }
        override fun use(amount: Double, doUse: Boolean): Double {
            val r = item()?.let { (s, i) -> i.consume(s, amount, doUse) } ?: 0.0
            if (doUse) setChanged()
            return r
        }
    }

    companion object {
        const val MOUNT_COMPOUND = "Mount"
        const val PEDESTALS_KEY = "Pedestals"
        const val CRYSTAL_KEY = "Crystal"
        const val PEDESTAL_RANGE = 8f
    }
}

/**
 * Links to a crystal mount directly above or below; power sinks read the mount through it. Port of
 * 1.7.10 `TilePowerPedestal`.
 */
class PowerPedestalBlockEntity(pos: BlockPos, state: BlockState) :
    FemtoBlockEntity(FemtoBlockEntities.POWER_PEDESTAL.get(), pos, state) {
    var mountLoc: Loc4? = null
        private set

    init {
        fragList.addInternalFragment(FragData("Pedestal", FragData.LEVEL_AND_DESCRIPTION, { _, out ->
            out.child(PEDESTAL_COMPOUND).putLoc(MOUNT_KEY, mountLoc)
        }, { _, input ->
            input.child(PEDESTAL_COMPOUND).ifPresent { mountLoc = it.getLoc(MOUNT_KEY) }
        }))
    }

    fun mount(): CrystalMountBlockEntity? = mountLoc?.let { level?.let { l -> it.blockEntity(l, true) } } as? CrystalMountBlockEntity

    fun canSetMount(loc: Loc4): Boolean = loc == this.loc.getOffset(0, 1, 0) || loc == this.loc.getOffset(0, -1, 0)

    fun setMount(loc: Loc4?) {
        mountLoc = loc
        sync()
    }

    override fun onPlaced(placer: LivingEntity?, stack: ItemStack) {
        if (!isServer) return
        if (!checkAndAddMount(Direction.UP)) checkAndAddMount(Direction.DOWN)
    }

    private fun checkAndAddMount(dir: Direction): Boolean {
        val mount = level!!.getBlockEntity(blockPos.relative(dir)) as? CrystalMountBlockEntity ?: return false
        val mountLoc = loc.getOffset(dir.stepX, dir.stepY, dir.stepZ)
        if (mount.canAcceptPedestal(loc) && canSetMount(mountLoc)) {
            mount.addPedestal(loc)
            setMount(mountLoc)
            return true
        }
        return false
    }

    override fun onRemove(level: com.itszuvalex.itszulib.api.adapters.ILevel, pos: BlockPos, blockStatePrev: BlockState) {
        mount()?.removePedestal(loc)
        super.onRemove(level, pos, blockStatePrev)
    }

    companion object {
        const val PEDESTAL_COMPOUND = "Pedestal"
        const val MOUNT_KEY = "Mount"
    }
}

/**
 * Offers a "dump power" task: generators in range send power into the crystal mount on the pedestal next to this
 * block. Port of 1.7.10 `TilePowerSink`.
 */
class PowerSinkBlockEntity(pos: BlockPos, state: BlockState) :
    FemtoBlockEntity(FemtoBlockEntities.POWER_SINK.get(), pos, state), ITaskProvider, ITilePower {
    val taskDumpPower = TaskDumpPower(this, TaskDumpPower.TASK_TYPE_DUMP_POWER, 5L, 1, 0)

    init {
        fragList.addFragment(FragExpose("TaskProvider", FemtoModules.TASK_PROVIDER) { this })
    }

    override fun serverTick() = taskDumpPower.onTick()

    override fun onServerLoad() = DistributedManager.addTaskProvider(this)

    override fun onServerUnload() = DistributedManager.removeTaskProvider(this)

    override fun getActiveTasks(): Set<ITask> = setOf(taskDumpPower)
    override fun getProviderLocation(): Loc4 = loc
    override fun getWorkerConnectionRadius(): Float = POWER_CONNECTION_RADIUS

    private fun mountNode(): IPowerNode? {
        val lvl = level ?: return null
        val ped = (lvl.getBlockEntity(blockPos.above()) as? PowerPedestalBlockEntity)
            ?: (lvl.getBlockEntity(blockPos.below()) as? PowerPedestalBlockEntity) ?: return null
        return ped.mount()?.node
    }

    override fun getCurrentPower(): Double = mountNode()?.getPowerCurrent() ?: 0.0
    override fun getMaximumPower(): Double = mountNode()?.getPowerMax() ?: 0.0
    override fun charge(amt: Double, doCharge: Boolean): Double = mountNode()?.addPower(amt, doCharge) ?: 0.0
    override fun drain(amt: Double, doDrain: Boolean): Double = mountNode()?.usePower(amt, doDrain) ?: 0.0

    override fun onUse(player: Player): InteractionResult {
        if (!isServer) return InteractionResult.SUCCESS
        player.sendSystemMessage(Component.literal("Power = ${getCurrentPower()}/${getMaximumPower()}"))
        player.sendSystemMessage(Component.literal("Tasks(${getActiveTasks().size}):"))
        getActiveTasks().forEach { task ->
            player.sendSystemMessage(Component.literal("    Task:  workers:${task.getWorkers().size}-${task.getWorkerCap()}"))
            task.getWorkers().forEach { player.sendSystemMessage(Component.literal("       Worker:${it.getProvider().getProviderLocation()}")) }
        }
        return InteractionResult.SUCCESS
    }

    companion object {
        const val POWER_CONNECTION_RADIUS = 16f
    }
}

/**
 * Generates power until full, then offers a worker that dumps it into power sinks in range. Port of
 * 1.7.10 `TilePowerGenerator`.
 */
class PowerGeneratorBlockEntity(pos: BlockPos, state: BlockState) :
    FemtoBlockEntity(FemtoBlockEntities.POWER_GENERATOR.get(), pos, state), IPowerGenerator {
    val workerPowerDumper = WorkerPowerDumper(this, TaskDumpPower.TASK_TYPE_DUMP_POWER)
    val powerMax = POWER_MAXIMUM.toDouble()
    var isDumping = false
        private set
    var powerCurrent = 0.0
        private set

    init {
        fragList.addFragment(FragExpose("WorkerProvider", FemtoModules.WORKER_PROVIDER) { this })
        fragList.addInternalFragment(FragData("Generator", FragData.LEVEL, { _, out ->
            out.putBoolean(KEY_IS_DUMPING, isDumping)
            out.putDouble(KEY_POWER_CURRENT, powerCurrent)
        }, { _, input ->
            isDumping = input.getBooleanOr(KEY_IS_DUMPING, false)
            powerCurrent = input.getDoubleOr(KEY_POWER_CURRENT, 0.0)
        }))
    }

    override fun onServerLoad() {
        if (isDumping) DistributedManager.addWorkerProvider(this)
    }

    override fun onServerUnload() = DistributedManager.removeWorkerProvider(this)

    override fun serverTick() {
        if (powerCurrent >= powerMax) {
            isDumping = true
            DistributedManager.addWorkerProvider(this)
        } else if (!isDumping) {
            charge(POWER_GEN.toDouble(), true)
        }
    }

    override fun charge(amt: Double, doCharge: Boolean): Double {
        val amtd = min(amt, getMaximumPower() - getCurrentPower())
        if (doCharge) {
            powerCurrent += amtd
            setChanged()
        }
        return amtd
    }

    override fun drain(amt: Double, doDrain: Boolean): Double {
        val amtd = min(amt, powerCurrent)
        if (doDrain) {
            powerCurrent -= amtd
            setChanged()
        }
        return amtd
    }

    override fun getCurrentPower(): Double = powerCurrent
    override fun getMaximumPower(): Double = powerMax
    override fun getTaskConnectionRadius(): Float = CONNECTION_RADIUS
    override fun getProviderLocation(): Loc4 = loc
    override fun getProvidedWorkers(): Set<IWorker> = if (isDumping) setOf(workerPowerDumper) else emptySet()

    override fun onDumpNoPower(worker: IWorker) {
        if (worker === workerPowerDumper) {
            DistributedManager.removeWorkerProvider(this)
            isDumping = false
            setChanged()
        }
    }

    override fun onUse(player: Player): InteractionResult {
        if (!isServer) return InteractionResult.SUCCESS
        player.sendSystemMessage(Component.literal("Power = $powerCurrent/$powerMax"))
        player.sendSystemMessage(Component.literal("Workers(${getProvidedWorkers().size}):"))
        getProvidedWorkers().forEach { w ->
            player.sendSystemMessage(Component.literal("    Worker:" + (w.getTask()?.getProvider()?.getProviderLocation()?.toString() ?: " no task")))
        }
        return InteractionResult.SUCCESS
    }

    companion object {
        const val CONNECTION_RADIUS = 16f
        const val POWER_MAXIMUM = 2000L
        const val POWER_GEN = 20L
        const val KEY_IS_DUMPING = "Dumping"
        const val KEY_POWER_CURRENT = "CurrentPower"
    }
}

/**
 * A colored light. Port of 1.7.10 `TileGlowStick`; the color is synced for the (follow-up) colored renderer.
 */
class GlowStickBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(FemtoBlockEntities.GLOW_STICK.get(), pos, state) {
    var color: Int = Color(255.toByte(), (Random.nextInt(125) + 130).toByte(), (Random.nextInt(125) + 130).toByte(), (Random.nextInt(125) + 130).toByte()).toInt()

    init {
        fragList.addInternalFragment(FragData("GlowStick", FragData.LEVEL_AND_DESCRIPTION, { _, out -> out.putInt("color", color) }, { _, input ->
            color = input.getIntOr("color", color)
        }))
    }
}

interface ITilePower {
    fun getCurrentPower(): Double
    fun getMaximumPower(): Double
    fun drain(amt: Double, doDrain: Boolean): Double
    fun charge(amt: Double, doCharge: Boolean): Double
}

interface IPowerGenerator : IWorkerProvider, ITilePower {
    fun onDumpNoPower(worker: IWorker)
}

/**
 * Pulls power from assigned [WorkerPowerDumper]s into the owning sink, up to [transferRate] per worker per tick.
 */
class TaskDumpPower(
    val owner: PowerSinkBlockEntity,
    private val taskType: String,
    val transferRate: Long,
    private val workerCap: Int,
    @Suppress("unused") private val priority: Int,
) : ITask {
    private val workers = LinkedHashSet<IWorker>()

    override fun getTaskType(): String = taskType

    override fun onTick() {
        workers.toList().forEach { worker ->
            worker.inform(INFORM_POWER_RATE, transferRate.toDouble())
            val powerToDump = worker.getEfficiency(EFFICIENCY_POWER_TO_DUMP).toLong()
            var power = min(powerToDump, transferRate)
            power = min(power, owner.getMaximumPower().toLong() - owner.getCurrentPower().toLong())
            worker.inform(INFORM_POWER_DRAINED, power.toDouble())
            owner.charge(power.toDouble(), true)
            worker.onTick()
        }
    }

    override fun removeWorker(worker: IWorker) {
        workers -= worker
    }

    override fun cancel() = DistributedManager.onTaskEnd(this)
    override fun getProvider(): ITaskProvider = owner
    override fun getPriority(): Int = (owner.getMaximumPower() - owner.getCurrentPower()).toInt()

    override fun addWorker(worker: IWorker): Boolean {
        if (workers.size >= getWorkerCap()) return false
        workers += worker
        return true
    }

    override fun getWorkerCap(): Int = workerCap
    override fun getWorkers(): Set<IWorker> = workers

    companion object {
        const val TASK_TYPE_DUMP_POWER = "Dump Power"
        const val EFFICIENCY_POWER_TO_DUMP = "Power"
        const val INFORM_POWER_RATE = "Rate"
        const val INFORM_POWER_DRAINED = "Drained"
    }
}

/**
 * Drains its generator by whatever the task reports it took.
 */
class WorkerPowerDumper(val owner: IPowerGenerator, private val taskType: String) : IWorker {
    private var task: ITask? = null
    var transferRate = 0L
        private set

    override fun getProvider(): IWorkerProvider = owner
    override fun getTask(): ITask? = task
    override fun setTask(task: ITask?) {
        this.task = task
    }

    override fun onTick() {
        if (owner.getCurrentPower() <= 0) owner.onDumpNoPower(this)
    }

    override fun getEfficiency(attribute: String): Double =
        if (attribute == TaskDumpPower.EFFICIENCY_POWER_TO_DUMP) owner.getCurrentPower() else 0.0

    override fun canWorkTask(task: ITask): Boolean = task.getTaskType().equals(taskType, ignoreCase = true)

    override fun inform(key: String, value: Double) {
        when (key) {
            TaskDumpPower.INFORM_POWER_RATE -> transferRate = value.toLong()
            TaskDumpPower.INFORM_POWER_DRAINED -> owner.drain(value.toLong().toDouble(), true)
        }
    }
}
