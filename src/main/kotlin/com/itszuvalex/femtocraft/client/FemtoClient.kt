package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.industry.FemtoFluids
import com.itszuvalex.femtocraft.industry.IndustryContent
import com.itszuvalex.femtocraft.power.PowerContent
import net.minecraft.client.renderer.block.FluidModel
import net.minecraft.client.resources.model.sprite.Material
import net.minecraft.resources.Identifier
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent

/**
 * Client-only registrations. Only referenced when running on a client.
 */
object FemtoClient {
    fun register(modBus: IEventBus) {
        modBus.addListener(::registerScreens)
        modBus.addListener(::registerFluidModels)
    }

    private fun registerFluidModels(event: RegisterFluidModelsEvent) {
        event.register(
            FluidModel.Unbaked(Material(id("block/blockgrittyslurry_still")), Material(id("block/blockgrittyslurry_flow")), null, null),
            FemtoFluids.GRITTY_SLURRY, FemtoFluids.FLOWING_GRITTY_SLURRY,
        )
    }

    private fun id(path: String) = Identifier.fromNamespaceAndPath(Femtocraft.ID, path)

    private fun registerScreens(event: RegisterMenuScreensEvent) {
        event.register(PowerContent.CRYSTAL_MOUNT_MENU.get(), ::CrystalMountScreen)
        event.register(PowerContent.CRYSTAL_MACHINE_MENU.get(), ::CrystalMachineScreen)
        event.register(IndustryContent.MACHINE_MENU.get(), ::MachineScreen)
        event.register(IndustryContent.FRAME_MENU.get(), ::FrameScreen)
        event.register(IndustryContent.FRAME_SELECTION_MENU.get(), ::FrameSelectionScreen)
        event.register(IndustryContent.GERMINATION_CHAMBER_MENU.get(), ::GerminationChamberScreen)
        event.register(IndustryContent.FOCUSING_CHAMBER_MENU.get(), ::FocusingChamberScreen)
    }
}
