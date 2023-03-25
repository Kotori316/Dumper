package com.kotori316.dumper.dumps

import net.minecraft.ChatFormatting
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.resources.{ResourceKey, ResourceLocation}
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraftforge.registries.ForgeRegistries

import scala.jdk.CollectionConverters._

object EnchantmentNames extends FastDumps[Enchantment] {
  override val configName: String = "OutputEnchantments"
  override val fileName: String = "enchantment"
  final val formatter = new Formatter[EData](Seq("ID", "-name", "-Registry Name", "MaxLevel", "Rarity", "Treasure"),
    Seq(_.id, _.translatedName, _.name, _.e.getMaxLevel, _.e.getRarity, _.e.isTreasureOnly)
  )

  override def content(filters: Seq[Filter[Enchantment]]): Seq[String] = {
    implicit val vanillaRegistry: Registry[Enchantment] = ForgeRegistries.ENCHANTMENTS.getSlaveMap(WRAPPER_ID, classOf[Registry[Enchantment]])
    val data = ForgeRegistries.ENCHANTMENTS.getEntries.asScala.toSeq.map(EData.apply).sorted
    val seq = formatter.format(data)
    seq
    //    val mn: Int = data.map(_.translatedName.length).max
    //    val mi: Int = data.map(_.name.toString.length).max
    //    //      id    name  Resource maxLevel rarity treasure
    //    val f = s"%3s : %-${mn}s : %-${mi}s : %s : %-${lengthOfRarity}s : %s"
    //    f.format("ID", "name", "Registry Name", "MaxLevel", "Rarity", "Treasure") +: data
    //      .map(e => f.format(e.id, e.translatedName, e.name, e.e.getMaxLevel, e.e.getRarity, e.e.isTreasureEnchantment))
  }

  case class EData(name: ResourceLocation, e: Enchantment, id: Int) extends Ordered[EData] {
    override def compare(that: EData): Int = this.id compare that.id

    def translatedName: String = ChatFormatting.stripFormatting(Component.translatable(e.getDescriptionId).getString)
  }

  object EData {
    def apply(e: java.util.Map.Entry[ResourceKey[Enchantment], Enchantment])(implicit r: Registry[Enchantment]): EData = {
      new EData(e.getKey.location(), e.getValue, r.getId(e.getValue))
    }
  }

}
