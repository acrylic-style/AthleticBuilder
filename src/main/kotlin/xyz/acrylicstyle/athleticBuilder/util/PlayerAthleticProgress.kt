package xyz.acrylicstyle.athleticBuilder.util

import org.bukkit.Location
import java.util.*

class PlayerAthleticProgress(private val uuid: UUID, val id: String) {
    private var pendingRecord: PendingPlayerAthleticRecord? = null
    var lastSection: Location? = null

    fun getPath(): AthleticPath = AthleticManager.getAthletic(id)!!.toAthleticPath()

    fun getConfig() = PlayerConfiguration(uuid)

    fun getRecord() = getConfig().getAthleticRecord(id)

    fun getPendingRecord(): PendingPlayerAthleticRecord = pendingRecord!!

    fun setPendingRecord(record: PendingPlayerAthleticRecord) { pendingRecord = record }
}
