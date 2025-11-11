package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GravityAxeListener implements Listener {
    private static final String AXE_NAME = GravityAxeCommand.AXE_NAME;
    private static final int COOLDOWN = 15; // seconds
    private static final int PULL_RADIUS = 8;
    private static final double PULL_STRENGTH = 1.5;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.NETHERITE_AXE) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "gravity_axe"), PersistentDataType.BYTE)) return;

        // Block usage in any WorldGuard region
        if (isInAnyRegion(player.getLocation())) {
            player.sendActionBar(ChatColor.RED + "You cannot use the Gravity Axe in protected regions!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(uuid) && (now - cooldowns.get(uuid)) < COOLDOWN * 1000) {
            long timeLeft = COOLDOWN - ((now - cooldowns.get(uuid)) / 1000);
            player.sendActionBar(ChatColor.RED + "Cooldown: " + timeLeft + "s");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        // Effects
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 1);

        // Pull entities
        for (Entity entity : player.getNearbyEntities(PULL_RADIUS, PULL_RADIUS, PULL_RADIUS)) {
            if (entity.equals(player)) continue;
            Vector v = player.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(PULL_STRENGTH);
            entity.setVelocity(v);
            entity.getWorld().spawnParticle(Particle.PORTAL, entity.getLocation(), 10, 0.5, 0.5, 0.5, 0.1);
            entity.getWorld().spawnParticle(Particle.REVERSE_PORTAL, entity.getLocation(), 5, 0.5, 0.5, 0.5, 0.1);
        }

        // Area effect
        for (int i = 0; i < 36; i++) {
            double angle = Math.toRadians(i * 10);
            double x = PULL_RADIUS * Math.cos(angle);
            double z = PULL_RADIUS * Math.sin(angle);
            player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation().add(x, 1, z), 1, 0, 0, 0, 0);
        }

        cooldowns.put(uuid, now);
        player.sendActionBar(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "GRAVITATIONAL PULL!");
    }

    /**
     * Check if a location is in any WorldGuard region
     * @param location The location to check
     * @return true if the location is in any region, false otherwise
     */
    private boolean isInAnyRegion(org.bukkit.Location location) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            var adaptedLoc = BukkitAdapter.adapt(location);
            return !query.getApplicableRegions(adaptedLoc).getRegions().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }
}