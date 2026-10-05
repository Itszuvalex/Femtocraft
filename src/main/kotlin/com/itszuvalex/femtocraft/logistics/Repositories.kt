package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoHorizontalEntityBlock
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.nanite.FragNaniteAutoIO
import com.itszuvalex.femtocraft.nanite.FragNaniteTank
import com.itszuvalex.femtocraft.nanite.INaniteTank
import com.itszuvalex.femtocraft.nanite.NaniteModules
import com.itszuvalex.femtocraft.nanite.NaniteTank
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration
import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IFluidStack
import com.itszuvalex.itszulib.api.storage.FluidStorageArray
import com.itszuvalex.itszulib.api.storage.IFluidStorage
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.core.BreakBehavior
import com.itszuvalex.itszulib.core.HorizontalFacing
import com.itszuvalex.itszulib.core.SidedFluidStorageConfiguration
import com.itszuvalex.itszulib.core.SidedItemStorageConfiguration
import com.itszuvalex.itszulib.core.frag.FragFluidAutoIO
import com.itszuvalex.itszulib.core.frag.FragFluidStorage
import com.itszuvalex.itszulib.core.frag.FragItemAutoIO
import com.itszuvalex.itszulib.core.frag.FragItemStorage
import com.itszuvalex.itszulib.core.frag.FragMenu
import com.itszuvalex.itszulib.core.frag.FragSidedConfiguration
import com.itszuvalex.itszulib.core.frag.addFluidStorage
import com.itszuvalex.itszulib.core.frag.addItemStorage
import com.itszuvalex.itszulib.core.frag.addTickableFragment
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.tags.FluidTags
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.Fluids
import net.neoforged.neoforge.fluids.FluidStack

private const val NONE = "None"

/**
 * 54-slot chest exposed on every face, with automatic IO. Port of v3's `TileItemRepository`. Keeps its items when
 * broken (the dropped item carries them, ItszuLib D20).
 */
class ItemRepositoryBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(LogisticsContent.ITEM_REPOSITORY_BE.get(), pos, state) {
    @JvmField
    val storage = ItemStorageArray(SIZE) { markDirty() }

    init {
        fragList.addFragment(FragSidedConfiguration(
            "ItemConfig", SidedItemStorageConfiguration({ INVENTORY }, mapOf(NONE to IItemStorage.Empty, INVENTORY to storage)) { HorizontalFacing.front(blockState) },
            Modules.ITEM_STORAGE_CONFIGURABLE,
        ))
        fragList.addItemStorage(FragItemStorage(storage, breakBehavior = BreakBehavior.KEEP))
        fragList.addTickableFragment(FragItemAutoIO())
        fragList.addFragment(FragMenu(Component.translatable("block.femtocraft.item_repository"), { id, inv, _ -> ItemRepositoryMenu(id, inv, this) }))
    }

    companion object {
        const val SIZE = 9 * 6
        const val INVENTORY = "Inventory"
    }
}

/**
 * 5000 mB tank that fills with water from a water block below it (25 mB/tick), exposed on every face with automatic
 * IO; the tank is synced to clients (v3 drew it). Port of v3's `TileFluidRepository`.
 */
class FluidRepositoryBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(LogisticsContent.FLUID_REPOSITORY_BE.get(), pos, state) {
    @JvmField
    val tank = FluidStorageArray(1, TANK_SIZE) { markDirty() }

    private var lastFluid: net.minecraft.world.level.material.Fluid? = null

    init {
        fragList.addFragment(FragSidedConfiguration(
            "FluidConfig", SidedFluidStorageConfiguration({ TANK }, mapOf(NONE to IFluidStorage.Empty, TANK to tank)) { HorizontalFacing.front(blockState) },
            Modules.FLUID_STORAGE_CONFIGURABLE,
        ))
        fragList.addFluidStorage(FragFluidStorage(tank, breakBehavior = BreakBehavior.KEEP))
        fragList.addInternalFragment(FragData("TankSync", setOf(NBTSerializationScope.DESCRIPTION), { _, o -> tank.serialize(o) }, { _, i -> tank.deserialize(i) }))
        fragList.addTickableFragment(FragFluidAutoIO())
        fragList.addFragment(FragMenu(Component.translatable("block.femtocraft.fluid_repository"), { id, inv, _ -> FluidRepositoryMenu(id, inv, this) }))
    }

    override fun serverTick() {
        val lvl = level ?: return
        if (lvl.getFluidState(blockPos.below()).`is`(FluidTags.WATER)) tank.fill(IFluidStack.of(FluidStack(Fluids.WATER, WATER_PER_TICK)), true)
        val fluid = tank.get(0).toMinecraft().takeIf { !it.isEmpty }?.fluid
        if (fluid != lastFluid) {
            lastFluid = fluid
            markDirtyAndSync()
        }
    }

    companion object {
        const val TANK_SIZE = 5000
        const val WATER_PER_TICK = 25
        const val TANK = "Tank"
    }
}

/**
 * 250-nanite tank for a single strain, exposed on every face with automatic IO. Port of v3's `TileNaniteRepository`.
 */
class NaniteRepositoryBlockEntity(pos: BlockPos, state: BlockState) : FemtoBlockEntity(LogisticsContent.NANITE_REPOSITORY_BE.get(), pos, state),
    com.itszuvalex.femtocraft.nanite.NaniteMachine {
    override val naniteTank = NaniteTank(VOLUME, { markDirty() }) { tank, stack -> tank.contents().isEmpty() || tank.canDrain(stack) }

    init {
        fragList.addFragment(FragSidedConfiguration(
            "NaniteConfig", SidedNaniteStorageConfiguration({ TANK }, mapOf(NONE to INaniteTank.EMPTY, TANK to naniteTank)) { HorizontalFacing.front(blockState) },
            NaniteModules.NANITE_STORAGE_CONFIGURABLE,
        ))
        fragList.addFragment(FragNaniteTank(naniteTank, breakBehavior = BreakBehavior.KEEP))
        fragList.addTickableFragment(FragNaniteAutoIO())
        fragList.addFragment(FragMenu(Component.translatable("block.femtocraft.nanite_repository"), { id, inv, _ -> NaniteRepositoryMenu(id, inv, this) }))
    }

    companion object {
        const val VOLUME = 250
        const val TANK = "Tank"
    }
}

class ItemRepositoryBlock(p: BlockBehaviour.Properties) : FemtoHorizontalEntityBlock<ItemRepositoryBlockEntity>(p, { LogisticsContent.ITEM_REPOSITORY_BE.get() })
class FluidRepositoryBlock(p: BlockBehaviour.Properties) : FemtoHorizontalEntityBlock<FluidRepositoryBlockEntity>(p, { LogisticsContent.FLUID_REPOSITORY_BE.get() })
class NaniteRepositoryBlock(p: BlockBehaviour.Properties) : FemtoHorizontalEntityBlock<NaniteRepositoryBlockEntity>(p, { LogisticsContent.NANITE_REPOSITORY_BE.get() })
