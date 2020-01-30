package com.kotori316.dumper

import java.nio.file.{Files, Paths}

import com.kotori316.dumper.dumps._
import com.kotori316.dumper.dumps.items.{BlocksDump, ItemsDump, TagDump}
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent
import net.minecraftforge.fml.event.server.FMLServerStartedEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, TimeoutException}
import scala.util.Failure

object DumperInternal {
  val loadCompleteDumpers: Seq[Dumps[_]] = Seq(ModNames, EnchantmentNames, FluidNames, TENames)
  val loginDumpers: Seq[Dumps[_]] = Seq(ItemsDump, BlocksDump, TagDump)

  def loadComplete(event: FMLLoadCompleteEvent): Unit = {
    output(loadCompleteDumpers)
  }

  def worldLoaded(event: FMLServerStartedEvent): Unit = {
    output(loginDumpers)
  }

  private def output(dumpers: Seq[Dumps[_]]): Unit = {
    val l = System.nanoTime()
    val ROOTPath = Paths.get(Dumper.modID)
    if (Files.notExists(ROOTPath))
      Files.createDirectories(ROOTPath)
    val futures = Future.traverse(dumpers) { d =>
      val future = Future(d.apply())
      future.onComplete {
        case Failure(exception) => Dumper.LOGGER.error(d.getClass, exception)
        case _ => Dumper.LOGGER.info(s"Success to output ${d.fileName}.txt")
      }
      future
    }
    try {
      Await.ready(futures, Duration(5, "min"))
      futures.onComplete { _ =>
        val l2 = System.nanoTime()
        Dumper.LOGGER.info(f"Dumper finished in ${(l2 - l) / 1e9}%.3f s")
      }
    } catch {
      case e: TimeoutException => Dumper.LOGGER.error("Timeout to dump info.", e)
    }
  }

}

class Config(builder: ForgeConfigSpec.Builder) {
  builder.comment("Which information to output?").push("setting")
  val enables0 = DumperInternal.loadCompleteDumpers.map(_.configName).map(n =>
    builder.comment(s"Enable output of $n.").define(n, true))
  val enables1 = DumperInternal.loginDumpers.map(_.configName).map(n =>
    builder.comment(s"Enable output of $n.").define(n, true))
  val enables = enables0 ++ enables1
  builder.pop()
}
