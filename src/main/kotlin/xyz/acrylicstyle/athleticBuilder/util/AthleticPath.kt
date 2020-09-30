package xyz.acrylicstyle.athleticBuilder.util

import org.bukkit.Location

open class AthleticPath(
    val id: String,
    val name: String,
    open val initialLocation: Location?,
    open val start: Location?,
    open val paths: List<Location>,
    open val goal: Location?,
)
