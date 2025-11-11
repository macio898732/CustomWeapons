package com.customweapons;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.RayTraceResult;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DragonBoneListener implements Listener {
    private static final String NAME = ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Dragon Bone";
    private static final long COOLDOWN_MS = 30_000L;
    private final Map<UUID, Long> lastUse = new ConcurrentHashMap<>();
    private static final double MAX_DISTANCE = 12.0;

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        // only main-hand right-click
        if (e.getHand() != null && e.getHand() != EquipmentSlot.HAND) return;
        switch (e.getAction()) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                break;
            default:
                return;
        }

        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BONE) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        if (!NAME.equals(meta.getDisplayName())) return;

        // prevent default interactions / double behavior
        e.setCancelled(true);

        UUID id = p.getUniqueId();
        long now = System.currentTimeMillis();
        long readyAt = lastUse.getOrDefault(id, 0L);
        if (now < readyAt) {
            long remaining = readyAt - now;
            int secs = (int) Math.max(1, Math.ceil(remaining / 1000.0));
            String msg = ChatColor.RED + "Dragon Bone cooldown: " + ChatColor.YELLOW + secs + "s";
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
            return;
        }

        // Raytrace to find the block being aimed at (up to MAX_DISTANCE)
        RayTraceResult res = p.getWorld().rayTraceBlocks(
                p.getEyeLocation(),
                p.getEyeLocation().getDirection(),
                MAX_DISTANCE,
                FluidCollisionMode.NEVER,
                true
        );

        if (res == null || res.getHitBlock() == null) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                    ChatColor.RED + "No valid target within " + (int) MAX_DISTANCE + " blocks"));
            return;
        }

        Block hit = res.getHitBlock();
        BlockFace face = res.getHitBlockFace();
        // destination is the block space adjacent to the face that was hit (where player's feet should be)
        Block destination = (face != null) ? hit.getRelative(face) : hit;

        // Ensure destination blocks are safe: destination and above must not be solid (no wall/roof)
        Block headBlock = destination.getRelative(BlockFace.UP);
        boolean destinationBlocked = destination.getType().isSolid();
        boolean headBlocked = headBlock.getType().isSolid();

        if (destinationBlocked || headBlocked) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                    ChatColor.RED + "Cannot teleport: target is blocked"));
            return;
        }

        Location targetLoc = destination.getLocation().add(0.5, 0.0, 0.5);

        // WorldGuard check: refuse teleport if either current location or target location is inside a WG region
        if (isInWorldGuardRegion(p.getLocation()) || isInWorldGuardRegion(targetLoc)) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                    ChatColor.RED + "You cannot use Dragon Bone inside a WorldGuard region"));
            return;
        }

        // Teleport player to center of destination block (preserve yaw/pitch)
        targetLoc.setYaw(p.getLocation().getYaw());
        targetLoc.setPitch(p.getLocation().getPitch());
        p.teleport(targetLoc);

        // play teleport sound at destination
        p.playSound(targetLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        // set cooldown and display message
        lastUse.put(id, now + COOLDOWN_MS);
        String usedMsg = ChatColor.GREEN + "Dragon Bone used! Cooldown: " + ChatColor.YELLOW + (COOLDOWN_MS / 1000) + "s";
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(usedMsg));
    }

    // Reflection-based check for WorldGuard regions; returns true if 'loc' is inside any region.
    private boolean isInWorldGuardRegion(Location loc) {
        try {
            Plugin wg = org.bukkit.Bukkit.getPluginManager().getPlugin("WorldGuard");
            if (wg == null) return false;
            // call getRegionManager(World)
            Object regionManager = wg.getClass().getMethod("getRegionManager", org.bukkit.World.class).invoke(wg, loc.getWorld());
            if (regionManager == null) return false;
            // call getApplicableRegions(Location)
            Object applicable = regionManager.getClass().getMethod("getApplicableRegions", org.bukkit.Location.class).invoke(regionManager, loc);
            if (applicable == null) return false;
            // call getRegions() on the applicable set and check emptiness
            Object regions = applicable.getClass().getMethod("getRegions").invoke(applicable);
            if (regions instanceof java.util.Collection) {
                return !((java.util.Collection<?>) regions).isEmpty();
            }
        } catch (NoSuchMethodException ignored) {
        } catch (Throwable ignored) {
        }
        return false;
    }
}