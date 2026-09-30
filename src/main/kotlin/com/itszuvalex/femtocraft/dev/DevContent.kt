package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.core.FemtoBlockEntity
import com.itszuvalex.femtocraft.core.FemtoEntityBlock
import com.itszuvalex.femtocraft.power.FragPowerNode
import com.itszuvalex.femtocraft.power.OwnPowerStorage
import com.itszuvalex.femtocraft.power.PowerNodeRules
import com.itszuvalex.itszulib.api.ModuleCapabilities
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent
import net.neoforged.neoforge.registries.DeferredBlock
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister

/**
 * Development-only content: the 1.7.10 in-world test blocks (a power node of each type). Registered only when
 * `!FMLEnvironment.isProduction()`; exercised by [DevGameTests].
 */
object DevContent {
    @JvmField
    val BLOCKS: DeferredRegister.Blocks = DeferredRegister.createBlocks(Femtocraft.ID)

    @JvmField
    val TYPES: DeferredRegister<BlockEntityType<*>> = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Femtocraft.ID)

    private val devTypes = mutableListOf<DeferredHolder<BlockEntityType<*>, BlockEntityType<DevPowerNodeBlockEntity>>>()

    class DevNode(
        val block: DeferredBlock<FemtoEntityBlock<DevPowerNodeBlockEntity>>,
        val type: DeferredHolder<BlockEntityType<*>, BlockEntityType<DevPowerNodeBlockEntity>>,
    )

    private fun node(name: String, rules: PowerNodeRules): DevNode {
        var typeRef: DeferredHolder<BlockEntityType<*>, BlockEntityType<DevPowerNodeBlockEntity>>? = null
        val block = BLOCKS.registerBlock("dev_${name}_node", { p: BlockBehaviour.Properties -> FemtoEntityBlock(p) { typeRef!!.get() } })
        val type = TYPES.register("dev_${name}_node") { ->
            BlockEntityType({ pos: BlockPos, state: BlockState -> DevPowerNodeBlockEntity(typeRef!!.get(), pos, state, rules) }, block.get())
        }
        typeRef = type
        devTypes += type
        return DevNode(block, type)
    }

    @JvmField
    val TRANSFER_NODE = node("transfer", PowerNodeRules.TRANSFER)

    @JvmField
    val DIFFUSION_NODE = node("diffusion", PowerNodeRules.DIFFUSION)

    @JvmField
    val DIFFUSION_TARGET_NODE = node("diffusion_target", PowerNodeRules.DIFFUSION_TARGET)

    @JvmField
    val DIRECT_NODE = node("direct", PowerNodeRules.DIRECT)

    @JvmField
    val GENERATION_NODE = node("generation", PowerNodeRules.GENERATION)

    fun register(modBus: IEventBus) {
        BLOCKS.register(modBus)
        TYPES.register(modBus)
        DevGameTests.register(modBus)
        modBus.addListener { event: RegisterCapabilitiesEvent ->
            devTypes.forEach { ModuleCapabilities.registerBlockEntity(event, it.get()) }
        }
    }
}

/**
 * A bare power node with its own storage. Right-click prints its links (the 1.7.10 `TileNodeTest` debug output).
 */
class DevPowerNodeBlockEntity(type: BlockEntityType<*>, pos: BlockPos, state: BlockState, rules: PowerNodeRules) :
    FemtoBlockEntity(type, pos, state) {
    val node = FragPowerNode(rules, OwnPowerStorage(1000.0) { setChanged() })

    init {
        fragList.addFragment(node)
    }

    override fun onServerLoad() = node.onServerLoad()

    override fun onServerUnload() = node.onServerUnload()

    override fun onUse(player: Player): InteractionResult {
        if (!isServer) return InteractionResult.SUCCESS
        player.sendSystemMessage(Component.literal("Location is: ${node.getNodeLoc()}"))
        player.sendSystemMessage(Component.literal("Parent is: ${node.getParentLoc()}"))
        val children = node.getChildren()
        player.sendSystemMessage(Component.literal("Children(${children?.size ?: "leaf"}):"))
        children?.forEach { player.sendSystemMessage(Component.literal("    ${it.getNodeLoc()}")) }
        player.sendSystemMessage(Component.literal("Power: ${node.getPowerCurrent()}/${node.getPowerMax()}"))
        return InteractionResult.SUCCESS
    }
}
