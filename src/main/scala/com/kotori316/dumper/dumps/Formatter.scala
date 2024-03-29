package com.kotori316.dumper.dumps

class Formatter[A](rows: Seq[String], converters: Seq[A => Any]) {

  require(rows.size == converters.size, s"rows $rows doesn't match converters $converters.")

  def format(as: scala.collection.Seq[A]): Seq[String] = {
    val convertedContent: Seq[Seq[String]] = as.map(a => converters.map(_.apply(a).toString)).toSeq
    val lengthMaxes: Seq[Int] = (rows +: convertedContent).map(_.map(_.length)).foldLeft(Seq.fill(rows.size)(0)) {
      case (s1, s2) => (s1 zip s2).map { case (i, i1) => i max i1 }
    }
    val secondRow = makeSecondRow(lengthMaxes)
    val formatString = (lengthMaxes zip rows).map { case (i, str) => s"%${getMinus(str)}${i}s" }.mkString("| ", " | ", " |")

    val headers: Seq[String] = Seq(
      formatString.format(removeMinus(rows): _*),
      secondRow.mkString("|", "|", "|")
    )

    (headers ++ convertedContent.map(ss => formatString.format(ss: _*)))
      .map(s => ("0" + s).trim.substring(1))
  }

  def getMinus(s: String): String = if (s.startsWith("-")) "-" else ""

  def removeMinus(ss: Seq[String]): Seq[String] = ss.map(s => if (s.startsWith("-")) s.tail else s)

  def makeSecondRow(maxes: Seq[Int]): Seq[String] = {
    for {
      (row, max) <- rows zip maxes
    } yield {
      val isLeft = row.startsWith("-")
      val prefix = if (isLeft) ":" else ""
      val postfix = if (isLeft) "" else ":"
      // Minus 1 means the colon. Plus 2 means the spaces inserted to separate entries.
      prefix + "-" * (max - 1 + 2) + postfix
    }
  }
}
