package xyz.acrylicstyle.athleticBuilder.util

import util.CollectionList
import java.util.*

class PlayerAthleticRecord(val id: String, val sectionTime: CollectionList<Int>, val goalTime: Int) {
    fun save(uuid: UUID) = PlayerConfiguration(uuid).setAthleticRecord(this)
}

class PendingPlayerAthleticRecord(
    val id: String,
    val startTime: Long,
    val sectionTime: CollectionList<Int>,
    var goalTime: Int
) {
    fun toPlayerAthleticRecord() = PlayerAthleticRecord(id, sectionTime, goalTime)
}
