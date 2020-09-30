package xyz.acrylicstyle.athleticBuilder

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.plugin.java.JavaPlugin
import util.Collection
import util.CollectionList
import xyz.acrylicstyle.athleticBuilder.util.AthleticManager
import xyz.acrylicstyle.athleticBuilder.util.MutableAthleticPath
import xyz.acrylicstyle.athleticBuilder.util.PendingPlayerAthleticRecord
import xyz.acrylicstyle.athleticBuilder.util.PlayerAthleticProgress
import xyz.acrylicstyle.tomeito_api.TomeitoAPI
import xyz.acrylicstyle.tomeito_api.command.PlayerCommandExecutor
import xyz.acrylicstyle.tomeito_api.sounds.Sound
import java.util.*
import kotlin.math.floor

// ロビー用アスレプラグイン
class AthleticBuilderPlugin : JavaPlugin(), Listener {
    companion object {
        val buildingAthletic = Collection<UUID, MutableAthleticPath>()
        val playingAthletic = Collection<UUID, PlayerAthleticProgress>()
    }

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
        TomeitoAPI.registerCommand("spawn", object: PlayerCommandExecutor() {
            override fun onCommand(player: Player, args: Array<String>) {
                playingAthletic.remove(player.uniqueId)
                player.teleport(player.world.spawnLocation)
            }
        })
        TomeitoAPI.registerCommand("reset", object: PlayerCommandExecutor() {
            override fun onCommand(player: Player, args: Array<String>) {
                if (playingAthletic.containsKey(player.uniqueId)) {
                    val progress = playingAthletic.remove(player.uniqueId)!!
                    player.sendMessage("${ChatColor.GREEN}スタート地点に戻りました。")
                    player.teleport(progress.getPath().initialLocation)
                } else {
                    player.sendMessage("${ChatColor.RED}現在あなたはアスレチックをプレイしていません。")
                }
            }
        })
        TomeitoAPI.registerCommand("back", object: PlayerCommandExecutor() {
            override fun onCommand(player: Player, args: Array<String>) {
                if (playingAthletic.containsKey(player.uniqueId)) {
                    val progress = playingAthletic[player.uniqueId]!!
                    player.sendMessage("${ChatColor.GREEN}最後に通ったチェックポイントに戻りました。")
                    player.teleport(progress.lastSection ?: progress.getPath().initialLocation)
                } else {
                    player.sendMessage("${ChatColor.RED}現在あなたはアスレチックをプレイしていません。")
                }
            }
        })
        AthleticManager.loadAthletics()
        TomeitoAPI.getInstance().registerCommands(this.classLoader, "athletic", "xyz.acrylicstyle.athleticBuilder")
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        buildingAthletic.remove(e.player.uniqueId)
        playingAthletic.remove(e.player.uniqueId)
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        buildingAthletic.remove(e.player.uniqueId)
        playingAthletic.remove(e.player.uniqueId)
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        if (buildingAthletic.containsKey(e.player.uniqueId)) {
            val path = buildingAthletic[e.player.uniqueId]!!
            if (e.blockPlaced.type == Material.GOLD_PLATE) {
                if (path.start == null) {
                    path.initialLocation = e.player.location.clone()
                    path.start = e.blockPlaced.location
                    e.player.sendMessage("${ChatColor.GREEN}スタート地点を追加しました。(${e.blockPlaced.location.toReadableString()}${ChatColor.GREEN})")
                    return
                }
                if (path.goal == null) {
                    path.goal = e.blockPlaced.location
                    e.player.sendMessage("${ChatColor.GREEN}ゴール地点を追加しました。(${e.blockPlaced.location.toReadableString()}${ChatColor.GREEN})")
                    Thread {
                        buildingAthletic.remove(e.player.uniqueId)
                        AthleticManager.createAthletic(path.id).fromAthleticPath(path)
                        e.player.sendMessage("${ChatColor.YELLOW}${path.name}(ID: ${path.id})${ChatColor.GREEN}を作成しました。")
                    }.start()
                    return
                }
            } else if (e.blockPlaced.type == Material.IRON_PLATE) {
                val size = path.paths.size + 1
                path.paths.add(e.blockPlaced.location)
                e.player.sendMessage("${ChatColor.YELLOW}${size}${ChatColor.GREEN}個目の中間地点を追加しました。(${e.blockPlaced.location.toReadableString()}${ChatColor.GREEN})")
            }
        }
    }

    @EventHandler
    fun onPlayerTeleport(e: PlayerTeleportEvent) {
        if (e.cause == PlayerTeleportEvent.TeleportCause.PLUGIN) return
        playingAthletic.remove(e.player.uniqueId)?.let {
            e.player.sendMessage("${ChatColor.RED}アスレチック失敗: テレポートされました。")
            e.player.teleport(it.getPath().initialLocation)
        }
    }

    @EventHandler
    fun onPlayerToggleFlight(e: PlayerToggleFlightEvent) {
        playingAthletic.remove(e.player.uniqueId)?.let {
            e.player.sendMessage("${ChatColor.RED}アスレチック失敗: 浮遊状態を切り替えました。")
            e.player.teleport(it.getPath().initialLocation)
        }
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if (e.action != Action.PHYSICAL) return
        if (e.clickedBlock == null) return
        if (e.clickedBlock.type == Material.GOLD_PLATE) {
            e.setUseInteractedBlock(Event.Result.DENY)
            if (playingAthletic.containsKey(e.player.uniqueId)) { // goal
                val progress = playingAthletic[e.player.uniqueId]!!
                if (e.clickedBlock.location != progress.getPath().goal) return
                playingAthletic.remove(e.player.uniqueId)
                val record = progress.getRecord()
                if (progress.getPath().paths.size != progress.getPendingRecord().sectionTime.size) {
                    e.player.sendMessage("${ChatColor.RED}アスレチック失敗: どこかの中間地点をスキップしました。")
                    playingAthletic.remove(e.player.uniqueId)
                    return
                }
                progress.getPendingRecord().goalTime = (System.currentTimeMillis() - progress.getPendingRecord().startTime).toInt()
                e.player.playSound(e.player.location, org.bukkit.Sound.LEVEL_UP, 100000F, 1F)
                e.player.sendMessage("${ChatColor.GREEN}アスレチック「${progress.getPath().name}」をゴールしました。")
                var newRecord = false
                if (record != null) {
                    e.player.sendMessage("${ChatColor.YELLOW}前回の記録: ${ChatColor.WHITE}${formatTime(record.goalTime)}${ChatColor.GREEN}秒")
                    if (record.goalTime > progress.getPendingRecord().goalTime) {
                        newRecord = true
                    }
                } else {
                    newRecord = true
                }
                if (newRecord) progress.getPendingRecord().toPlayerAthleticRecord().save(e.player.uniqueId)
                e.player.sendMessage("${ChatColor.YELLOW}今回の記録: ${ChatColor.WHITE}${formatTime(progress.getPendingRecord().goalTime)}${ChatColor.GREEN}秒" + if (newRecord) "${ChatColor.GOLD} (新記録)" else "")
            } else { // start, or invalid location
                val config = AthleticManager.findAthletic(e.clickedBlock.location) ?: return
                if (e.player.isFlying) {
                    e.player.sendMessage("${ChatColor.RED}浮遊している状態ではアスレチックを開始できません。")
                    return
                }
                val progress = PlayerAthleticProgress(e.player.uniqueId, config.id)
                progress.setPendingRecord(PendingPlayerAthleticRecord(config.id, System.currentTimeMillis(), CollectionList(), 0))
                playingAthletic[e.player.uniqueId] = progress
                e.player.playSound(e.player.location, Sound.BLOCK_NOTE_PLING, 100000F, 2F)
                e.player.sendMessage("${ChatColor.GREEN}アスレチック「${config.getAthleticName()}」を開始しました。")
                e.player.sendMessage("${ChatColor.LIGHT_PURPLE}/reset${ChatColor.GREEN}で最初の位置に戻れます。")
                e.player.sendMessage("${ChatColor.LIGHT_PURPLE}/back${ChatColor.GREEN}で最後に通ったチェックポイントの位置に戻れます。")
            }
        } else if (e.clickedBlock.type == Material.IRON_PLATE) {
            e.setUseInteractedBlock(Event.Result.DENY)
            if (playingAthletic.containsKey(e.player.uniqueId)) {
                val progress = playingAthletic[e.player.uniqueId]!!
                if (!progress.getPath().paths.contains(e.clickedBlock.location)) return
                if (progress.lastSection == e.clickedBlock.location) return
                if (progress.getPath().paths.indexOf(e.clickedBlock.location) != progress.getPendingRecord().sectionTime.size) {
                    e.player.sendMessage("${ChatColor.RED}アスレチック失敗: どこかの中間地点をスキップしました。")
                    playingAthletic.remove(e.player.uniqueId)
                    e.player.teleport(progress.getPath().initialLocation)
                    return
                }
                val record = progress.getRecord()
                val currTime = (System.currentTimeMillis() - progress.getPendingRecord().startTime).toInt()
                progress.getPendingRecord().sectionTime.add(currTime)
                e.player.playSound(e.player.location, Sound.BLOCK_NOTE_PLING, 100000F, 2F)
                var color = ChatColor.WHITE
                var previousRecord = ""
                if (record != null) {
                    val prevTime = record.sectionTime[progress.getPendingRecord().sectionTime.size-1]
                    previousRecord += ", 前回: ${ChatColor.WHITE}${formatTime(prevTime)}${ChatColor.GRAY}"
                    if (currTime < prevTime) color = ChatColor.GOLD
                }
                previousRecord += ")"
                e.player.sendMessage("${ChatColor.GREEN}中間地点を通過しました。 ${ChatColor.GRAY}(今回: ${color}${formatTime(currTime)}${ChatColor.GRAY}$previousRecord")
                progress.lastSection = e.clickedBlock.location
            }
        }
    }

    private fun formatTime(i: Int): String {
        val minutes = floor(i / 60000F).toInt().toString().let { if (it.length == 1) "0$it" else it }
        val seconds = floor(i / 1000F).toInt().toString().let { if (it.length == 1) "0$it" else it }
        val ms = i % 1000
        return "$minutes:$seconds.$ms"
    }

    private fun Location.toReadableString() = "${ChatColor.YELLOW}${this.blockX}${ChatColor.GRAY}, ${ChatColor.YELLOW}${this.blockY}${ChatColor.GRAY}, ${ChatColor.YELLOW}${this.blockZ}"
}
