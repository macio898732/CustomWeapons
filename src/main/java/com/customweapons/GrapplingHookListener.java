package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GrapplingHookListener implements Listener {
    private static final String HOOK_NAME = GrapplingHookCommand.HOOK_NAME;
    private static final int COOLDOWN = 5; // seconds
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Boolean> noFall = new HashMap<>();

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.FISHING_ROD) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.isUnbreakable()) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "grappling_hook"), PersistentDataType.BYTE)) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid) && (now - cooldowns.get(uuid)) < COOLDOWN * 1000) {
            event.setCancelled(true);
            long timeLeft = COOLDOWN - ((now - cooldowns.get(uuid)) / 1000);
            player.sendActionBar(ChatColor.RED + "Cooldown: " + timeLeft + "s");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        if (event.getState() == PlayerFishEvent.State.REEL_IN || event.getState() == PlayerFishEvent.State.IN_GROUND) {
            FishHook hook = event.getHook();
            Location hookLoc = hook.getLocation();
            Location playerLoc = player.getLocation();
            org.bukkit.util.Vector vector = hookLoc.toVector().subtract(playerLoc.toVector()).normalize().multiply(1.8);
            player.setVelocity(vector);
            cooldowns.put(uuid, now);
            noFall.put(uuid, true);
            player.sendMessage(ChatColor.GREEN + "Grappling Hook Activated!");
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && noFall.getOrDefault(player.getUniqueId(), false)) {
            event.setCancelled(true);
            noFall.remove(player.getUniqueId());
        }
    }
}