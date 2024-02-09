package xyz.acrylicstyle.athleticbuilder.util

import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

class AthleticConfiguration(val id: String, private val path: File): YamlConfiguration() {
    init {
        path.parentFile.mkdirs()
        if (path.exists()) load(path)
    }

    fun getAthleticName(): String? = this.getString("name")

    fun setAthleticName(name: String?) = this.set("name", name)

    fun setInitialLocation(location: Location?) = this.set("initialLocation", location)

    fun getInitialLocation(): Location? = this.getLocation("initialLocation")

    fun setStartLocation(location: Location?) = this.set("start", location)

    fun getStartLocation(): Location? = this.getLocation("start")

    fun setPaths(path: List<Location>?) {
        if (path == null) return this.set("path", null)
        this.set("path", path)
        //this.set("path", path.map { Location::serialize })
    }

    @Suppress("UNCHECKED_CAST")
    fun getPaths(): List<Location>? {
        val list = this.getList("path") ?: return null
        return try {
            list as List<Location>
        } catch (e: ClassCastException) {
            list.map { Location.deserialize(it as MutableMap<String, Any>) }
        }
    }

    fun setGoalLocation(location: Location?) = this.set("goal", location)

    fun getGoalLocation(): Location? = this.getLocation("goal")

    fun fromAthleticPath(path: AthleticPath) {
        this.setAthleticName(path.name)
        this.setInitialLocation(path.initialLocation)
        this.setStartLocation(path.start)
        this.setPaths(path.paths)
        this.setGoalLocation(path.goal)
        this.save(this.path)
    }

    fun toAthleticPath() = AthleticPath(id, getAthleticName() ?: id, getInitialLocation(), getStartLocation(), getPaths() ?: ArrayList(), getGoalLocation())
}
