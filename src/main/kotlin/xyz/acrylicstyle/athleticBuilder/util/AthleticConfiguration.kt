package xyz.acrylicstyle.athleticBuilder.util

import org.bukkit.Location
import xyz.acrylicstyle.tomeito_api.providers.ConfigProvider
import java.io.File

class AthleticConfiguration(val id: String, path: File): ConfigProvider(path) {
    fun getAthleticName(): String? = this.getString("name")

    fun setAthleticName(name: String?) = this.set("name", name)

    fun setInitialLocation(location: Location?) = this.set("initialLocation", location)

    fun getInitialLocation(): Location? = this.get("initialLocation") as Location?

    fun setStartLocation(location: Location?) = this.set("start", location)

    fun getStartLocation(): Location? = this.get("start") as Location?

    fun setPaths(path: List<Location>?) {
        if (path == null) return this.set("path", null)
        this.set("path", path)
        //this.set("path", path.map { Location::serialize })
    }

    @Suppress("UNCHECKED_CAST")
    fun getPaths(): List<Location>? {
        val list = this.getList("path") ?: return null
        return list as List<Location>
        //return list.map { Location.deserialize(it as MutableMap<String, Any>) }
    }

    fun setGoalLocation(location: Location?) = this.set("goal", location)

    fun getGoalLocation(): Location? = this.get("goal") as Location?

    fun fromAthleticPath(path: AthleticPath) {
        this.setAthleticName(path.name)
        this.setInitialLocation(path.initialLocation)
        this.setStartLocation(path.start)
        this.setPaths(path.paths)
        this.setGoalLocation(path.goal)
        this.save()
    }

    fun toAthleticPath() = AthleticPath(id, getAthleticName() ?: id, getInitialLocation(), getStartLocation(), getPaths() ?: ArrayList(), getGoalLocation())
}
