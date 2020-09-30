package xyz.acrylicstyle.athleticBuilder.util

import org.bukkit.Location
import util.StringCollection
import java.io.File
import java.lang.RuntimeException

object AthleticManager {
    private val cache = StringCollection<AthleticConfiguration>()

    fun loadAthletics() {
        if (!File("./plugins/AthleticBuilder/athletics/").exists()) return
        File("./plugins/AthleticBuilder/athletics/").listFiles { file -> file.isFile }!!.forEach { file ->
            val config = AthleticConfiguration(file.nameWithoutExtension, file)
            cache[file.nameWithoutExtension] = config
        }
    }

    fun getAthletic(id: String): AthleticConfiguration? {
        if (cache.containsKey(id)) return cache[id]
        val file = File("./plugins/AthleticBuilder/athletics/$id.yml")
        if (!file.exists()) return null
        val config = AthleticConfiguration(id, file)
        cache[id] = config
        return config
    }

    fun deleteAthletic(id: String) {
        val file = File("./plugins/AthleticBuilder/athletics/$id.yml")
        if (file.exists()) file.delete()
        cache.remove(id)
    }

    fun createAthletic(id: String): AthleticConfiguration {
        if (cache.containsKey(id)) return cache[id]!!
        val file = File("./plugins/AthleticBuilder/athletics/$id.yml")
        val config = AthleticConfiguration(id, file)
        cache[id] = config
        return config
    }

    fun findAthletic(start: Location): AthleticConfiguration? {
        return try {
            cache.valuesList().last { config ->
                try {
                    config.getStartLocation() == start
                } catch (e: RuntimeException) {
                    throw RuntimeException("Could not get start location for " + config.id)
                }
            }
        } catch (ex: NoSuchElementException) {
            null
        }
    }
}
