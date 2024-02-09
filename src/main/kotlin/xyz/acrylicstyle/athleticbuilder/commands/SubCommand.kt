package xyz.acrylicstyle.athleticbuilder.commands

import org.bukkit.entity.Player

abstract class SubCommand(val name: String, val usage: String, val description: String) {
    abstract fun onCommand(player: Player, args: Array<String>)

    open fun suggest(player: Player, args: Array<String>): List<String> = emptyList()
}
