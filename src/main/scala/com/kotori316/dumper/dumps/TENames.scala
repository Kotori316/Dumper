package com.kotori316.dumper.dumps

import java.util

import cpw.mods.modlauncher.Launcher
import net.minecraft.core.{BlockPos, Direction}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.{BlockEntity, BlockEntityType}
import net.minecraftforge.common.capabilities.{Capability, CapabilityManager}
import net.minecraftforge.fml.util.ObfuscationReflectionHelper
import net.minecraftforge.registries.ForgeRegistries

import scala.jdk.CollectionConverters._
import scala.util.control.Exception._
import scala.util.{Failure, Success, Try}

object TENames extends FastDumps[BlockEntity] {
  override val configName: String = "OutputTileentity"
  override val fileName: String = "tileentity"

  private[this] final val field_Capacity = classOf[CapabilityManager].getDeclaredField("providers")
  field_Capacity.setAccessible(true)
  private[this] final val fieldValidBlocks = {
    if (Launcher.INSTANCE != null) {
      ObfuscationReflectionHelper.findField(classOf[BlockEntityType[_]], "f_58915_")
    } else {
      val f = classOf[BlockEntityType[_]].getDeclaredField("validBlocks")
      f.setAccessible(true)
      f
    }
  }

  override def content(filters: Seq[Filter[BlockEntity]]): Seq[String] = {
    val caps = field_Capacity.get(CapabilityManager.INSTANCE).asInstanceOf[util.Map[_, Capability[_]]].asScala.clone().values.toSeq
    Seq("------Capabilities------") ++ contentCapability(caps) ++ Seq("", "------BlockEntities------") ++ contentBlockEntity(caps)
  }

  private[this] def simpleName(cap: Capability[_]): String = {
    val s = cap.getName
    val a = s.lastIndexOf('.')
    if (a == -1) {
      s
    } else {
      s.drop(a + 1)
    }
  }

  private[this] def contentCapability(caps: Seq[Capability[_]]): Seq[String] = {
    caps.map(_.getName).sorted
  }

  private[this] def contentBlockEntity(caps: Seq[Capability[_]]): Seq[String] = {
    ForgeRegistries.BLOCK_ENTITY_TYPES.getEntries.asScala
      .toSeq
      .sortBy(_.getKey)
      .flatMap(e => contentOneBlockEntity(caps, e.getValue, e.getKey.location()))
  }

  private[this] def contentOneBlockEntity(caps: Seq[Capability[_]], entityType: BlockEntityType[_], name: ResourceLocation): Seq[String] = {
    val blocks = fieldValidBlocks.get(entityType).asInstanceOf[util.Set[Block]].asScala
    val instance: Try[BlockEntity] = allCatch withTry entityType.create(BlockPos.ZERO, blocks.head.defaultBlockState()).asInstanceOf[BlockEntity]
    val clazz = instance.map(_.getClass).getOrElse(classOf[BlockEntity])
    val tileCaps = instance.map(t => caps.filter(c => t.getCapability(c, Direction.UP).isPresent))

    val nameRow = "%s : %s".formatted(name, clazz.getName)
    val statesRow = blocks.map(ForgeRegistries.BLOCKS.getKey).map(_.toString).mkString("\t", ", ", "")
    val capsRows = tileCaps match {
      case Failure(exception) => Seq("\t" + exception.toString)
      case Success(c) => c.map(simpleName).map(s => "\t" + s)
    }
    nameRow +: statesRow +: capsRows
  }
}
