package com.kotori316.dumper.dumps

import net.minecraft.block.Blocks
import net.minecraft.fluid.Fluid
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.registries.ForgeRegistries

import scala.jdk.CollectionConverters._

object FluidNames extends Dumps[Fluid] {
  override val configName: String = "OutputFluid"
  override val fileName: String = "fluid"

  private[this] final val luminosity = "Luminosity"
  private[this] final val density = "Density"
  private[this] final val temperature = "Temperature"
  private[this] final val viscosity = "Viscosity"
  private[this] final val gaseous = "Gaseous"
  private[this] final val rarity = "Rarity"
  private[this] final val color = "Color"
  private[this] final val hasBlock = "hasBlock"
  final val formatter = new Formatter[Fluid](Seq("-RegistryName", "-Name", luminosity, density, temperature, viscosity, gaseous, rarity, color, hasBlock),
    Seq(_.getRegistryName, f => new TranslationTextComponent(f.getAttributes.getTranslationKey).getFormattedText, _.getAttributes.getLuminosity, _.getAttributes.getDensity,
      _.getAttributes.getTemperature.toString + " [K]", _.getAttributes.getViscosity, _.getAttributes.isGaseous, _.getAttributes.getRarity.toString,
      _.getAttributes.getColor.toHexString, _.getDefaultState.getBlockState != Blocks.AIR.getDefaultState))

  override def content(filters: Seq[Filter[Fluid]]): Seq[String] = {
    val fluids = ForgeRegistries.FLUIDS.asScala
    formatter.format(fluids.toSeq)
  }

}
