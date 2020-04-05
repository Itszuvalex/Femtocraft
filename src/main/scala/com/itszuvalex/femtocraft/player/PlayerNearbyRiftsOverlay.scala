package com.itszuvalex.femtocraft.player

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.worldgen.IRift
import com.itszuvalex.femtocraft.worldgen.{FemtocraftRiftTracker, Rift}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.gui.{GuiFlowLayout, GuiLabel}
import net.minecraft.client.Minecraft
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.opengl.GL11

import scala.collection.mutable

@SideOnly(Side.CLIENT)
object PlayerNearbyRiftsOverlay {
  val range   = 64f
  var enabled = true
}

@SideOnly(Side.CLIENT)
class PlayerNearbyRiftsOverlay {
  lazy val mc = Minecraft.getMinecraft

  @SubscribeEvent
  def renderOverlay(event: RenderGameOverlayEvent.Post): Unit = {
    if (event.isCanceled || event.getType != ElementType.EXPERIENCE)
      return

    val res    = event.getResolution
    val factor = mc.gameSettings.guiScale match {
      case 0 => 1
      case s => s
    }


    if (!PlayerNearbyRiftsOverlay.enabled)
      return

    val rifts = getRifts
    rifts.sortBy(rift => getPlayerLoc.distSqr(rift.location))

    GL11.glPushMatrix()
    GL11.glColor4f(1f, 1f, 1f, 1f)
    GL11.glDisable(GL11.GL_LIGHTING)
    val scale: Float = 1 / 3f
    val topString    = s"Nearby Rifts(${TextFormatting.DARK_GREEN}${PlayerNearbyRiftsOverlay.range}${TextFormatting.RESET}m):"

    val width  = mc.fontRenderer.getStringWidth(topString) * factor
    val height = res.getScaledHeight

    val layout: GuiFlowLayout = new GuiFlowLayout(2, 2, width - 4, height - 4)
    layout.add(new GuiLabel(0, 0, width, mc.fontRenderer.FONT_HEIGHT, () => topString, scale))
    rifts.foreach { rift =>
      val traits         = rift.traits
      val riftLayout     = new GuiFlowLayout(0, 0, width, (traits.size + 1) * mc.fontRenderer.FONT_HEIGHT)
      val stability      = rift.stability
      val stabilityColor = stability match {
        case _ if stability >= (Rift.maxStability * 2f) / 3f => TextFormatting.GREEN
        case _ if stability >= Rift.maxStability / 3f => TextFormatting.YELLOW
        case _ => TextFormatting.RED
      }
      val distance       = getPlayerLoc.dist(rift.location)
      val distColor      = distance match {
        case _ if distance <= PlayerNearbyRiftsOverlay.range / 3f => TextFormatting.GREEN
        case _ if distance <= (2f * PlayerNearbyRiftsOverlay.range) / 3f => TextFormatting.BLUE
        case _ => TextFormatting.RED
      }
      val riftText       = new GuiLabel(0, 0, width, mc.fontRenderer.FONT_HEIGHT, () => f"Rift($stabilityColor$stability${TextFormatting.RESET}): $distColor$distance%.2f${TextFormatting.RESET}m", scale)
      riftLayout.add(riftText)
      traits.foreach { riftTrait =>
        val riftTraitText = new GuiLabel(0, 0, width, mc.fontRenderer.FONT_HEIGHT, () => s"\t${riftTrait.name}", scale)
        riftLayout.add(riftTraitText)
      }
      layout.add(riftLayout)
    }

    layout.renderUpdate(0, 0, 0, 0, 0)
    layout.render(0, 0, 0, 0, 0)
    GL11.glPopMatrix()
  }

  def getRifts: mutable.Buffer[IRift] = {
    val locs  = FemtocraftRiftTracker.instance.riftLocs.getLocationsInRange(getPlayerLoc, PlayerNearbyRiftsOverlay.range)
    val rifts = locs.flatMap(l => l.getChunk(false).map(_.toMinecraft).map(_.getCapability(Capabilities.CHUNK_RIFT, null)).flatMap(_.getRiftAtLocation(l)))
    rifts.toBuffer
  }

  def getPlayerLoc = Loc4(mc.player.getPosition, mc.world.provider.getDimension)
}
