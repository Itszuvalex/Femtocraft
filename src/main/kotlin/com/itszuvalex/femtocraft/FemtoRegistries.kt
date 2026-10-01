package com.itszuvalex.femtocraft

import com.itszuvalex.itszulib.api.ModuleCapabilities
import com.itszuvalex.itszulib.core.BlockEntityCore
import com.itszuvalex.itszulib.menu.BlockMenus
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

/**
 * Femtocraft's deferred registers. Each area's content object (e.g. `PowerContent`) registers into these when it is
 * initialized; [register] then attaches them all to the mod bus.
 */
object FemtoRegistries {
    @JvmField
    val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(Femtocraft.ID)

    @JvmField
    val ITEMS: DeferredRegister.Items = DeferredRegister.createItems(Femtocraft.ID)

    @JvmField
    val BLOCK_ENTITY_TYPES: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Femtocraft.ID)

    @JvmField
    val MENUS: DeferredRegister<MenuType<*>> = DeferredRegister.create(Registries.MENU, Femtocraft.ID)

    @JvmField
    val DATA_COMPONENTS: DeferredRegister.DataComponents = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Femtocraft.ID)

    @JvmField
    val SOUNDS: DeferredRegister<SoundEvent> = DeferredRegister.create(Registries.SOUND_EVENT, Femtocraft.ID)

    @JvmField
    val ENTITY_TYPES: DeferredRegister.Entities = DeferredRegister.createEntities(Femtocraft.ID)

    @JvmField
    val ATTACHMENT_TYPES: DeferredRegister<net.neoforged.neoforge.attachment.AttachmentType<*>> =
        DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Femtocraft.ID)

    @JvmField
    val TABS: DeferredRegister<CreativeModeTab> = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Femtocraft.ID)

    private val coreBlockEntities = ArrayList<DeferredHolder<BlockEntityType<*>, out BlockEntityType<out BlockEntityCore>>>()

    /**
     * Registers a block entity type for [blocks] and remembers it, so its fragments' modules and NeoForge capabilities
     * are registered (ItszuLib `ModuleCapabilities.registerBlockEntity`).
     */
    fun <T : BlockEntityCore> blockEntity(
        name: String,
        factory: (BlockPos, BlockState) -> T,
        vararg blocks: () -> Block,
    ): DeferredHolder<BlockEntityType<*>, BlockEntityType<T>> {
        val holder = BLOCK_ENTITY_TYPES.register(name) { -> BlockEntityType(factory, *blocks.map { it() }.toTypedArray()) }
        coreBlockEntities += holder
        return holder
    }

    /**
     * Registers a menu type whose client side finds the block entity at the position the server sent
     * (ItszuLib `IMenuHost.menuPos`).
     */
    inline fun <reified B : BlockEntity, M : AbstractContainerMenu> blockMenu(
        name: String,
        crossinline factory: (Int, Inventory, B?) -> M,
    ): DeferredHolder<MenuType<*>, MenuType<M>> = MENUS.register(name) { ->
        IMenuTypeExtension.create { id, inventory, buf -> factory(id, inventory, BlockMenus.blockEntity<B>(inventory, buf)) }
    }

    fun sound(name: String): DeferredHolder<SoundEvent, SoundEvent> =
        SOUNDS.register(name) { -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Femtocraft.ID, name)) }

    @JvmField
    val TAB: DeferredHolder<CreativeModeTab, CreativeModeTab> = TABS.register("femtocraft") { ->
        CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.femtocraft"))
            .icon { ItemStack(ITEMS.entries.firstOrNull()?.get() ?: net.minecraft.world.item.Items.AMETHYST_SHARD) }
            .displayItems { _, output -> ITEMS.entries.forEach { output.accept(it.get()) } }
            .build()
    }

    fun register(modBus: IEventBus) {
        BLOCKS.register(modBus)
        ITEMS.register(modBus)
        BLOCK_ENTITY_TYPES.register(modBus)
        MENUS.register(modBus)
        DATA_COMPONENTS.register(modBus)
        SOUNDS.register(modBus)
        TABS.register(modBus)
        ENTITY_TYPES.register(modBus)
        ATTACHMENT_TYPES.register(modBus)
        modBus.addListener { event: RegisterCapabilitiesEvent ->
            coreBlockEntities.forEach { ModuleCapabilities.registerBlockEntity(event, it.get()) }
        }
    }
}
