package com.customweapons;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class UnbreakableWallListener implements Listener {
    private final Map<UUID, Integer> cooldowns = new HashMap<>();
    private final Set<UUID> invincible = new HashSet<>();
    private static final int COOLDOWN = 90; // seconds

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.GOLDEN_AXE || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();

        // Check for the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", UnbreakableWallCommand.TAG_KEY);
        if (meta == null || !meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) return;

        UUID uuid = player.getUniqueId();
        if (cooldowns.getOrDefault(uuid, 0) > 0) {
            player.sendActionBar("§c§lCooldown: §f" + cooldowns.get(uuid) + "s");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        cooldowns.put(uuid, COOLDOWN);
        invincible.add(uuid);

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
        player.sendTitle("§6§lUNBREAKABLE", "§7You are invincible for 10 seconds!", 10, 40, 10);

        // Particle effect
        for (int i = 0; i < 36; i++) {
            double angle = Math.toRadians(i * 10);
            double x = player.getLocation().getX() + 2 * Math.cos(angle);
            double z = player.getLocation().getZ() + 2 * Math.sin(angle);
            player.getWorld().spawnParticle(Particle.FLAME, x, player.getLocation().getY() + 1, z, 1, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.END_ROD, x, player.getLocation().getY() + 1, z, 1, 0, 0, 0, 0);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                invincible.remove(uuid);
                player.sendMessage("§c§lHammers effect has worn off!");
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
            }
        }.runTaskLater(CustomWeapons.getInstance(), 20 * 10);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (invincible.contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 0.5f, 1);
            for (int i = 0; i < 36; i++) {
                double angle = Math.toRadians(i * 10);
                double x = player.getLocation().getX() + 2 * Math.cos(angle);
                double z = player.getLocation().getZ() + 2 * Math.sin(angle);
                player.getWorld().spawnParticle(Particle.SMOKE, x, player.getLocation().getY() + 1, z, 1, 0, 0, 0, 0);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cooldowns.remove(uuid);
        invincible.remove(uuid);
    }

    public UnbreakableWallListener() {
        // Start cooldown timer
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<Map.Entry<UUID, Integer>> it = cooldowns.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<UUID, Integer> entry = it.next();
                    if (entry.getValue() > 0) {
                        cooldowns.put(entry.getKey(), entry.getValue() - 1);
                    } else {
                        it.remove();
                    }
                }
            }
        }.runTaskTimer(CustomWeapons.getInstance(), 20, 20);
    }
}