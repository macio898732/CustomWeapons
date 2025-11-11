package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

public class BedrockBreakerListener implements Listener {
    private final CustomWeapons plugin;
    public BedrockBreakerListener(CustomWeapons plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (event.getItem() == null) {
            return;
        }
        ItemStack item = event.getItem();
        if (item.getType() != Material.WOODEN_PICKAXE) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        if (!ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(BedrockBreakerCommand.ITEM_NAME))) {
            return;
        }
        if (!meta.getPersistentDataContainer().has(plugin.getBedrockBreakerKey(), PersistentDataType.BYTE)) {
            return;
        }
        event.setCancelled(true);
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block != null && block.getType() == Material.BEDROCK) {
            block.setType(Material.AIR);
            player.getInventory().addItem(new ItemStack(Material.BEDROCK, 1));
            item.setAmount(item.getAmount() - 1);
            player.playSound(block.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.bedrockbreaker.success", "&a&lSuccessfully broke the bedrock!")));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.bedrockbreaker.fail", "&c&lThis item only works on bedrock!")));
        }
    }
}