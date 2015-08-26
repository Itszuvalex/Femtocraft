//package com.itszuvalex.femtocraft.render
//
//import com.itszuvalex.femtocraft.core.Power.IPowerCrystal
//import com.itszuvalex.itszulib.util.Color
//import net.minecraft.client.renderer.entity.RenderItem
//import net.minecraft.client.renderer.{ItemRenderer, Tessellator}
//import net.minecraft.item.ItemStack
//import net.minecraft.util.IIcon
//import net.minecraftforge.client.IItemRenderer
//import net.minecraftforge.client.IItemRenderer.{ItemRenderType, ItemRendererHelper}
//import org.lwjgl.opengl.GL11
//
//import scala.collection._
//
///**
// * Created by Christopher on 8/26/2015.
// */
//object CrystalItemRenderer {
//
//}
//
//class CrystalItemRenderer extends IItemRenderer {
//  private val renderItem = new RenderItem()
//
//  override def handleRenderType(item: ItemStack, rtype: ItemRenderType) = rtype match {
//    //    case ItemRenderType.INVENTORY => true
//    //    case ItemRenderType.ENTITY    => true
//    case ItemRenderType.FIRST_PERSON_MAP => false
//    case _                               => true
//  }
//
//  override def shouldUseRenderHelper(rtype: ItemRenderType, item: ItemStack, helper: ItemRendererHelper) = rtype match {
//    case ItemRenderType.ENTITY => true
//    case _                     => false
//  }
//
//  override def renderItem(rtype: ItemRenderType, item: ItemStack, data: AnyRef*): Unit = {
//    item.getItem match {
//      case crystal: IPowerCrystal =>
//        CrystalItemRenderer.getIcon(crystal.getType(item)) match {
//          case Some(icon) =>
//            val color = new Color(crystal.getColor(item))
////            GL11.glColor3b(color.red, color.green, color.blue)
//            Tessellator.instance.setColorRGBA(color.red.toInt & 255,
//                                              color.green.toInt & 255,
//                                              color.blue.toInt & 255,
//                                              color.alpha.toInt & 255)
//            rtype match {
//              case ItemRenderType.INVENTORY => renderItem.renderIcon(0, 0, icon, 16, 16)
//              case _                        =>
//                if (rtype == ItemRenderType.ENTITY) Tessellator.instance.addTranslation(-.5f, 0,0)
//                ItemRenderer.renderItemIn2D(Tessellator.instance,
//                                            icon.getMaxU,
//                                            icon.getMinV,
//                                            icon.getMinU,
//                                            icon.getMaxV,
//                                            icon.getIconWidth,
//                                            icon.getIconHeight,
//                                            1f / 16f)
//                if (rtype == ItemRenderType.ENTITY) Tessellator.instance.addTranslation(.5f, 0, 0)
//            }
//          case _          =>
//        }
//      case _                      =>
//    }
//  }
//}
