package com.customweapons;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class TempestHammerListener implements Listener {
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final int COOLDOWN = 45; // seconds
    private static final int DAMAGE = 10;
    private static final int EFFECT_RADIUS = 15;
    private static final int EFFECT_DURATION = 15;

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.NETHERITE_AXE || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();

        // Check for the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", TempestHammerCommand.TAG_KEY);
        if (meta == null || !meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid) && (now - cooldowns.get(uuid)) < COOLDOWN * 1000) {
            long timeLeft = COOLDOWN - ((now - cooldowns.get(uuid)) / 1000);
            player.sendActionBar("§c§lCooldown: " + timeLeft + "s");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }
        cooldowns.put(uuid, now);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

        // Thunder effect
        player.getWorld().strikeLightningEffect(player.getLocation());
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 2, 1);

        // Damage and effects to nearby entities
        for (Entity entity : player.getNearbyEntities(EFFECT_RADIUS, EFFECT_RADIUS, EFFECT_RADIUS)) {
            if (entity instanceof Player target && !target.equals(player)) {
                target.damage(DAMAGE, player);
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, EFFECT_DURATION * 20, 1));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, EFFECT_DURATION * 20, 1));
            }
        }

        // Particle effects (circle)
        Location loc = player.getLocation();
        for (int i = 0; i < 36; i++) {
            double angle = Math.toRadians(i * 10);
            double x = loc.getX() + 3 * Math.cos(angle);
            double z = loc.getZ() + 3 * Math.sin(angle);
            Location particleLoc = new Location(loc.getWorld(), x, loc.getY(), z);
            loc.getWorld().spawnParticle(Particle.CLOUD, particleLoc, 5, 0, 0, 0, 0);
            loc.getWorld().spawnParticle(Particle.END_ROD, particleLoc, 5, 0, 0, 0, 0);
        }
        for (int i = 0; i < 16; i++) {
            double angle = Math.toRadians(i * 22.5);
            double x = loc.getX() + 2 * Math.cos(angle);
            double z = loc.getZ() + 2 * Math.sin(angle);
            Location flameLoc = new Location(loc.getWorld(), x, loc.getY(), z);
            loc.getWorld().spawnParticle(Particle.FLAME, flameLoc, 3, 0, 0, 0, 0);
        }

        player.sendTitle("§b§lTHUNDERCLAP!", "", 10, 40, 10);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}