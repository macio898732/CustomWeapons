package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

public class CarrotCrossbowListener implements Listener {
    private static final String CROSSBOW_NAME = CarrotCrossbowCommand.CROSSBOW_NAME;

    /**
     * Check if a location is in any WorldGuard region
     * @param location The location to check
     * @return true if the location is in any region, false otherwise
     */
    private boolean isInAnyRegion(Location location) {
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
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main == null || main.getType() != Material.CROSSBOW) return;
        ItemMeta meta = main.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.isUnbreakable()) return;
        if (!ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(CROSSBOW_NAME))) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "carrot_crossbow"), PersistentDataType.BYTE)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow)) return;
        Arrow arrow = (Arrow) event.getEntity();
        if (!(arrow.getShooter() instanceof Player)) return;
        Player shooter = (Player) arrow.getShooter();
        ItemStack main = shooter.getInventory().getItemInMainHand();
        if (main == null || main.getType() != Material.CROSSBOW) return;
        ItemMeta meta = main.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.isUnbreakable()) return;
        if (!ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(CROSSBOW_NAME))) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "carrot_crossbow"), PersistentDataType.BYTE)) return;

        // Check if shooter is in a protected region
        if (isInAnyRegion(shooter.getLocation())) {
            shooter.sendMessage(ChatColor.RED + "You cannot use " + CROSSBOW_NAME + ChatColor.RED + " in protected regions!");
            arrow.remove();
            return;
        }

        if (event.getHitEntity() instanceof Player) {
            Player victim = (Player) event.getHitEntity();
            
            // Check if victim is in a protected region
            if (isInAnyRegion(victim.getLocation())) {
                shooter.sendMessage(ChatColor.RED + "Cannot pull players from protected regions!");
                arrow.remove();
                return;
            }
            
            arrow.remove();
            victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            victim.teleport(shooter.getLocation());
            shooter.sendTitle(ChatColor.GOLD + "Pulled!", ChatColor.GRAY + "You pulled " + victim.getName() + " toward you!", 10, 40, 10);
            victim.sendTitle(ChatColor.RED + "Pulled!", ChatColor.GRAY + "You were pulled toward " + shooter.getName() + "!", 10, 40, 10);
        } else {
            arrow.remove();
            shooter.sendTitle(ChatColor.RED + "Missed!", ChatColor.GRAY + "Your arrow didn't hit anyone!", 10, 40, 10);
        }
    }
}