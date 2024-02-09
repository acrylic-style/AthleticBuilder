package xyz.acrylicstyle.athleticbuilder.util

import java.util.*

class PlayerAthleticRecord(val id: String, val sectionTime: List<Int>, val goalTime: Int) {
    fun save(uuid: UUID) = PlayerConfiguration(uuid).setAthleticRecord(this)
}

class PendingPlayerAthleticRecord(
    val id: String,
    val startTime: Long,
    val sectionTime: MutableList<Int>,
    var goalTime: Int
) {
    fun toPlayerAthleticRecord() = PlayerAthleticRecord(id, sectionTime, goalTime)
}
