package chessengine.engine

import chessengine.domain.{Move}

/** cache that stores the results of previous searches to avoid redundant work
  * when the same position is reached via different move orders.
  */
private class TranspositionTable private (val size: Int):
  private val table: Array[TTEntry] = new Array[TTEntry](size)

  def index(hash: Long): Int =
    // % size is equal to & (size - 1) when size is power of 2. & is way faster
    (hash & (size - 1)).toInt

  def lookup(hash: Long): Option[TTEntry] =
    Option(table(index(hash))) match
      case Some(entry) if entry.hash == hash => Some(entry)
      case _                                 => None

  /** depth decides what to store regardless if entry.hash == pastEntry.hash or
    * not. If depth is same, it is better to replace with most recent result, as
    * it is probably more relevant to current branch
    */
  def store(entry: TTEntry): Unit =
    val idx = index(entry.hash)
    Option(table(idx)) match
      case Some(pastEntry) =>
        //
        if entry.depth >= pastEntry.depth then table.update(idx, entry) else ()
      case None => table.update(idx, entry)

object TranspositionTable:
  /** Create TT with size N which gets calculated using the sizeInMB. We want
    * the largest power of 2 that is less than or = to maxEntries. This allows
    * us to use bitwise operations for indexing later. Uses built-in JVM
    * function that takes a number and returns the largest power of 2 that is
    * less than or equal to it.
    */
  def apply(sizeInMB: Int): TranspositionTable =
    val bytesPerEntry = 48
    val maxEntries = (sizeInMB.toLong * (1024 * 1024)) / bytesPerEntry
    new TranspositionTable(size = Integer.highestOneBit(maxEntries.toInt))
