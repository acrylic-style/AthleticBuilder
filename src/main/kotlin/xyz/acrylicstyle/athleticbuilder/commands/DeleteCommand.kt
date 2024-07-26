package xyz.acrylicstyle.athleticbuilder.commands

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import xyz.acrylicstyle.athleticbuilder.util.AthleticManager

object DeleteCommand: SubCommand("delete", "/athletic delete <ID>", "アスレチックを削除します。") {
    override fun onCommand(player: Player, args: Array<String>) {
        if (args.isEmpty()) {
            player.sendMessage("${ChatColor.RED}IDを指定してください。")
            return
        }
        val id = args[0]
        AthleticManager.getAthletic(id).let { athletic ->
            if (athletic == null) {
                player.sendMessage("${ChatColor.RED}指定したアスレチックは存在しません。")
                return
            }
            if (!player.hasPermission("athleticbuilder.delete-others") && athletic.getOwner() != player.uniqueId) {
                player.sendMessage("${ChatColor.RED}指定したアスレチックはあなたが作成したものではありません。")
                return
            }
            AthleticManager.deleteAthletic(id)
            player.sendMessage("${ChatColor.GREEN}アスレチック${ChatColor.YELLOW}$id${ChatColor.GREEN}を削除しました。")
        }
    }

    override fun suggest(player: Player, args: Array<String>): List<String> {
        if (args.size == 1) {
            return AthleticManager.getAthletics().keys.filter { it.startsWith(args[0], ignoreCase = true) }
        }
        return emptyList()
    }
}
