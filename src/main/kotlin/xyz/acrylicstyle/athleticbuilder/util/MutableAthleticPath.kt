package xyz.acrylicstyle.athleticbuilder.util

import org.bukkit.Location
import java.util.UUID

class MutableAthleticPath(
    id: String,
    name: String,
    owner: UUID,
    override var initialLocation: Location?,
    override var start: Location?,
    override var paths: MutableList<Location>,
    override var goal: Location?,
): AthleticPath(id, name, owner, initialLocation, start, paths, goal)
