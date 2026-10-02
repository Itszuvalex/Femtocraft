package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.industry.ProcessingMachineBlockEntity
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.menu.MenuSync
import com.itszuvalex.itszulib.menu.MenuSyncs
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player

/**
 * Nanite extractor and infuser: their slots, battery, progress and nanite tank, plus the player's own nanites. Actions
 * [ACTION_FILL] (player to machine) and [ACTION_DRAIN] (machine to player) move one nanite of each strain, replacing
 * v3's `MessageFillNanite`/`MessageDrainNanite` (DECISIONS D11). Port of v3's `ContainerNaniteExtractor`/`Infuser` and
 * `GuiNaniteTank`.
 */
class NaniteMachineMenu(containerId: Int, inventory: Inventory, be: ProcessingMachineBlockEntity?) :
    FemtoMenu<ProcessingMachineBlockEntity>(NaniteContent.NANITE_MACHINE_MENU.get(), containerId, inventory, be) {
    @JvmField
    var battery = com.itszuvalex.itszulib.menu.EnergyView()

    var progress = 0.0
    var tank: List<NaniteStack> = listOf()
    var playerTank: List<NaniteStack> = listOf()

    init {
        val storage = be?.inventory ?: IItemStorage.Empty
        when (be) {
            is NaniteExtractorBlockEntity -> addStorageSlots(storage, 56, 35, count = 1)
            is NaniteInfuserBlockEntity -> {
                addStorageSlots(storage, 56, 35, count = 1)
                addStorageSlots(storage, 116, 35, first = 1, count = 1, output = true)
            }
            else -> {}
        }
        addPlayerInventorySlots(inventory)
        if (be != null) {
            battery = syncEnergy { be.battery }
            addSync(MenuSyncs.double(be::progressFraction) { progress = it })
            addSync(MenuSync({ (be as NaniteMachine).naniteTank.contents() }, { tank = it }, LIST))
        }
        addSync(MenuSync({ PlayerNanites.tank(inventory.player).contents() }, { playerTank = it }, LIST))
    }

    override fun handleAction(player: Player, action: Int, data: Int): Boolean {
        val tank = (blockEntity as? NaniteMachine)?.naniteTank ?: return false
        when (action) {
            ACTION_FILL -> PlayerNanites.fill(player, tank)
            ACTION_DRAIN -> PlayerNanites.drain(player, tank)
            else -> return false
        }
        return true
    }

    companion object {
        const val ACTION_FILL = 0
        const val ACTION_DRAIN = 1

        @JvmField
        val LIST: StreamCodec<RegistryFriendlyByteBuf, List<NaniteStack>> = NaniteStack.STREAM_CODEC.apply(ByteBufCodecs.list())
    }
}
