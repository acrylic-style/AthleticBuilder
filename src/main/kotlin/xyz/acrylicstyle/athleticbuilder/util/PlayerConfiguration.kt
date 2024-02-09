package xyz.acrylicstyle.athleticbuilder.util

import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.util.*

class PlayerConfiguration(uuid: UUID) : YamlConfiguration() {
    private val path: String = "plugins/AthleticBuilder/$uuid.yml"

    init {
        File(path).parentFile.mkdirs()
        if (File(path).exists()) load(path)
    }

    fun getAthleticRecord(id: String): PlayerAthleticRecord? {
        if (this.get("record.$id") == null) return null
        val list = this.getIntegerList("record.$id.sections")
        val goalTime = this.getInt("record.$id.goalTime", -1)
        return PlayerAthleticRecord(id, list, goalTime)
    }

    fun setAthleticRecord(record: PlayerAthleticRecord) {
        this.set("record.${record.id}.sections", record.sectionTime)
        this.set("record.${record.id}.goalTime", record.goalTime)
        this.save(path)
    }
}
