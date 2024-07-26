package xyz.acrylicstyle.athleticbuilder.util

import org.bukkit.Location
import java.util.UUID

open class AthleticPath(
    val id: String,
    val name: String,
    val owner: UUID,
    open val initialLocation: Location?,
    open val start: Location?,
    open val paths: List<Location>,
    open val goal: Location?,
)
