package com.itszuvalex.femtocraft.util

import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Christopher Harris (Itszuvalex) on 2/1/2016.
  */
object ItemFilter {
  val FILTER_COMPOUND_KEY = "ItemFilter"
  val FILTER_RULES_KEY    = "Rules"
  val FILTER_TYPE_KEY     = "FilterType"
  val FILTER_DATA_KEY     = "FilterData"
}

class ItemFilter extends IItemFilter with INBTSerializable[NBTTagCompound] {
  private val filters = new ArrayBuffer[IItemFilterRule]()

  override def filterItem(item: ItemStack): Boolean = getFilterRules.forall(_.itemMatches(item))

  override def getFilterRules = filters

  def addFilter(filter: IItemFilterRule) = filters += filter

  def removeFilter(filter: IItemFilterRule) = filters -= filter

  override def serializeNBT(): NBTTagCompound = {
    NBTCompound(ItemFilter.FILTER_COMPOUND_KEY ->
                NBTCompound(ItemFilter.FILTER_RULES_KEY -> NBTList(filters.map { filter =>
                  NBTCompound(ItemFilter.FILTER_TYPE_KEY -> filter.ruleType,
                              ItemFilter.FILTER_DATA_KEY -> NBTCompound(filter))
                                                                               }))
               )
  }

  override def deserializeNBT(compound: NBTTagCompound): Unit = {
    compound.NBTCompound(ItemFilter.FILTER_COMPOUND_KEY) { comp =>
      filters.clear()
      filters ++= comp.NBTList(ItemFilter.FILTER_RULES_KEY).flatMap { filterMeta =>
        val filterType = filterMeta.String(ItemFilter.FILTER_TYPE_KEY)
        ItemFilterRule.getFilterClass(filterType) match {
          case None => None
          case Some(clazz) =>
            try {
              val instance = clazz.newInstance()
              filterMeta.NBTCompound(ItemFilter.FILTER_DATA_KEY) { comp => instance.deserializeNBT(comp); Unit }
              Some(instance)
            }
            catch {
              case ignored: Throwable =>
                ignored.printStackTrace()
                None
            }
        }
                                                                    }
                                                         }
  }
}
