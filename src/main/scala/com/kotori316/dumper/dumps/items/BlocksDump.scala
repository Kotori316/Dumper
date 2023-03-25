package com.kotori316.dumper.dumps.items

import com.kotori316.dumper.dumps.{Dumps, Filter, Formatter}
import net.minecraft.server.MinecraftServer
import net.minecraftforge.registries.ForgeRegistries

import scala.jdk.CollectionConverters._

object BlocksDump extends Dumps[BlockData] {
  override val configName: String = "outputBlocks"
  override val fileName: String = "blocksOutput"

  override def getFilters: Seq[SFilter] = Seq(new OreFilter, new WoodFilter, new LeaveFilter)

  private final val formatter = new Formatter[BlockData](
    Seq("-Name", "-RegistryName", "Hardness", "-Item Class", "-Properties", "-Tag"),
    Seq(_.name, _.registryName, _.hardness, _.itemClass, _.properties, _.tagStrings)
  )

  override def content(filters: Seq[Filter[BlockData]], server: MinecraftServer): Seq[String] = {
    val blockList = for {
      block <- ForgeRegistries.BLOCKS.asScala
      stack <- Seq(block.asItem.getDefaultInstance)
    } yield BlockData(block, stack)
    blockList.foreach(b => filters.foreach(_.addToList(b)))
    formatter.format(blockList.toSeq)
  }
}
