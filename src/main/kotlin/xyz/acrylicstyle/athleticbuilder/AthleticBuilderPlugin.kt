package xyz.acrylicstyle.athleticbuilder

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.plugin.java.JavaPlugin
import xyz.acrylicstyle.athleticbuilder.commands.RootCommand
import xyz.acrylicstyle.athleticbuilder.util.AthleticManager
import xyz.acrylicstyle.athleticbuilder.util.Items
import xyz.acrylicstyle.athleticbuilder.util.MutableAthleticPath
import xyz.acrylicstyle.athleticbuilder.util.PendingPlayerAthleticRecord
import xyz.acrylicstyle.athleticbuilder.util.PlayerAthleticProgress
import java.util.*
import kotlin.math.floor

class AthleticBuilderPlugin : JavaPlugin(), Listener {
    companion object {
        val buildingAthletic = mutableMapOf<UUID, MutableAthleticPath>()
        val playingAthletic = mutableMapOf<UUID, PlayerAthleticProgress>()
    }

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this)
        AthleticManager.loadAthletics()
        server.getPluginCommand("athletic")?.setExecutor(RootCommand)
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, Runnable {
            Bukkit.getOnlinePlayers().forEach { player ->
                val progress = playingAthletic[player.uniqueId] ?: return@forEach
                sendActionBar(player, "${ChatColor.GREEN}${progress.getPath().name} ${ChatColor.LIGHT_PURPLE}| ${ChatColor.GREEN}時間: ${ChatColor.WHITE}${formatTime((System.currentTimeMillis() - progress.getPendingRecord().startTime).toInt())}")
            }
        }, 1L, 1L)
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerQuit(e: PlayerQuitEvent) {
        buildingAthletic.remove(e.player.uniqueId)
        playingAthletic.remove(e.player.uniqueId)?.restoreHotBar(e.player)
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        buildingAthletic.remove(e.player.uniqueId)
        playingAthletic.remove(e.player.uniqueId)?.restoreHotBar(e.player)
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        if (e.itemInHand == Items.BACK_TO_LAST_CHECKPOINT
            || e.itemInHand == Items.RESET
            || e.itemInHand == Items.CANCEL) e.isCancelled = true
        if (buildingAthletic.containsKey(e.player.uniqueId)) {
            val path = buildingAthletic[e.player.uniqueId]!!
            if (e.blockPlaced.type == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
                if (path.start == null) {
                    path.initialLocation = e.player.location.clone().apply { yaw = 0.0F }
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
            } else if (e.blockPlaced.type == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
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
            it.getPath().initialLocation?.let { it1 -> e.player.teleport(it1) }
            removeAthleticItem(e.player)
            it.restoreHotBar(e.player)
        }
    }

    @EventHandler
    fun onPlayerToggleFlight(e: PlayerToggleFlightEvent) {
        playingAthletic.remove(e.player.uniqueId)?.let {
            e.player.sendMessage("${ChatColor.RED}アスレチック失敗: 浮遊状態を切り替えました。")
            it.getPath().initialLocation?.let { it1 -> e.player.teleport(it1) }
            removeAthleticItem(e.player)
            it.restoreHotBar(e.player)
        }
    }

    @EventHandler
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if (e.action == Action.RIGHT_CLICK_AIR || e.action == Action.RIGHT_CLICK_BLOCK || e.action == Action.LEFT_CLICK_AIR || e.action == Action.LEFT_CLICK_BLOCK) {
            if (e.item == null) return
            if (e.item == Items.BACK_TO_LAST_CHECKPOINT
                || e.item == Items.RESET
                || e.item == Items.CANCEL) {
                e.setUseInteractedBlock(Event.Result.DENY)
                e.setUseItemInHand(Event.Result.DENY)
                if (!playingAthletic.containsKey(e.player.uniqueId)) {
                    removeAthleticItem(e.player)
                    return
                }
                val progress = playingAthletic[e.player.uniqueId]!!
                when (e.item) {
                    Items.BACK_TO_LAST_CHECKPOINT -> {
                        if (e.action == Action.LEFT_CLICK_AIR || e.action == Action.LEFT_CLICK_BLOCK) {
                            progress.getPath().initialLocation?.let { e.player.teleport(it) }
                            return
                        }
                        (progress.lastSectionPlayer ?: progress.getPath().initialLocation)?.let { e.player.teleport(it) }
                    }
                    Items.RESET -> {
                        if (e.action == Action.LEFT_CLICK_AIR || e.action == Action.LEFT_CLICK_BLOCK) {
                            return
                        }
                        progress.getPath().initialLocation?.let { e.player.teleport(it) }
                    }
                    Items.CANCEL -> {
                        if (e.action == Action.LEFT_CLICK_AIR || e.action == Action.LEFT_CLICK_BLOCK) {
                            return
                        }
                        playingAthletic.remove(e.player.uniqueId)?.let {
                            removeAthleticItem(e.player)
                            it.restoreHotBar(e.player)
                        }
                    }
                }
            }
            return
        }
        if (e.action != Action.PHYSICAL) return
        if (e.clickedBlock == null) return
        if (e.clickedBlock!!.type == Material.LIGHT_WEIGHTED_PRESSURE_PLATE) {
            e.setUseInteractedBlock(Event.Result.DENY)
            if (playingAthletic.containsKey(e.player.uniqueId)) { // goal
                val progress = playingAthletic[e.player.uniqueId]!!
                if (e.clickedBlock!!.location == progress.getPath().start && (System.currentTimeMillis() - progress.getPendingRecord().startTime) > 1000) {
                    e.player.playSound(e.player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 100000F, 2F)
                    e.player.sendMessage("${ChatColor.GREEN}タイマーを00:00.000にリセットしました！")
                    progress.lastSection = null
                    progress.lastSectionPlayer = null
                    progress.setPendingRecord(PendingPlayerAthleticRecord(progress.id, System.currentTimeMillis(), mutableListOf(), 0))
                    giveAthleticItem(e.player)
                    return
                }
                if (e.clickedBlock!!.location != progress.getPath().goal) return
                playingAthletic.remove(e.player.uniqueId)
                val record = progress.getRecord()
                if (progress.getPath().paths.size != progress.getPendingRecord().sectionTime.size) {
                    e.player.sendMessage("${ChatColor.RED}アスレチック失敗: どこかの中間地点をスキップしました。")
                    removeAthleticItem(e.player)
                    progress.restoreHotBar(e.player)
                    return
                }
                progress.getPendingRecord().goalTime = (System.currentTimeMillis() - progress.getPendingRecord().startTime).toInt()
                e.player.playSound(e.player.location, Sound.ENTITY_PLAYER_LEVELUP, 100000F, 1F)
                e.player.sendMessage("${ChatColor.GREEN}アスレチック「${progress.getPath().name}」をゴールしました。")
                removeAthleticItem(e.player)
                progress.restoreHotBar(e.player)
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
                val config = AthleticManager.findAthletic(e.clickedBlock!!.location) ?: return
                if (e.player.isFlying) {
                    e.player.sendMessage("${ChatColor.RED}浮遊している状態ではアスレチックを開始できません。")
                    return
                }
                val progress = PlayerAthleticProgress(e.player.uniqueId, config.id)
                progress.storeAndClearHotBar(e.player)
                progress.setPendingRecord(PendingPlayerAthleticRecord(config.id, System.currentTimeMillis(), mutableListOf(), 0))
                playingAthletic[e.player.uniqueId] = progress
                e.player.playSound(e.player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 100000F, 2F)
                e.player.sendMessage("${ChatColor.GREEN}アスレチック「${config.getAthleticName()}」を開始しました。")
                giveAthleticItem(e.player)
            }
        } else if (e.clickedBlock!!.type == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
            e.setUseInteractedBlock(Event.Result.DENY)
            if (playingAthletic.containsKey(e.player.uniqueId)) {
                val progress = playingAthletic[e.player.uniqueId]!!
                if (!progress.getPath().paths.contains(e.clickedBlock!!.location)) return
                if (progress.lastSection == e.clickedBlock!!.location) return
                if (progress.getPath().paths.indexOf(e.clickedBlock!!.location) != progress.getPendingRecord().sectionTime.size) {
                    e.player.sendMessage("${ChatColor.RED}アスレチック失敗: どこかの中間地点をスキップしました。")
                    playingAthletic.remove(e.player.uniqueId)
                    progress.getPath().initialLocation?.let { e.player.teleport(it) }
                    removeAthleticItem(e.player)
                    progress.restoreHotBar(e.player)
                    return
                }
                val record = progress.getRecord()
                val currTime = (System.currentTimeMillis() - progress.getPendingRecord().startTime).toInt()
                progress.getPendingRecord().sectionTime.add(currTime)
                e.player.playSound(e.player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 100000F, 2F)
                var color = ChatColor.WHITE
                var previousRecord = ""
                if (record != null) {
                    val prevTime = record.sectionTime[progress.getPendingRecord().sectionTime.size-1]
                    previousRecord += ", 前回: ${ChatColor.WHITE}${formatTime(prevTime)}${ChatColor.GRAY}"
                    if (currTime < prevTime) color = ChatColor.GOLD
                } else {
                    color = ChatColor.GOLD
                }
                previousRecord += ")"
                e.player.sendMessage("${ChatColor.GREEN}中間地点を通過しました。 ${ChatColor.GRAY}(今回: ${color}${formatTime(currTime)}${ChatColor.GRAY}$previousRecord")
                progress.lastSection = e.clickedBlock!!.location
                progress.lastSectionPlayer = e.player.location.clone()
            }
        }
    }

    private fun sendActionBar(player: Player, text: String) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(text))
    }

    private fun giveAthleticItem(player: Player) {
        player.inventory.setItem(2, null)
        player.inventory.setItem(3, Items.BACK_TO_LAST_CHECKPOINT)
        player.inventory.setItem(4, Items.RESET)
        player.inventory.setItem(5, Items.CANCEL)
        player.inventory.setItem(6, null)
        player.inventory.heldItemSlot = 3
    }

    private fun removeAthleticItem(player: Player) {
        player.inventory.remove(Items.BACK_TO_LAST_CHECKPOINT)
        player.inventory.remove(Items.RESET)
        player.inventory.remove(Items.CANCEL)
    }

    @EventHandler
    fun onPlayerDropItem(e: PlayerDropItemEvent) {
        if (e.itemDrop.itemStack == Items.BACK_TO_LAST_CHECKPOINT
            || e.itemDrop.itemStack == Items.RESET
            || e.itemDrop.itemStack == Items.CANCEL) {
            e.isCancelled = true
        }
    }

    @EventHandler
    fun onItemSpawn(e: ItemSpawnEvent) {
        if (e.entity.itemStack == Items.BACK_TO_LAST_CHECKPOINT
            || e.entity.itemStack == Items.RESET
            || e.entity.itemStack == Items.CANCEL) {
            e.entity.remove()
        }
    }

    private fun formatTime(i: Int): String {
        val minutes = floor(i / 60000F).toInt().toString().let { if (it.length == 1) "0$it" else it }
        val seconds = floor(i / 1000F % 60).toInt().toString().let { if (it.length == 1) "0$it" else it }
        var ms = (i % 1000).toString()
        if (ms.length == 2) {
            ms += "0"
        } else if (ms.length == 1) {
            ms += "00"
        }
        return "$minutes:$seconds.$ms"
    }

    private fun Location.toReadableString() = "${ChatColor.YELLOW}${this.blockX}${ChatColor.GRAY}, ${ChatColor.YELLOW}${this.blockY}${ChatColor.GRAY}, ${ChatColor.YELLOW}${this.blockZ}"
}
