package chessengine.engine

/** A high-performance cache for search results, using a Dual-Bucket strategy to
  * balance search depth and recency. The table is implemented as a fixed-size
  * array of [[TTEntry]], where each logical "bucket" contains 2 idx slots:
  *
  *   1. A Depth-Preferred slot: Stores the result of the deepest search
  *   2. A Recent-Replace slot: Always stores the most recent search result
  *
  * @param size
  *   The total number of slots in the underlying array. Must be a power of 2.
  */
class TranspositionTable private (val size: Int):
  private val table: Array[TTEntry] = new Array[TTEntry](size)
  private final val BUCKETS: Int = size / 2

  /** Maps 64-bit Zobrist hash to a pair of array indices (depthIdx, recentIdx).
    *
    * Uses bitwise AND (`&`) with `BUCKETS - 1` as a fast alternative to modulo
    * (`%`). This is mathematically equivalent to `hash % BUCKETS` because
    * `BUCKETS` is guaranteed to be a power of 2.
    */
  def index(hash: Long): (Int, Int) =
    val bucketIdx = (hash & (BUCKETS - 1)).toInt
    (bucketIdx * 2, bucketIdx * 2 + 1)

  /** Searches Depth-Preferred and Recent-Replace slots for a matching hash.
    * Gets the entry with the greatest depth if multiple matches are found,
    * otherwise the single matching entry, or None.
    */
  def lookup(hash: Long): Option[TTEntry] =
    val candidates = List(depthLookup(hash), recentLookup(hash)).flatten
    candidates.maxByOption(e => e.depth)

  def depthLookup(hash: Long): Option[TTEntry] =
    val (depthIndex, _) = index(hash)
    Option(table(depthIndex)).filter(_.hash == hash)

  def recentLookup(hash: Long): Option[TTEntry] =
    val (_, recentIndex) = index(hash)
    Option(table(recentIndex)).filter(_.hash == hash)

  /** Stores a search result in the table. This method always updates the
    * Recent-Replace slot. It only updates the Depth-Preferred slot if the new
    * entry has a depth greater than or equal to the existing one.
    */
  def store(entry: TTEntry): Unit =
    val (depthIndex, recentIndex) = index(entry.hash)
    table.update(recentIndex, entry)
    val replaceDepth =
      Option(table(depthIndex)).forall(past => entry.depth >= past.depth)
    if replaceDepth then table.update(depthIndex, entry)

object TranspositionTable:
  /** Creates a new TranspositionTable with a size optimized for memory budget.
    * size must be a power of 2 at the end for efficient bitwise AND operation.
    */
  def apply(sizeInMB: Int): TranspositionTable =
    val bytesPerEntry = 48
    val sizeInBytes = sizeInMB.toLong * 1024 * 1024
    val maxEntries = (sizeInBytes / bytesPerEntry)
    new TranspositionTable(size = Integer.highestOneBit(maxEntries.toInt))
