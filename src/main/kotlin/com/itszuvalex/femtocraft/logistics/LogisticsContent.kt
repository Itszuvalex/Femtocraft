package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.femtocraft.FemtoRegistries
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension
import java.util.function.UnaryOperator

/**
 * Logistics area registrations.
 */
object LogisticsContent {
    private val R = FemtoRegistries

    private fun machine(p: BlockBehaviour.Properties) = p.strength(2f).sound(SoundType.METAL).requiresCorrectToolForDrops()

    private fun <B : net.minecraft.world.level.block.Block> block(name: String, factory: (BlockBehaviour.Properties) -> B, props: (BlockBehaviour.Properties) -> BlockBehaviour.Properties = ::machine) =
        R.BLOCKS.registerBlock(name, factory, UnaryOperator { props(it) }).also { R.ITEMS.registerSimpleBlockItem(name, it) }

    @JvmField val ITEM_REPOSITORY = block("item_repository", ::ItemRepositoryBlock)
    @JvmField val FLUID_REPOSITORY = block("fluid_repository", ::FluidRepositoryBlock) { machine(it).noOcclusion() }
    @JvmField val NANITE_REPOSITORY = block("nanite_repository", ::NaniteRepositoryBlock)
    @JvmField val CONDUIT = block("conduit", ::ConduitBlock) { machine(it).noOcclusion() }

    @JvmField val ITEM_CHIP = R.ITEMS.registerItem("logistics_item_chip_basic", ::ItemChipItem, UnaryOperator { it.stacksTo(1) })
    @JvmField val FLUID_CHIP = R.ITEMS.registerSimpleItem("logistics_fluid_chip_basic")
    @JvmField val NANITE_CHIP = R.ITEMS.registerSimpleItem("logistics_nanite_chip_basic")
    @JvmField val NANO_PACK = R.ITEMS.registerItem("nano_pack", ::NanoPackItem, UnaryOperator { it.stacksTo(1) })

    @JvmField val ITEM_REPOSITORY_BE = R.blockEntity("item_repository", ::ItemRepositoryBlockEntity, ITEM_REPOSITORY::get)
    @JvmField val FLUID_REPOSITORY_BE = R.blockEntity("fluid_repository", ::FluidRepositoryBlockEntity, FLUID_REPOSITORY::get)
    @JvmField val NANITE_REPOSITORY_BE = R.blockEntity("nanite_repository", ::NaniteRepositoryBlockEntity, NANITE_REPOSITORY::get)
    @JvmField val CONDUIT_BE = R.blockEntity("conduit", ::ConduitBlockEntity, CONDUIT::get)

    @JvmField val ITEM_REPOSITORY_MENU = R.blockMenu<ItemRepositoryBlockEntity, ItemRepositoryMenu>("item_repository", ::ItemRepositoryMenu)
    @JvmField val FLUID_REPOSITORY_MENU = R.blockMenu<FluidRepositoryBlockEntity, FluidRepositoryMenu>("fluid_repository", ::FluidRepositoryMenu)
    @JvmField val NANITE_REPOSITORY_MENU = R.blockMenu<NaniteRepositoryBlockEntity, NaniteRepositoryMenu>("nanite_repository", ::NaniteRepositoryMenu)
    @JvmField val CONDUIT_MENU = R.blockMenu<ConduitBlockEntity, ConduitMenu>("conduit", ::ConduitMenu)
    @JvmField val NANO_PACK_MENU = R.MENUS.register("nano_pack") { ->
        IMenuTypeExtension.create { id, inv, buf -> NanoPackMenu(id, inv, buf.readEnum(net.minecraft.world.InteractionHand::class.java)) }
    }

    fun init() {
        ItemChips.CONNECTION
        LogisticsConduit.MODULE
    }

}
