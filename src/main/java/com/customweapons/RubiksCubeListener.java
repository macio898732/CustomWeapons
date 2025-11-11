package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Sound;

import java.util.*;

public class RubiksCubeListener implements Listener {
    private static final String RUBIK_NAME = RubiksCubeCommand.RUBIK_NAME;
    private static final String TAG_KEY = RubiksCubeCommand.TAG_KEY;

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        ItemStack tool = attacker.getInventory().getItemInMainHand();
        if (tool == null || tool.getType() != Material.PLAYER_HEAD) return;
        ItemMeta meta = tool.getItemMeta();

        // Check for the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", TAG_KEY);
        if (meta == null || !meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) return;

        // Get uses from lore
        List<String> lore = meta.getLore();
        if (lore == null || lore.size() < 2) return;
        String usesLine = lore.get(1).replace(ChatColor.translateAlternateColorCodes('&', "&7Uses remaining: &a"), "");
        int uses;
        try {
            uses = Integer.parseInt(ChatColor.stripColor(usesLine));
        } catch (NumberFormatException e) {
            uses = 0;
        }
        if (uses <= 0) {
            attacker.getInventory().remove(tool);
            attacker.sendTitle(ChatColor.RED + "✖ No Charges!", ChatColor.GRAY + "This Rubik's Cube is depleted", 10, 40, 10);
            return;
        }

        // Decrease uses and update lore
        uses--;
        meta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&', "&7Hit a player to scramble their hotbar!"),
                ChatColor.translateAlternateColorCodes('&', "&7Uses remaining: &a") + uses
        ));
        tool.setItemMeta(meta);

        // Scramble hotbar
        ItemStack[] hotbar = new ItemStack[9];
        for (int i = 0; i < 9; i++) {
            hotbar[i] = victim.getInventory().getItem(i);
            victim.getInventory().setItem(i, null);
        }
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 9; i++) slots.add(i);
        Collections.shuffle(slots);
        for (int i = 0; i < 9; i++) {
            if (hotbar[i] != null) {
                int slot = slots.remove(0);
                victim.getInventory().setItem(slot, hotbar[i]);
            }
        }

        // Feedback
        attacker.sendTitle(ChatColor.GREEN + "SCRAMBLED!", ChatColor.YELLOW + "You scrambled " + victim.getName() + "'s hotbar!", 10, 40, 10);
        victim.sendTitle(ChatColor.RED + "SCRAMBLED!", ChatColor.GRAY + "Your hotbar has been scrambled!", 10, 40, 10);

        // Remove item if no uses left
        if (uses == 0) {
            attacker.getInventory().remove(tool);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey("customweapons", RubiksCubeCommand.TAG_KEY);
        if (meta == null || !meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) return;
        // Cancel placement
        event.setCancelled(true);
        Player player = event.getPlayer();
        // Action bar message
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
            new net.md_5.bungee.api.chat.TextComponent(ChatColor.RED + "You cannot place the Rubik's Cube!"));
        // Play sound
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
    }
}