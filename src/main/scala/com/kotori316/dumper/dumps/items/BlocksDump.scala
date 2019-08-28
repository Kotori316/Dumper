package com.kotori316.dumper.dumps.items

import com.kotori316.dumper.dumps.{Dumps, Filter}
import net.minecraft.block.Block
import net.minecraft.item.{BlockItem, ItemStack, Items}
import net.minecraft.tags.BlockTags
import net.minecraft.util.registry.Registry
import net.minecraft.util.{NonNullList, ResourceLocation}
import net.minecraftforge.registries.ForgeRegistries

import scala.collection.JavaConverters._

object BlocksDump extends Dumps[Block] {
  override val configName: String = "outputBlocks"
  override val fileName: String = "blocksOutput"

  override def getFilters: Seq[SFilter] = Seq(new OreFilter, new WoodFilter, new LeaveFilter)

  override def output(filters: Seq[Filter[Block]]): Unit = {
    super.output(filters)
    if (isEnabled) {
      filters.foreach(_.writeToFile())
    }
  }

  override def content(filters: Seq[Filter[Block]]): Seq[String] = {
    val vanillaRegistry: Registry[Block] = ForgeRegistries.BLOCKS.getSlaveMap(WRAPPER_ID, classOf[Registry[Block]])

    ForgeRegistries.BLOCKS.asScala.map(b => BD.apply(b, vanillaRegistry.getId(b))).flatMap(_.stacks).map { e: BlockStack =>
      filters.find(_.addToList(e.bd.block, e.o, e.stack.getDisplayName.getUnformattedComponentText, e.bd.name.toString))
      e.o
    }.zipWithIndex.map { case (s, i) => "%4d : %s".format(i, s) }.toSeq
  }

  def oreNameSeq(block: Block): Iterable[ResourceLocation] = {
    BlockTags.getCollection.getTagMap.asScala.collect { case (key, tag) if tag.contains(block) => key }
  }

  case class BD(block: Block, id: Int) {
    val item = block.asItem()
    val name = block.getRegistryName
    val blocks = NonNullList.create[ItemStack]()
    block.fillItemGroup(item.getGroup, blocks)

    def stacks: Seq[BlockStack] = {
      if (blocks.isEmpty) {
        return Nil
      }
      val f :: rest = blocks.asScala.toList
      FBS(this, f) :: rest.map(s => BlockStack(this, s))
    }
  }

  val f = "%4d : %3s : %s"

  sealed trait BlockStack {
    val o: String
    val bd: BD
    val stack: ItemStack
  }

  object BlockStack {
    def apply(p_bd: BD, p_stack: ItemStack): BlockStack = {
      if (!p_stack.isEmpty)
        NNStack(p_bd, p_stack)
      else {
        new BlockStack {
          override val o: String = f.format(p_bd.id, p_stack.getDamage, "")
          override val bd: BD = p_bd
          override val stack: ItemStack = ItemStack.EMPTY
        }
      }
    }
  }

  case class NNStack(bd: BD, stack: ItemStack) extends BlockStack {
    val o = f.format(bd.id, stack.getDamage, stack.getDisplayName.getUnformattedComponentText) + oreName(stack)
  }

  case class FBS(bd: BD, stack: ItemStack) extends BlockStack {

    def classString = {
      val clazz = bd.item.getClass
      if (clazz != classOf[BlockItem])
        " : " + clazz.getName
      else ""
    }

    val o: String =
      if (stack.isEmpty) {
        if (bd.item == Items.AIR)
          f.format(bd.id, if (false /*bd.item.getHasSubtypes*/ ) stack.getDamage else "", bd.block.getTranslationKey) + " : " + bd.name
        else
          f.format(bd.id, if (false /*bd.item.getHasSubtypes*/ ) stack.getDamage else "", bd.item.getDisplayName(stack)) + " : " + bd.name
      } else {
        f.format(bd.id, if (false /*bd.item.getHasSubtypes*/ ) stack.getDamage else "", stack.getDisplayName.getUnformattedComponentText) +
          classString + " : " + bd.name + oreName(stack)
      }
  }

}
