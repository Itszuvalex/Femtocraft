package com.itszuvalex.femtocraft.client

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.industry.FemtoFluids
import com.itszuvalex.femtocraft.industry.IndustryContent
import com.itszuvalex.femtocraft.logistics.LogisticsContent
import com.itszuvalex.femtocraft.nanite.NaniteContent
import com.itszuvalex.femtocraft.power.PowerContent
import net.minecraft.client.renderer.entity.ThrownItemRenderer
import net.neoforged.neoforge.client.event.EntityRenderersEvent
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
        modBus.addListener(FemtoTints::register)
        modBus.addListener(ObjParts::register)
        modBus.addListener(FemtoRenderers::register)
        modBus.addListener(FemtoParticleProviders::register)
        modBus.addListener(NaniteOverlay::register)
        modBus.addListener(HostOverlay::register)
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(Previews::submit)
        modBus.addListener { event: EntityRenderersEvent.RegisterRenderers -> event.registerEntityRenderer(NaniteContent.NANO_LASH_ENTITY.get(), ::ThrownItemRenderer) }
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
        event.register(PowerContent.CRYO_BASE_MENU.get(), ::CryoChargingBaseScreen)
        event.register(PowerContent.ATMOSPHERIC_BASE_MENU.get(), ::AtmosphericChargingBaseScreen)
        event.register(IndustryContent.MACHINE_MENU.get(), ::MachineScreen)
        event.register(IndustryContent.FRAME_MENU.get(), ::FrameScreen)
        event.register(IndustryContent.FRAME_SELECTION_MENU.get(), ::FrameSelectionScreen)
        event.register(IndustryContent.GERMINATION_CHAMBER_MENU.get(), ::GerminationChamberScreen)
        event.register(IndustryContent.FOCUSING_CHAMBER_MENU.get(), ::FocusingChamberScreen)
        event.register(com.itszuvalex.femtocraft.archive.ArchiveContent.ARCHIVE_MENU.get(), ::ArchiveScreen)
        event.register(com.itszuvalex.femtocraft.archive.ArchiveContent.CODEX_MENU.get(), ::CodexScreen)
        event.register(NaniteContent.NANITE_MACHINE_MENU.get(), ::NaniteMachineScreen)
        event.register(LogisticsContent.ITEM_REPOSITORY_MENU.get(), ::ItemRepositoryScreen)
        event.register(LogisticsContent.FLUID_REPOSITORY_MENU.get(), ::FluidRepositoryScreen)
        event.register(LogisticsContent.NANITE_REPOSITORY_MENU.get(), ::NaniteRepositoryScreen)
        event.register(LogisticsContent.CONDUIT_MENU.get(), ::ConduitScreen)
        event.register(LogisticsContent.NANO_PACK_MENU.get(), ::NanoPackScreen)
        event.register(com.itszuvalex.femtocraft.computation.ComputationContent.MAINFRAME_MENU.get(), ::MainframeScreen)
    }
}
