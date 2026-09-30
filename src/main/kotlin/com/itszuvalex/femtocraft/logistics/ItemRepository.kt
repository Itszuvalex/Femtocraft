package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.femtocraft.FemtoBlockEntities
import com.itszuvalex.femtocraft.FemtoMenus
import com.itszuvalex.femtocraft.FemtoModules
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.core.FragData
import com.itszuvalex.femtocraft.core.FragExpose
import com.itszuvalex.femtocraft.nanite.FragNaniteNode
import com.itszuvalex.itszulib.api.wrappers.WrapperResourceHandlerIItemStorage
import com.itszuvalex.itszulib.core.frag.FragDropInventory
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.neoforge.capabilities.Capabilities

/**
 * A 54-slot indexed item store that attaches to a nanite hive in range. Port of 1.7.10 `TileItemRepository`.
 */
class ItemRepositoryBlockEntity(pos: BlockPos, state: BlockState) :
    FemtoBlockEntity(FemtoBlockEntities.ITEM_REPOSITORY.get(), pos, state), MenuProvider {
    val inventory = IndexedItemStorage(INVENTORY_SIZE) { setChanged() }
    val itemHandler = WrapperResourceHandlerIItemStorage.of(inventory)
    val naniteNode = FragNaniteNode(HIVE_CONNECTION_RADIUS)

    init {
        fragList.addFragment(naniteNode)
        fragList.addInternalFragment(FragDropInventory(inventory))
        fragList.addInternalFragment(FragData("Inventory", FragData.LEVEL, { _, out -> inventory.serialize(out) }, { _, input ->
            inventory.deserialize(input)
        }))
        fragList.addCapability(Capabilities.Item.BLOCK) { itemHandler }
    }

    override fun onServerLoad() = naniteNode.onServerLoad()

    override fun onServerUnload() = naniteNode.onServerUnload()

    override fun onUse(player: Player): InteractionResult {
        if (isServer) player.openMenu(this, blockPos)
        return InteractionResult.SUCCESS
    }

    override fun getDisplayName(): Component = Component.translatable("block.femtocraft.item_repository")

    override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu =
        ItemRepositoryMenu(containerId, inventory, this)

    companion object {
        const val INVENTORY_SIZE = 9 * 6
        const val HIVE_CONNECTION_RADIUS = 32f
    }
}

/**
 * Port of 1.7.10 `ContainerItemRepository`: 6 rows of 9, player inventory below.
 */
class ItemRepositoryMenu(containerId: Int, inventory: Inventory, val repository: ItemRepositoryBlockEntity?) :
    FemtoMenu(FemtoMenus.ITEM_REPOSITORY.get(), containerId, inventory, repository) {

    constructor(containerId: Int, inventory: Inventory, buf: RegistryFriendlyByteBuf) :
        this(containerId, inventory, readBlockEntity<ItemRepositoryBlockEntity>(inventory, buf))

    init {
        repository?.let { repo ->
            for (i in 0 until ItemRepositoryBlockEntity.INVENTORY_SIZE) addStorageSlot(repo.inventory, i, 8 + (i % 9) * 18, 12 + (i / 9) * 18)
        }
        addPlayerInventory(8, 129)
    }
}
