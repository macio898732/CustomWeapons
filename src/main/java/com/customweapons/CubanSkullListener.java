package com.customweapons;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Sound;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CubanSkullListener implements Listener {
    private static final String SKULL_NAME = CubanSkullCommand.SKULL_NAME;

    // Track glass locations we placed so we only protect those
    private final Set<Location> pluginPlacedGlass = ConcurrentHashMap.newKeySet();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block b = event.getBlock();
        if (b.getType() != Material.CYAN_STAINED_GLASS) return;

        Location loc = b.getLocation();
        if (pluginPlacedGlass.contains(loc)) {
            // Prevent breaking plugin-placed glass while the trap is active
            event.setCancelled(true);
            Player p = event.getPlayer();
            p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    new net.md_5.bungee.api.chat.TextComponent(ChatColor.RED + "You cannot break this trapped glass!"));
        } else {
            // allow breaking normal cyan glass
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player)) return;
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool == null || tool.getType() != Material.PLAYER_HEAD) return;
        ItemMeta meta = tool.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(SKULL_NAME))) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "cuban_skull"), PersistentDataType.BYTE)) return;

        // Get uses from lore
        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) return;
        String usesLine = lore.get(0).replace(ChatColor.translateAlternateColorCodes('&', "&7Uses remaining: &a"), "");
        int uses;
        try {
            uses = Integer.parseInt(ChatColor.stripColor(usesLine));
        } catch (NumberFormatException e) {
            uses = 0;
        }
        if (uses <= 0) {
            player.getInventory().remove(tool);
            player.sendTitle(ChatColor.RED + "✖ No Charges!", ChatColor.GRAY + "This skull is depleted", 10, 40, 10);
            return;
        }

        // Decrease uses and update lore
        uses--;
        meta.setLore(Arrays.asList(
                ChatColor.translateAlternateColorCodes('&', "&7Uses remaining: &a") + uses,
                ChatColor.translateAlternateColorCodes('&', "&7Click a player to trap them")
        ));
        tool.setItemMeta(meta);

        Player target = (Player) event.getRightClicked();
        Location loc = target.getLocation();

        // Create hollow glass sphere
        List<Block> toSet = new ArrayList<>();
        for (int x = -7; x <= 7; x++) {
            for (int y = -7; y <= 7; y++) {
                for (int z = -7; z <= 7; z++) {
                    Location l = loc.clone().add(x, y, z);
                    double dist = l.distance(loc);
                    if (dist > 6 && dist < 7) {
                        Block b = l.getBlock();
                        if (b.getType() == Material.AIR) {
                            toSet.add(b);
                        }
                    }
                }
            }
        }

        // Place glass and record locations we placed
        for (Block b : toSet) {
            b.setType(Material.CYAN_STAINED_GLASS);
            pluginPlacedGlass.add(b.getLocation());
        }

        player.sendTitle(ChatColor.GREEN + "TRAPPED!", ChatColor.YELLOW + target.getName() + " has been trapped!", 10, 40, 10);
        target.sendTitle(ChatColor.RED + "TRAPPED!", ChatColor.GRAY + "Trapped for 6 seconds!", 10, 40, 10);

        // Warnings and gradual removal schedule using plugin instance
        CustomWeapons plugin = CustomWeapons.getInstance();

        // 2 seconds before vanish (at 12s)
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendTitle(ChatColor.YELLOW + "WARNING!", ChatColor.GRAY + "2 seconds remaining", 10, 40, 10);
            target.sendTitle(ChatColor.YELLOW + "WARNING!", ChatColor.GRAY + "2 seconds remaining", 10, 40, 10);
        }, 12 * 20L);

        // final warning at 13s
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.sendTitle(ChatColor.RED + "FINAL WARNING!", ChatColor.GRAY + "Disappearing in 1 second!", 10, 40, 10);
            target.sendTitle(ChatColor.RED + "FINAL WARNING!", ChatColor.GRAY + "Disappearing in 1 second!", 10, 40, 10);
        }, 13 * 20L);

        // After 14s begin slow top-to-bottom break with calcite break sound
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Group blocks by Y level
            Map<Integer, List<Block>> byY = new HashMap<>();
            for (Block b : toSet) {
                if (b.getType() != Material.CYAN_STAINED_GLASS) {
                    // ignore blocks that were changed by other means
                    pluginPlacedGlass.remove(b.getLocation());
                    continue;
                }
                int y = b.getY();
                byY.computeIfAbsent(y, k -> new ArrayList<>()).add(b);
            }

            // sort Y levels descending (top to bottom)
            List<Integer> yLevels = new ArrayList<>(byY.keySet());
            Collections.sort(yLevels, Collections.reverseOrder());

            long layerDelay = 0L;
            long delayBetweenLayers = 4L; // 4 ticks between layers (adjustable)

            for (int i = 0; i < yLevels.size(); i++) {
                final List<Block> layerBlocks = byY.get(yLevels.get(i));
                final long scheduledDelay = layerDelay + (i * delayBetweenLayers);

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (Block b : layerBlocks) {
                        // only remove if still plugin-placed cyan glass
                        if (b.getType() == Material.CYAN_STAINED_GLASS && pluginPlacedGlass.contains(b.getLocation())) {
                            pluginPlacedGlass.remove(b.getLocation());
                            b.setType(Material.AIR);
                            // play calcite break sound at block
                            b.getWorld().playSound(b.getLocation().add(0.5, 0.5, 0.5), Sound.BLOCK_CALCITE_BREAK, 1.0f, 1.0f);
                        }
                    }
                }, scheduledDelay);
            }

            // schedule final titles after all layers done
            long totalDelay = layerDelay + (yLevels.size() * delayBetweenLayers) + 2L;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.sendTitle(ChatColor.GREEN + "FREED!", ChatColor.GRAY + "The sphere has disappeared", 10, 40, 10);
                target.sendTitle(ChatColor.GREEN + "FREE!", ChatColor.GRAY + "The sphere has disappeared", 10, 40, 10);
            }, totalDelay);
        }, 14 * 20L);

        // Remove item if no uses left
        if (uses == 0) {
            player.getInventory().remove(tool);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null || item.getType() != Material.PLAYER_HEAD) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(SKULL_NAME))) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "cuban_skull"), PersistentDataType.BYTE)) return;
        // Cancel placement
        event.setCancelled(true);
        Player player = event.getPlayer();
        // Action bar message
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(ChatColor.RED + "You cannot place the Cuban Skull!"));
        // Play sound
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
    }
}
