package com.customweapons;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

public class SwapBallListener implements Listener {
    private static final String SWAP_BALL_NAME = "§x§E§E§F§9§F§B§lS§x§E§9§F§7§F§3§lW§x§E§3§F§5§E§B§lA§x§D§E§F§3§E§3§lP §x§D§C§F§2§D§F§lS§x§E§1§F§0§E§3§lN§x§E§6§E§F§E§8§lO§x§E§B§E§D§E§C§lW§x§E§5§E§F§E§F§lB§x§D§A§F§1§F§1§lA§x§C§E§F§4§F§4§lL§x§C§3§F§6§F§6§lL";

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
            // Get all regions at this location
            return !query.getApplicableRegions(adaptedLoc).getRegions().isEmpty();
        } catch (Exception e) {
            // If WorldGuard is not available or there's an error, assume it's not protected
            return false;
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball ball)) return;
        if (!(ball.getShooter() instanceof Player shooter)) return;

        if (event.getHitEntity() instanceof Player victim) {
            ItemStack hand = shooter.getInventory().getItemInMainHand();
            if (!isSwapBall(hand)) hand = shooter.getInventory().getItemInOffHand();
            if (!isSwapBall(hand)) return;

            // Check if shooter is in any region
            if (isInAnyRegion(shooter.getLocation())) {
                shooter.sendMessage(ChatColor.RED + "You cannot use " + SWAP_BALL_NAME + ChatColor.RED + " in protected regions!");
                ball.remove();
                return;
            }

            // Check if victim is in any region
            if (isInAnyRegion(victim.getLocation())) {
                shooter.sendMessage(ChatColor.RED + "Cannot swap with players in protected regions!");
                ball.remove();
                return;
            }

            // Swap locations
            var shooterLoc = shooter.getLocation();
            var victimLoc = victim.getLocation();
            shooter.teleport(victimLoc);
            victim.teleport(shooterLoc);

            // Titles
            shooter.sendTitle("§bSwapped!", "§7You swapped places with " + victim.getName() + "!", 10, 40, 10);
            victim.sendTitle("§cSwapped!", "§7You were swapped with " + shooter.getName() + "!", 10, 40, 10);

            // Sounds
            shooter.getWorld().playSound(shooterLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
            victim.getWorld().playSound(victimLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        } else {
            shooter.sendTitle("§cMissed!", "§7Your snowball didn't hit anyone!", 10, 40, 10);
        }
    }

    private boolean isSwapBall(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey("customweapons", SwapBallCommand.TAG_KEY);
        return meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}