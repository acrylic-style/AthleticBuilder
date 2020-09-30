package xyz.acrylicstyle.athleticBuilder.util

import org.bukkit.Location
import util.CollectionList

class MutableAthleticPath(
    id: String,
    name: String,
    override var initialLocation: Location?,
    override var start: Location?,
    override var paths: CollectionList<Location>,
    override var goal: Location?,
): AthleticPath(id, name, initialLocation, start, paths, goal)
