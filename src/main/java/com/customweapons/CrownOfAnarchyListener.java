package com.customweapons;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.checkerframework.checker.lock.qual.Holding;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CrownOfAnarchyListener implements Listener {
    private static final String CROWN_NAME = CrownOfAnarchyCommand.CROWN_NAME;
    private final Set<UUID> crowned = new HashSet<>();
    private final Map<UUID, DropAttempt> dropAttempts = new HashMap<>();
    private final String webhookUrl = "https://discord.com/api/webhooks/1407878630905876550/bYGj4kdFn-ZyyG5IktDdOzhUqeLyBBGWsqTWvjlt6u5HprNJQyeOpxCFwUhl0zdAx_Kv"; // Replace with your Discord webhook URL

    private static class DropAttempt {
        int count = 0;
        long firstAttempt = 0;
        
        public boolean canDrop() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - firstAttempt > 10000) { // 10 seconds
                reset();
                return false;
            }
            return count >= 3;
        }
        
        public void increment() {
            if (count == 0) {
                firstAttempt = System.currentTimeMillis();
            }
            count++;
        }
        
        public void reset() {
            count = 0;
            firstAttempt = 0;
        }
        
        public int getRemainingAttempts() {
            return Math.max(0, 3 - count);
        }
    }

    // Check on join
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("CustomWeapons"), () -> {
            checkCrown(event.getPlayer());
        }, 20L);
    }

    // Check on inventory click
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Bukkit.getScheduler().runTaskLater(Bukkit.getPluginManager().getPlugin("CustomWeapons"), () -> {
            checkCrown((Player) event.getWhoClicked());
        }, 2L);
    }

    // Remove effects on quit
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeCrownEffects(event.getPlayer());
        crowned.remove(event.getPlayer().getUniqueId());
        dropAttempts.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (isCrownOfAnarchy(droppedItem)) {
            Player player = event.getPlayer();
            UUID playerId = player.getUniqueId();
            
            DropAttempt attempt = dropAttempts.getOrDefault(playerId, new DropAttempt());
            attempt.increment();
            dropAttempts.put(playerId, attempt);
            
                         if (attempt.canDrop()) {
                 player.sendMessage(ChatColor.GREEN + "You have successfully dropped the " + CROWN_NAME + ChatColor.GREEN + "!");
                 dropAttempts.remove(playerId);
                                   // Send Discord webhook for successful drop
                  sendDiscordWebhook("👑 **Crown Dropped!** 👑 ",
                      "**" + player.getName() + "** has dropped the Crown of Anarchy at " + 
                      formatLocation(player.getLocation()) + "\n\n" +
                      "The crown is now available for pickup!",
                      0xFF0000); // Red color
            } else {
                int remaining = attempt.getRemainingAttempts();
                if (remaining > 0) {
                    player.sendMessage(ChatColor.YELLOW + "Drop attempt " + (3 - remaining) + "/3. Press Q " + remaining + " more time(s) within 10 seconds to drop the crown.");
                } else {
                    player.sendMessage(ChatColor.RED + "Drop attempt failed. You must press Q 3 times within 10 seconds to drop the crown.");
                    attempt.reset();
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        ItemStack pickedItem = event.getItem().getItemStack();
        if (isCrownOfAnarchy(pickedItem)) {
            Player player = event.getPlayer();
            // Send Discord webhook for pickup
            sendDiscordWebhook("👑 **Crown Claimed!** 👑",
                "**" + player.getName() + "** has picked up the Crown of Anarchy!\n\n" +
                "The crown now belongs to **" + player.getName() + "**!", 
                0x00FF00); // Green color
        }
    }



    private boolean isCrownOfAnarchy(ItemStack item) {
        if (item == null || item.getType() != Material.GOLDEN_HELMET) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(CustomWeapons.getInstance().getCrownOfAnarchyKey(), PersistentDataType.BYTE);
    }

    private void checkCrown(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet != null && isCrownOfAnarchy(helmet)) {
            if (!crowned.contains(player.getUniqueId())) {
                applyCrownEffects(player);
                crowned.add(player.getUniqueId());
            }
            return;
        }
        if (crowned.contains(player.getUniqueId())) {
            removeCrownEffects(player);
            crowned.remove(player.getUniqueId());
        }
    }

    private void applyCrownEffects(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, true, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, true, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 2, true, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 0, true, false, false));
        player.sendMessage(ChatColor.GOLD + "You feel the power of the " + CROWN_NAME + ChatColor.GOLD + "!");
    }

    private void removeCrownEffects(Player player) {
        player.removePotionEffect(PotionEffectType.SPEED);
        player.removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.removePotionEffect(PotionEffectType.LUCK);
        player.sendMessage(ChatColor.RED + "You feel the power of the " + CROWN_NAME + ChatColor.RED + " leave you!");
    }

    private void sendDiscordWebhook(String title, String description, int color) {
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("YOUR_DISCORD_WEBHOOK_URL_HERE")) {
            return; // Skip if webhook URL is not configured
        }
        
        Bukkit.getScheduler().runTaskAsynchronously(Bukkit.getPluginManager().getPlugin("CustomWeapons"), () -> {
            try {
                // Escape special characters in JSON
                String escapedTitle = title.replace("\"", "\\\"");
                String escapedDescription = description.replace("\"", "\\\"").replace("\n", "\\n");
                



                String json = "{\"embeds\":[{\"title\":\"" + escapedTitle + "\",\"description\":\"" + escapedDescription + "\",\"color\":" + color + ",\"timestamp\":\"" + java.time.Instant.now().toString() + "\"}]}";
                
                java.net.URL url = new java.net.URL(webhookUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "Minecraft-Server");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                try (java.io.OutputStream os = connection.getOutputStream()) {
                    byte[] input = json.getBytes("UTF-8");
                    os.write(input, 0, input.length);
                }
                
                int responseCode = connection.getResponseCode();
                if (responseCode == 204) {
                    Bukkit.getLogger().info("Discord webhook sent successfully: " + title);
                } else {
                    Bukkit.getLogger().warning("Discord webhook failed with response code: " + responseCode);
                }
                
                connection.disconnect();
            } catch (Exception e) {
                Bukkit.getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private String formatLocation(org.bukkit.Location location) {
        return "X: " + Math.round(location.getX()) + 
               ", Y: " + Math.round(location.getY()) + 
               ", Z: " + Math.round(location.getZ()) + 
               " (" + location.getWorld().getName() + ")";
    }


}