package com.customweapons;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.event.block.Action;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.bukkit.plugin.Plugin;
import org.bukkit.Location;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WitherSkullListener implements Listener {
    private static final String NAME = ChatColor.BLACK + "" + ChatColor.BOLD + "Wither Shot";
    private static final long COOLDOWN_MS = 45_000L;
    private final Map<UUID, Long> lastUse = new ConcurrentHashMap<>();

    // short shot lock to prevent the duplicate projectile spawn (fires for ~2 ticks)
    private final Set<UUID> shotLock = ConcurrentHashMap.newKeySet();

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        ItemStack placed = e.getItemInHand();
        if (placed == null || placed.getType() != Material.WITHER_SKELETON_SKULL) return;
        ItemMeta meta = placed.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        if (NAME.equals(meta.getDisplayName())) {
            e.setCancelled(true);
            if (e.getPlayer() != null) {
                e.getPlayer().sendMessage(ChatColor.RED + "You cannot place the " + NAME);
            }
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getHand() != null && e.getHand() != EquipmentSlot.HAND) return;
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.WITHER_SKELETON_SKULL) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !NAME.equals(meta.getDisplayName())) return;

        // cancel the event immediately to prevent default behavior spawning another skull
        e.setCancelled(true);

        UUID id = p.getUniqueId();

        // prevent very-fast duplicate firing due to multiple event calls
        if (!shotLock.add(id)) {
            // already recently shot, ignore
            return;
        }
        // remove lock after 2 ticks
        Bukkit.getScheduler().runTaskLater(CustomWeapons.getInstance(), () -> shotLock.remove(id), 2L);

        long now = System.currentTimeMillis();
        long readyAt = lastUse.getOrDefault(id, 0L);
        if (now < readyAt) {
            long remaining = readyAt - now;
            int secs = (int) Math.max(1, Math.ceil(remaining / 1000.0));
            String msg = ChatColor.RED + "Wither Shot cooldown: " + ChatColor.YELLOW + secs + "s";
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(msg));
            return;
        }

        // compute spawn location/direction
        Location eye = p.getEyeLocation();
        Vector dir = eye.getDirection().normalize();
        Location spawnLoc = eye.clone().add(dir.multiply(0.5));

        // WorldGuard check: refuse use if spawn loc is inside any region (reflection so WG is optional)
        if (isInWorldGuardRegion(spawnLoc)) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                    ChatColor.RED + "You cannot use Wither Shot inside a WorldGuard region"));
            return;
        }

        // spawn and tag our custom skull
        Entity spawned = p.getWorld().spawnEntity(spawnLoc, EntityType.WITHER_SKULL);
        if (!(spawned instanceof WitherSkull)) {
            // fallback: try launching normally
            WitherSkull fallback = p.launchProjectile(WitherSkull.class);
            fallback.setVelocity(fallback.getVelocity().multiply(1.4));
            lastUse.put(id, now + COOLDOWN_MS);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                    ChatColor.GREEN + "Wither Shot used! Cooldown: 45s"));
            return;
        }

        WitherSkull skull = (WitherSkull) spawned;

        // tag using PersistentDataContainer so we can identify "our" skull
        skull.getPersistentDataContainer().set(CustomWeapons.getInstance().getWitherSkullKey(), PersistentDataType.BYTE, (byte) 1);

        // set shooter and velocity
        try {
            skull.setShooter(p);
        } catch (NoSuchMethodError ignored) {}
        skull.setVelocity(dir.multiply(1.4));

        // schedule a cleanup tick to remove any nearby untagged wither skulls (duplicate from default behavior)
        Bukkit.getScheduler().runTaskLater(CustomWeapons.getInstance(), () -> {
            for (Entity nearby : p.getWorld().getNearbyEntities(spawnLoc, 1.5, 1.5, 1.5)) {
                if (nearby.getType() != EntityType.WITHER_SKULL) continue;
                if (nearby.equals(skull)) continue;
                // remove only untagged ones
                if (!nearby.getPersistentDataContainer().has(CustomWeapons.getInstance().getWitherSkullKey(), PersistentDataType.BYTE)) {
                    nearby.remove();
                }
            }
        }, 1L);

        lastUse.put(id, now + COOLDOWN_MS);
        String usedMsg = ChatColor.GREEN + "Wither Shot used! Cooldown: 45s";
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(usedMsg));
    }

    @EventHandler
    public void onProjectileHit(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Projectile proj;
        if (damager instanceof Projectile) {
            proj = (Projectile) damager;
        } else {
            return;
        }

        if (proj.getType() != EntityType.WITHER_SKULL) return;

        // only apply effect if this is our tagged skull
        if (!proj.getPersistentDataContainer().has(CustomWeapons.getInstance().getWitherSkullKey(), PersistentDataType.BYTE)) return;

        Entity target = e.getEntity();
        if (!(target instanceof Player)) return;

        Player victim = (Player) target;
        // Apply Wither V for 15 seconds (amplifier zero-based -> 4 == level 5)
        victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 15 * 20, 9), true);

        // optional: remove the skull on hit so it won't trigger again
        try {
            proj.remove();
        } catch (Throwable ignored) {}
    }

    // Reflection-based check for WorldGuard regions; returns true if 'loc' is inside any region.
    private boolean isInWorldGuardRegion(Location loc) {
        try {
            Plugin wg = Bukkit.getPluginManager().getPlugin("WorldGuard");
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