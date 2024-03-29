package com.kotori316.dumper.dumps.items

import com.kotori316.dumper.dumps.{Dumps, Filter, Formatter}
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.tags.{BlockTags, TagKey}
import net.minecraft.world.level.block.Block
import net.minecraftforge.registries.ForgeRegistries

import scala.annotation.nowarn
import scala.jdk.CollectionConverters._

object MineableDump extends Dumps[TagKey[_]] {
  override val configName: String = "OutputMineable"
  override val fileName: String = "mineable"
  private final val tags = Seq(
    BlockTags.MINEABLE_WITH_AXE,
    BlockTags.MINEABLE_WITH_HOE,
    BlockTags.MINEABLE_WITH_PICKAXE,
    BlockTags.MINEABLE_WITH_SHOVEL,
  )
  final val formatter = new Formatter[Entry](
    Seq("-name"),
    Seq(_.registryName)
  )

  @nowarn //noinspection ScalaDeprecation,deprecation
  override def content(filters: Seq[Filter[TagKey[_]]], server: MinecraftServer): Seq[String] = {
    for {
      tag <- tags
      entries = BuiltInRegistries.BLOCK.getTagOrEmpty(tag).asScala.map(_.value).map(Entry).toSeq
      string <- s"# ${tag.location}" +: formatter.format(entries) :+ System.lineSeparator()
    } yield string
  }

  case class Entry(block: Block) {
    def registryName: ResourceLocation = ForgeRegistries.BLOCKS.getKey(block)
  }
}
