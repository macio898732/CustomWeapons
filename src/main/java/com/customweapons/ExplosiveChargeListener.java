package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

public class ExplosiveChargeListener implements Listener {
    private static final String CHARGE_NAME = ExplosiveChargeCommand.CHARGE_NAME;
    private static final String TAG_KEY = ExplosiveChargeCommand.TAG_KEY;

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
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.FIRE_CHARGE) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Check for the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", TAG_KEY);
        if (!meta.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.BYTE)) return;

        event.setCancelled(true);

        // Check if player is in a protected region
        if (isInAnyRegion(player.getLocation())) {
            player.sendMessage(ChatColor.RED + "You cannot use " + CHARGE_NAME + ChatColor.RED + " in protected regions!");
            return;
        }

        // Check for spawn region (replace with your own region check if needed)
        String worldName = player.getWorld().getName().toLowerCase();
        if (worldName.contains("spawn")) {
            player.sendMessage(ChatColor.RED + "You cannot use " + CHARGE_NAME + ChatColor.RED + " in spawn!");
            return;
        }

        // Remove one charge
        item.setAmount(item.getAmount() - 1);

        Location loc = player.getLocation();
        player.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
        player.getWorld().createExplosion(loc, 4F, false, false, player);

        // Remove blocks in radius except protected types
        for (Block block : BlockUtils.getBlocksInRadius(loc, 4)) {
            Material type = block.getType();
            if (type != Material.BEDROCK && type != Material.BARRIER && type != Material.END_PORTAL_FRAME
                    && type != Material.END_PORTAL && type != Material.COMMAND_BLOCK && type != Material.STRUCTURE_BLOCK) {
                block.setType(Material.AIR);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null || item.getType() != Material.FIRE_CHARGE) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Check for the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", TAG_KEY);
        if (!meta.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.BYTE)) return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.RED + "You cannot place this " + CHARGE_NAME + ChatColor.RED + "!");
    }
}