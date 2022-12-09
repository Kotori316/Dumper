package com.kotori316.dumper.dumps.items

import com.kotori316.dumper.dumps.{Dumps, Filter, Formatter}
import net.minecraft.server.MinecraftServer
import net.minecraftforge.registries.ForgeRegistries

import scala.jdk.javaapi.CollectionConverters

object ItemsDump extends Dumps[ItemData] {
  override val configName = "OutputItems"
  override val fileName = "items"
  final val formatter = new Formatter[ItemData](
    Seq("Index", "ID", "-Name", "-RegistryName", "-Tags"),
    Seq(_.index, _.id, _.displayName, d => ForgeRegistries.ITEMS.getKey(d.item), _.tags)
  )

  override def getFilters: Seq[Filter[ItemData]] = Seq(new PickaxeFilter, new AxeFilter, new ShovelFilter, new SwordFilter)

  override def content(filters: Seq[Filter[ItemData]], server: MinecraftServer): Seq[String] = {
    val items = ForgeRegistries.ITEMS
    val stacks = CollectionConverters.asScala(items)
      .flatMap { item =>
        Seq(item.getDefaultInstance)
      }
      .zipWithIndex
      .map { case (stack, i) =>
        val data = ItemData(i + 1, stack)
        filters.find(_.addToList(data))
        data
      }.toSeq
    formatter.format(stacks)
  }

}
