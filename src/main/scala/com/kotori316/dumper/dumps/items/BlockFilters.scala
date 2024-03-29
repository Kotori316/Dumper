package com.kotori316.dumper.dumps.items

import java.nio.file.{Files, Path, Paths}
import java.util.regex.Pattern

import com.kotori316.dumper.Dumper
import com.kotori316.dumper.dumps.Filter
import net.minecraft.tags.{BlockTags, ItemTags}
import net.minecraft.world.item.ItemStack

import scala.collection.mutable
import scala.jdk.CollectionConverters._

trait SFilter extends Filter[BlockData] {

  private[this] final val short = mutable.Buffer.empty[String]
  private[this] final val unique = mutable.Buffer.empty[String]

  val out: Path

  def accept(block: BlockData): Boolean

  override final def addToList(v: BlockData): Boolean = {
    if (accept(v)) {
      short += v.block.getName.getString + ": " + v.tagStrings
      unique += v.registryName.toString
      true
    } else false
  }

  override final def writeToFile(): Unit = {
    val s = short ++ Seq("", "") ++ unique.distinct ++ Seq(unique.distinct.reduceOption((s1, s2) => s1 + ", " + s2).getOrElse(""), short.size.toString)
    Files.write(out, s.asJava)
  }
}

class OreFilter extends SFilter {
  private[this] final val oreDicPattern = Pattern.compile("(forge:ores)|(.*:ores/.*)|(.*_ores.*)")
  override val out: Path = Paths.get(Dumper.modID, "ore.txt")

  override def accept(block: BlockData): Boolean = {
    block.tags.exists(n => oreDicPattern.matcher(n.toString).matches())
  }
}

class WoodFilter extends SFilter {
  private[this] final val woodPATTERN = Pattern.compile(".* Wood")
  private[this] final val woodDicPATTERN = Pattern.compile("^(.*:logs)|(wood[A-Z].*)")

  override val out: Path = Paths.get(Dumper.modID, "wood.txt")

  override def accept(data: BlockData): Boolean = {
    val block = data.block
    if (block.defaultBlockState().is(BlockTags.LOGS) || new ItemStack(block).is(ItemTags.LOGS))
      return true
    if (woodPATTERN.matcher(block.getName.getString).matches)
      return true
    data.tags.exists(n => woodDicPATTERN.matcher(n.toString).matches)
  }
}

class LeaveFilter extends SFilter {
  override val out: Path = Paths.get(Dumper.modID, "leave.txt")

  override def accept(data: BlockData): Boolean = {
    val block = data.block
    block.defaultBlockState().is(BlockTags.LEAVES) || new ItemStack(block).is(ItemTags.LEAVES)
  }
}
