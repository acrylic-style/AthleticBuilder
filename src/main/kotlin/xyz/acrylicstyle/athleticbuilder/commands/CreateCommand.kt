package xyz.acrylicstyle.athleticbuilder.commands

import org.bukkit.ChatColor
import org.bukkit.entity.Player
import xyz.acrylicstyle.athleticbuilder.AthleticBuilderPlugin
import xyz.acrylicstyle.athleticbuilder.util.AthleticManager
import xyz.acrylicstyle.athleticbuilder.util.MutableAthleticPath

object CreateCommand: SubCommand("create", "/athletic create <ID> <名前>", "新しいアスレチックを作成します。") {
    val ID_REGEX = "^[a-zA-Z0-9][a-zA-Z0-9_]*[a-zA-Z0-9]$".toRegex()

    override fun onCommand(player: Player, args: Array<String>) {
        if (args.size < 2) {
            player.sendMessage("${ChatColor.RED}IDと名前を指定してください。")
            return
        }
        val id = args[0]
        val name = args[1]
        if (!ID_REGEX.matches(id)) {
            player.sendMessage("${ChatColor.RED}IDは英数字アンダーバーのみで英数字で始まり、英数字で終わる必要があります。")
            return
        }
        if (AthleticManager.getAthletic(id) != null) {
            player.sendMessage("${ChatColor.RED}指定したアスレチックはすでに存在します。")
            return
        }
        AthleticBuilderPlugin.buildingAthletic[player.uniqueId] = MutableAthleticPath(id, name, null, null, mutableListOf(), null)
        player.sendMessage("${ChatColor.GREEN}アスレチックの作成を開始しました。${ChatColor.GRAY}(ID: ${id})")
        player.sendMessage("${ChatColor.LIGHT_PURPLE}金の感圧版${ChatColor.YELLOW}: ${ChatColor.GREEN}スタート・ゴール地点")
        player.sendMessage("${ChatColor.LIGHT_PURPLE}鉄の感圧版${ChatColor.YELLOW}: ${ChatColor.GREEN}中間地点")
        player.sendMessage("${ChatColor.LIGHT_PURPLE}最初の金の感圧版を設置した地点で設置したプレイヤーの位置をスタート(テレポート)地点として記録します。")
        player.sendMessage("${ChatColor.LIGHT_PURPLE}ゴール地点を設置した地点でアスレチックの設定が完了します。")
    }
}
