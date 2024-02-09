package xyz.acrylicstyle.athleticbuilder.commands

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

object RootCommand : TabExecutor {
    private val commands = listOf(CreateCommand, DeleteCommand, ListCommand)

    override fun onCommand(sender: CommandSender, p1: Command, p2: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            commands.forEach {
                sender.sendMessage("${ChatColor.LIGHT_PURPLE}${it.usage} ${ChatColor.GRAY}- ${ChatColor.AQUA}${it.description}")
            }
            return true
        }
        val command = commands.find { it.name.equals(args[0], ignoreCase = true) } ?: run {
            commands.forEach {
                sender.sendMessage("${ChatColor.LIGHT_PURPLE}${it.usage} ${ChatColor.GRAY}- ${ChatColor.AQUA}${it.description}")
            }
            return true
        }
        if (sender !is Player) {
            sender.sendMessage("This command can only be executed by players.")
            return true
        }
        command.onCommand(sender, args.drop(1).toTypedArray())
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        p1: Command,
        p2: String,
        args: Array<String>
    ): List<String> {
        if (args.size == 1) {
            return commands.map { it.name }.filter { it.startsWith(args[0], ignoreCase = true) }
        }
        if (args.size == 2 && sender is Player) {
            return commands.find { it.name.equals(args[0], ignoreCase = true) }?.suggest(sender, args) ?: emptyList()
        }
        return emptyList()
    }
}
