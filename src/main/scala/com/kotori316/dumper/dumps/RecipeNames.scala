package com.kotori316.dumper.dumps

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.item.crafting.Recipe

import scala.annotation.nowarn
import scala.jdk.javaapi.CollectionConverters

object RecipeNames extends Dumps[Recipe[_]] {
  override val configName: String = "OutputRecipeNames"
  override val fileName: String = "recipes"
  private final val formatter = new Formatter[RecipeData](
    Seq("-name", "-type", "-output", "-group", "isDynamic"),
    Seq(_.getId, _.recipeType, _.getRecipeOutput, _.getGroup, _.isDynamic)
  )

  override def content(filters: Seq[Filter[Recipe[_]]], server: MinecraftServer): Seq[String] = {
    val comparator = Ordering.by((r: RecipeData) => r.recipeType) orElseBy (_.getGroup) orElseBy (_.getId)
    val all = CollectionConverters.asScala(server.getRecipeManager.getRecipes)
      .toSeq
      .map(RecipeData)
      .sorted(comparator)
    formatter.format(all)
  }

  private case class RecipeData(private val recipe: Recipe[_]) {
    @nowarn //noinspection ScalaDeprecation,deprecation
    val recipeType: String = BuiltInRegistries.RECIPE_TYPE.getKey(recipe.getType).toString

    def getId: ResourceLocation = recipe.getId

    def getRecipeOutput: String = {
      val stack = recipe.getResultItem
      if (stack.isEmpty) {
        "Undefined"
      } else {
        stack.toString
      }
    }

    def getGroup: String = recipe.getGroup

    def isDynamic: Boolean = recipe.isSpecial
  }

}
