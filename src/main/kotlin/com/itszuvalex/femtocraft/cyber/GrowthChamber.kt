package com.itszuvalex.femtocraft.cyber

import com.itszuvalex.femtocraft.FemtoBlockEntities
import com.itszuvalex.femtocraft.FemtoMenus
import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.core.FluidTanks
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.logistics.IndexedItemStorage
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.capabilities.Capabilities
import java.util.UUID
import kotlin.math.floor
import kotlin.math.min

/**
 * Growth chamber (2x2 base, 2 slots): grows the input item per [GrowthChamberRecipe] into 9 output slots. A 5000 mB
 * water tank drains 1 mB/tick (it only drives the follow-up spray effect). Port of 1.7.10 `TileGrowthChamber`
 * (key `ProgressTicks`).
 */
class GrowthChamberBlockEntity(pos: BlockPos, state: BlockState) :
    CyberMachineBlockEntity(FemtoBlockEntities.GROWTH_CHAMBER.get(), pos, state, CyberMachineRegistry.GROWTH_CHAMBER), MenuProvider {

    val inventory = object : IndexedItemStorage(10, Runnable { onInventoryChanged() }) {
        override fun canInsert(index: Int, stack: IItemStack): Boolean =
            if (index == 0) stack.isEmpty() || GrowthChamberRecipe.find(level, stack.toMinecraft()) != null else false
    }

    /**
     * Water only; exposed for filling from outside (1.7.10 had no way to fill it).
     */
    val tank = FluidTanks(intArrayOf(5000), { setChanged() }) { _, r -> r.`is`(Fluids.WATER) }
    var progress = 0
    var progressTicks = 0
    private var currentRecipe: GrowthChamberRecipe? = null
    private var recipeChecked = false

    init {
        fragList.addInternalFragment(FragData("GrowthChamber", FragData.LEVEL_AND_DESCRIPTION, { scope, out ->
            out.putInt(PROGRESS_TICKS_KEY, progressTicks)
            out.putInt(PROGRESS_KEY, progress)
            inventory.serialize(out.child(INVENTORY_KEY))
            if (scope == NBTSerializationScope.LEVEL) tank.serialize(out.child("Tank"))
        }, { scope, input ->
            progressTicks = input.getIntOr(PROGRESS_TICKS_KEY, 0)
            progress = input.getIntOr(PROGRESS_KEY, 0)
            input.child(INVENTORY_KEY).ifPresent { inventory.deserialize(it) }
            if (scope == NBTSerializationScope.LEVEL) input.child("Tank").ifPresent { tank.deserialize(it) }
            recipeChecked = false
        }))
        fragList.addCapability(Capabilities.Fluid.BLOCK) { controllerChamber()?.tank }
    }

    private fun controllerChamber(): GrowthChamberBlockEntity? = if (multiblock.isController) this else multiblock.controller(level)

    private fun onInventoryChanged() {
        setChanged()
        recipeChecked = false
    }

    private fun recipe(): GrowthChamberRecipe? {
        if (!recipeChecked) {
            val old = currentRecipe
            currentRecipe = GrowthChamberRecipe.find(level, inventory.get(0).toMinecraft())
            recipeChecked = true
            if (currentRecipe == null || currentRecipe !== old && old != null) {
                progress = 0
                progressTicks = 0
            }
        }
        return currentRecipe
    }

    override fun serverTick() {
        if (!multiblock.isController) return
        if (tank.amount(0) > 0) {
            tank.drainDirect(0, 1)
            if (tank.amount(0) == 0) sync()
        }
        val recipe = recipe() ?: return
        progressTicks++
        val newProgress = floor(progressTicks * 100.0 / recipe.ticks).toInt()
        if (progress == newProgress) {
            setChanged()
            return
        }
        progress = min(newProgress, 100)
        if (progress >= 100 && outputItems(recipe, false)) {
            inventory.split(0, recipe.count)
            outputItems(recipe, true)
            progress = 0
            progressTicks = 0
        }
        sync()
    }

    /**
     * @return True if every result fits (and, when [doOut], puts them). Results that are themselves recipe inputs may
     * go back into slot 0.
     */
    fun outputItems(recipe: GrowthChamberRecipe, doOut: Boolean): Boolean {
        val sim = if (doOut) inventory else IndexedItemStorage(inventory.size()).also { copy ->
            for (i in 0 until inventory.size()) copy.setSlot(i, inventory.get(i).copy())
        }
        return recipe.results.all { result ->
            val stack = result.create()
            var rem = IItemStack.of(stack)
            val minSlot = if (GrowthChamberRecipe.find(level, stack) != null) 0 else 1
            for (i in minSlot..9) {
                if (rem.isEmpty()) break
                rem = sim.insert(i, rem)
            }
            rem.isEmpty()
        }
    }

    override fun onUse(player: Player): InteractionResult {
        val controller = controllerChamber() ?: return InteractionResult.PASS
        if (isServer) player.openMenu(controller, controller.blockPos)
        return InteractionResult.SUCCESS
    }

    override fun getDisplayName(): Component = Component.translatable("block.femtocraft.growth_chamber")

    override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
        GrowthChamberMenu(containerId, inventory, this)

    companion object {
        const val PROGRESS_TICKS_KEY = "ProgressTicks"
        const val PROGRESS_KEY = "Progress"
        const val INVENTORY_KEY = "Inventory"
    }
}

