package xyz.acrylicstyle.athleticBuilder.util

import util.CollectionList
import xyz.acrylicstyle.tomeito_api.providers.ConfigProvider
import java.util.*

class PlayerConfiguration(uuid: UUID): ConfigProvider("./plugins/AthleticBuilder/$uuid.yml") {
    fun getAthleticRecord(id: String): PlayerAthleticRecord? {
        if (this.get("record.$id") == null) return null
        val list = CollectionList(this.getIntegerList("record.$id.sections"))
        val goalTime = this.getInt("record.$id.goalTime", -1)
        return PlayerAthleticRecord(id, list, goalTime)
    }

    fun setAthleticRecord(record: PlayerAthleticRecord) {
        this.set("record.${record.id}.sections", record.sectionTime)
        this.set("record.${record.id}.goalTime", record.goalTime)
        this.save()
    }
}