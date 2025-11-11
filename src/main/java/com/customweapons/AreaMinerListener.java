package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.Set;
import java.util.EnumSet;

// WorldGuard imports
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;

public class AreaMinerListener implements Listener {
    private static final String PICK_NAME = AreaMinerCommand.PICK_NAME;

    // Blocks that must not be affected or mined next to
    private static final Set<Material> BLACKLIST = EnumSet.of(
            Material.BEDROCK,
            Material.BARRIER,
            Material.END_PORTAL,
            Material.END_PORTAL_FRAME,
            Material.COMMAND_BLOCK,
            Material.COMMAND_BLOCK_MINECART
    );

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || tool.getType() != Material.DIAMOND_PICKAXE) {
            return;
        }
        ItemMeta meta = tool.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        if (!ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(PICK_NAME))) {
            return;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "area_miner"), PersistentDataType.BYTE)) {
            return;
        }
        Block center = event.getBlock();
        if (isInRegion(center)) {
            player.sendMessage(ChatColor.RED + "You cannot use the Area Miner inside a protected region!");
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        // Determine the 3x3 plane based on player facing
        org.bukkit.util.Vector dir = player.getLocation().getDirection();
        double absX = Math.abs(dir.getX());
        double absY = Math.abs(dir.getY());
        double absZ = Math.abs(dir.getZ());
        int[][] offsets;
        if (absY > absX && absY > absZ) {
            // Looking mostly up/down: XZ plane (horizontal)
            offsets = new int[][] {
                    {-1, 0, -1}, {0, 0, -1}, {1, 0, -1},
                    {-1, 0, 0},  {0, 0, 0},  {1, 0, 0},
                    {-1, 0, 1},  {0, 0, 1},  {1, 0, 1}
            };
        } else if (absX > absZ) {
            // Looking mostly east/west: YZ plane
            offsets = new int[][] {
                    {0, -1, -1}, {0, 0, -1}, {0, 1, -1},
                    {0, -1, 0},  {0, 0, 0},  {0, 1, 0},
                    {0, -1, 1},  {0, 0, 1},  {0, 1, 1}
            };
        } else {
            // Looking mostly north/south: XY plane
            offsets = new int[][] {
                    {-1, -1, 0}, {0, -1, 0}, {1, -1, 0},
                    {-1, 0, 0},  {0, 0, 0},  {1, 0, 0},
                    {-1, 1, 0},  {0, 1, 0},  {1, 1, 0}
            };
        }

        // If any block in the 3x3 area is blacklist, cancel the whole action
        for (int[] offset : offsets) {
            Block check = center.getWorld().getBlockAt(center.getX() + offset[0], center.getY() + offset[1], center.getZ() + offset[2]);
            if (BLACKLIST.contains(check.getType())) {
                player.sendMessage(ChatColor.RED + "Area Miner cannot be used next to unbreakable/blacklisted blocks.");
                return;
            }
        }

        for (int[] offset : offsets) {
            Block b = center.getWorld().getBlockAt(center.getX() + offset[0], center.getY() + offset[1], center.getZ() + offset[2]);
            if (b.getType() == Material.AIR) continue;
            if (BLACKLIST.contains(b.getType())) continue; // never break blacklisted
            b.breakNaturally(tool);
        }
    }

    // Helper method to check if a block is inside any WorldGuard region
    private boolean isInRegion(Block block) {
        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(block.getWorld()));
        if (regionManager == null) return false;
        ApplicableRegionSet set = regionManager.getApplicableRegions(BukkitAdapter.asBlockVector(block.getLocation()));
        return set.size() > 0;
    }
}