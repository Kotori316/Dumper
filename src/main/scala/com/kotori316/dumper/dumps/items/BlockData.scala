package com.kotori316.dumper.dumps.items

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.{BlockItem, ItemStack}
import net.minecraft.world.level.EmptyBlockGetter
import net.minecraft.world.level.block.Block
import net.minecraftforge.registries.ForgeRegistries

import scala.annotation.nowarn
import scala.jdk.CollectionConverters._
import scala.jdk.StreamConverters._

case class BlockData(block: Block, stack: ItemStack) {
  def name: String = if (stack.isEmpty) {
    block.getName.getString
  } else {
    stack.getHoverName.getString
  }

  def registryName: ResourceLocation = ForgeRegistries.BLOCKS.getKey(block)

  def itemClass: String = stack.getItem.getClass match {
    case c if c == classOf[BlockItem] => ""
    case c => c.getName.replace("net.minecraft.world.item.", "")
  }

  //noinspection ScalaDeprecation,deprecation
  @nowarn
  def tags: Seq[ResourceLocation] = block.builtInRegistryHolder().tags().toScala(Seq).map(_.location)

  def tagStrings: String = tags.mkString(", ")

  def properties: String = block.getStateDefinition.getProperties.asScala.map(_.getName).mkString(", ")

  def hardness: Float = block.defaultBlockState().getDestroySpeed(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)
}
