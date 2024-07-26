package xyz.acrylicstyle.athleticbuilder.util

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class PlayerAthleticProgress(private val uuid: UUID, val id: String, val useItem: Boolean) {
    private var pendingRecord: PendingPlayerAthleticRecord? = null
    var lastSection: Location? = null
    var lastSectionPlayer: Location? = null
    private val hotBar = mutableMapOf<Int, ItemStack?>()

    fun getPath(): AthleticPath = AthleticManager.getAthletic(id)!!.toAthleticPath()

    fun getConfig() = PlayerConfiguration(uuid)

    fun getRecord() = getConfig().getAthleticRecord(id)

    fun getPendingRecord(): PendingPlayerAthleticRecord = pendingRecord!!

    fun setPendingRecord(record: PendingPlayerAthleticRecord) { pendingRecord = record }

    fun storeAndClearHotBar(player: Player) {
        if (useItem) {
            hotBar.clear()
            (0..8).forEach { slot -> hotBar[slot] = player.inventory.getItem(slot) }
            (0..8).forEach { player.inventory.setItem(it, null) }
        }
    }

    fun restoreHotBar(player: Player) {
        if (useItem) {
            hotBar.forEach { (slot, item) -> player.inventory.setItem(slot, item) }
        }
    }
}