/**
 * Port of 1.7.10 `ContainerGrowthChamber`.
 */
class GrowthChamberMenu(containerId: Int, inventory: Inventory, val chamber: GrowthChamberBlockEntity?) :
    FemtoMenu(FemtoMenus.GROWTH_CHAMBER.get(), containerId, inventory, chamber) {
    constructor(containerId: Int, inventory: Inventory, buf: RegistryFriendlyByteBuf) :
        this(containerId, inventory, readBlockEntity<GrowthChamberBlockEntity>(inventory, buf))

    var progress = 0
    var water = 0

    init {
        chamber?.let { c ->
            addStorageSlot(c.inventory, 0, 8, 21)
            for (i in 0..8) addOutputSlot(c.inventory, i + 1, 80 + 18 * (i % 3), 21 + 18 * (i / 3))
            trackInt({ c.progress }, { progress = it })
            trackInt({ c.tank.amount(0) }, { water = it })
        }
        addPlayerInventory()
    }
}

/**
 * Grasping vines: pull entities within [GRAB_RADIUS] toward the machine. Each entity is held by at most one vine.
 * Port of 1.7.10 `TileGraspingVines` (server side; motion reaches clients through normal entity sync).
 */
class GraspingVinesBlockEntity(pos: BlockPos, state: BlockState) :
    CyberMachineBlockEntity(FemtoBlockEntities.GRASPING_VINES.get(), pos, state, CyberMachineRegistry.GRASPING_VINES) {
    private val grabbed = LinkedHashSet<Entity>()

    override fun serverTick() {
        if (!multiblock.isController) return
        findAndGrabEntities()
        pullEntities()
    }

    private val center: Vec3 get() = Vec3(blockPos.x + .5, blockPos.y + 1.0, blockPos.z + .5)

    private fun findAndGrabEntities() {
        val c = center
        val box = AABB(c.x - GRAB_RADIUS, c.y - GRAB_RADIUS, c.z - GRAB_RADIUS, c.x + GRAB_RADIUS, c.y + GRAB_RADIUS, c.z + GRAB_RADIUS)
        level!!.getEntitiesOfClass(Entity::class.java, box) { it.distanceToSqr(c) <= GRAB_RADIUS * GRAB_RADIUS }
            .forEach { if (GRABBED.add(it.uuid)) grabbed += it }
    }

    private fun pullEntities() {
        val target = Vec3(blockPos.x + .5, blockPos.y + .5, blockPos.z + .5)
        grabbed.toList().forEach { entity ->
            when {
                !entity.isAlive || entity.distanceToSqr(target) > GRAB_RADIUS * GRAB_RADIUS -> release(entity)
                entity is Player && entity.abilities.instabuild -> {}
                else -> {
                    val vel = target.subtract(entity.position()).normalize().scale(VELOCITY.toDouble())
                    entity.push(vel.x, vel.y, vel.z)
                    entity.hurtMarked = true
                }
            }
        }
    }

    private fun release(entity: Entity) {
        grabbed -= entity
        GRABBED -= entity.uuid
    }

    override fun onServerUnload() {
        grabbed.toList().forEach(::release)
    }

    companion object {
        const val GRAB_RADIUS = 8f
        const val VELOCITY = .2f

        /**
         * Entities held by any vine (1.7.10 `grabbedHashSet`).
         */
        val GRABBED = HashSet<UUID>()
    }
}
