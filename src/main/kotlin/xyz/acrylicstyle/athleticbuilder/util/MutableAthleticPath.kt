package xyz.acrylicstyle.athleticbuilder.util

import org.bukkit.Location

class MutableAthleticPath(
    id: String,
    name: String,
    override var initialLocation: Location?,
    override var start: Location?,
    override var paths: MutableList<Location>,
    override var goal: Location?,
): AthleticPath(id, name, initialLocation, start, paths, goal)
