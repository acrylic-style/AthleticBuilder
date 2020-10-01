package xyz.acrylicstyle.athleticBuilder.util

import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object Items {
    val BACK_TO_LAST_CHECKPOINT: ItemStack = ItemStack(Material.IRON_PLATE)
    val RESET: ItemStack = ItemStack(Material.WOOD_DOOR)
    val CANCEL: ItemStack = ItemStack(Material.BARRIER)

    init {
        val meta1 = BACK_TO_LAST_CHECKPOINT.itemMeta
        meta1.displayName = "${ChatColor.GREEN}チェックポイントに戻る ${ChatColor.GRAY}(右クリック)"
        BACK_TO_LAST_CHECKPOINT.itemMeta = meta1
        val meta2 = RESET.itemMeta
        meta2.displayName = "${ChatColor.GREEN}最初の地点に戻る ${ChatColor.GRAY}(右クリック)"
        RESET.itemMeta = meta2
        val meta3 = CANCEL.itemMeta
        meta3.displayName = "${ChatColor.GREEN}アスレチックをやめる ${ChatColor.GRAY}(右クリック)"
        CANCEL.itemMeta = meta3
    }
}
