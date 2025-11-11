package com.customweapons;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;
// WorldGuard imports
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;

public class BalloonListener implements Listener {
    private static final String BALLOON_NAME = ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Balloon";
    private static final String BALLOON_TEXTURE = "b03338e1e9ae77cb28a195790fcbc0601c6588830ca429af19205c3e0642bed7";
    private static final HashMap<UUID, Long> balloonCooldowns = new HashMap<>();

    @EventHandler
    public void onBalloonUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        // Only allow right-click actions
        if (!(event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_AIR || event.getAction() == org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.PLAYER_HEAD || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasDisplayName() || !meta.getDisplayName().equals(BALLOON_NAME)) return;
        // Per-player cooldown (1 tick)
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        if (balloonCooldowns.containsKey(uuid) && now - balloonCooldowns.get(uuid) < 50) return;
        balloonCooldowns.put(uuid, now);
        // WorldGuard region check
        WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
        if (wg != null) {
            com.sk89q.worldedit.util.Location wgLoc = BukkitAdapter.adapt(player.getLocation());
            com.sk89q.worldguard.protection.managers.RegionManager regionManager =
                WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));
            if (regionManager != null) {
                ApplicableRegionSet regions = regionManager.getApplicableRegions(wgLoc.toVector().toBlockPoint());
                boolean inRegion = false;
                for (ProtectedRegion region : regions) {
                    if (!region.getId().equalsIgnoreCase("__global__")) {
                        inRegion = true;
                        break;
                    }
                }
                if (inRegion) {
                    player.sendMessage(ChatColor.RED + "You cannot use the balloon in a protected region!");
                    return;
                }
            }
        }
        event.setCancelled(true);
        Bukkit.getLogger().info("[BalloonDebug] Player " + player.getName() + " used balloon with action: " + event.getAction());
        ItemStack heldHead = player.getInventory().getItemInMainHand().clone();
        // Remove the balloon from the player's hand (one-time use)
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getAmount() > 1) {
            hand.setAmount(hand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
        Location loc = player.getLocation().add(0, 2, 0);
        ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class, as -> {
            as.setInvisible(true);
            as.setInvulnerable(true);
            as.setGravity(false);
            as.setMarker(true);
            as.setSmall(true);
            as.setHelmet(heldHead);
        });
        new BukkitRunnable() {
            double distance = 0;
            double lastBlockDelete = 0;
            Location current = stand.getLocation();
            float yaw = stand.getLocation().getYaw();
            @Override
            public void run() {
                if (distance >= 20) {
                    stand.remove();
                    cancel();
                    return;
                }
                // Spin the armor stand
                yaw += 10.0f;
                if (yaw > 360.0f) yaw -= 360.0f;
                current.setYaw(yaw);
                // Move up smoothly
                current.add(0, 0.05, 0);
                distance += 0.05;
                // Particle trail just below the head
                current.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, current.clone().add(0, 0.6, 0), 2, 0.1, 0, 0.1, 0.01);
                // For every 1 block up, break 2 blocks above
                if (distance - lastBlockDelete >= 1.0) {
                    Location breakLoc = current.clone().add(0, 2, 0);
                    Block block = breakLoc.getBlock();
                    if (block.getType() != Material.AIR) {
                        block.setType(Material.AIR);
                    }
                    lastBlockDelete = distance;
                }
                Block block = current.getBlock();
                if (block.getType() != Material.AIR) {
                    block.setType(Material.AIR);
                }
                stand.teleport(current);
            }
        }.runTaskTimer(CustomWeapons.getInstance(), 0L, 1L);
    }

    private ItemStack getCustomHead() {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Balloon");
        meta.setOwningPlayer(Bukkit.getOfflinePlayer("Wutt"));
        head.setItemMeta(meta);
        return head;
    }
} 