package com.customweapons;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.ChatColor;
import java.util.HashMap;
import java.util.UUID;

public class ScytheListener implements Listener {
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN = 60_000; // 60 seconds in ms
    private static final int BLINDNESS_DURATION = 10 * 20; // 10 seconds in ticks

    @EventHandler
    public void onPlayerRightClickEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.NETHERITE_HOE || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName() || !item.getItemMeta().getDisplayName().equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Scythe")) {
            return;
        }
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid) && now - cooldowns.get(uuid) < COOLDOWN) {
            long secondsLeft = (COOLDOWN - (now - cooldowns.get(uuid))) / 1000;
            player.sendActionBar(ChatColor.RED + "Cooldown: " + secondsLeft + "s");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }
        Player target = (Player) event.getRightClicked();
        target.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, BLINDNESS_DURATION, 1));
        player.sendMessage(org.bukkit.ChatColor.DARK_PURPLE + "You have blinded " + target.getName() + "!");
        target.sendMessage(org.bukkit.ChatColor.DARK_PURPLE + "You have been blinded by " + player.getName() + "'s Scythe!");
        cooldowns.put(uuid, now);
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
    }
} 