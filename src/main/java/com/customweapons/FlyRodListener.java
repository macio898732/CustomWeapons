package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FlyRodListener implements Listener {
    private static final String ROD_NAME = FlyRodCommand.ROD_NAME;
    private static final int DISABLE_DURATION = 30; // seconds
    private static final int COOLDOWN = 90; // seconds
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Long> elytraDisabled = new HashMap<>();

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.FISHING_ROD) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "fly_rod"), PersistentDataType.BYTE)) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid) && (now - cooldowns.get(uuid)) < COOLDOWN * 1000) {
            long timeLeft = COOLDOWN - ((now - cooldowns.get(uuid)) / 1000);
            player.sendActionBar(ChatColor.RED + "Cooldown: " + timeLeft + "s");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        Player target = (Player) event.getRightClicked();
        UUID targetId = target.getUniqueId();
        if (elytraDisabled.containsKey(targetId) && (now - elytraDisabled.get(targetId)) < DISABLE_DURATION * 1000) {
            player.sendMessage(ChatColor.RED + "This player's Elytra is already disabled!");
            return;
        }

        if (target.isGliding()) {
            target.setGliding(false);
        }

        cooldowns.put(uuid, now);
        elytraDisabled.put(targetId, now);
        target.sendMessage(ChatColor.AQUA + "Your Elytra has been disabled for " + DISABLE_DURATION + " seconds!");
        player.sendMessage(ChatColor.GREEN + "You disabled " + target.getName() + "'s Elytra!");
        player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

        // Re-enable after duration
        org.bukkit.Bukkit.getScheduler().runTaskLater(org.bukkit.Bukkit.getPluginManager().getPlugin("CustomWeapons"), () -> {
            elytraDisabled.remove(targetId);
            target.sendMessage(ChatColor.GREEN + "Your Elytra is working again!");
        }, DISABLE_DURATION * 20L);
    }

    @EventHandler
    public void onGlide(EntityToggleGlideEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (elytraDisabled.containsKey(uuid) && (now - elytraDisabled.get(uuid)) < DISABLE_DURATION * 1000) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Your Elytra is disabled and you cannot glide!");
        }
    }
}