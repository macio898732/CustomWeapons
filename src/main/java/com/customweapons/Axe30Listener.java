package com.customweapons;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatMessageType;

public class Axe30Listener implements Listener {
    private final CustomWeapons plugin;
    private final Map<UUID, Integer> cooldowns = new HashMap<>();
    private final int cooldownSeconds;

    public Axe30Listener(CustomWeapons plugin) {
        this.plugin = plugin;
        this.cooldownSeconds = plugin.getConfig().getInt("axe30.cooldown", 60);
        startCooldownTask();
    }

    private void startCooldownTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                cooldowns.entrySet().removeIf(entry -> {
                    int time = entry.getValue() - 1;
                    if (time <= 0) return true;
                    cooldowns.put(entry.getKey(), time);
                    return false;
                });
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity target = event.getRightClicked();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.NETHERITE_AXE) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.isUnbreakable()) return;
        if (!meta.getDisplayName().contains("Executioner's Axe")) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(plugin, "executioners_axe"), PersistentDataType.BYTE)) return;
        if (!(target instanceof Player)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.axe30.not_player", "&cYou must target a player to use this ability!")));
            return;
        }

        // WorldGuard check: disallow using ability inside ANY WorldGuard region (checks both user and victim locations).
        Player victim = (Player) target;
        try {
            if (isInWorldGuardRegion(player.getLocation()) || isInWorldGuardRegion(victim.getLocation())) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.axe30.disabled_region", "&cYou cannot use this inside a WorldGuard region.")));
                return;
            }
        } catch (Throwable ignored) {
            // If something goes wrong with reflection/worldguard detection, just continue (do not crash plugin).
        }

        UUID uuid = player.getUniqueId();
        if (cooldowns.getOrDefault(uuid, 0) > 0) {
            int time = cooldowns.get(uuid);
            // Show cooldown in action bar instead of chat
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "Cooldown: " + time + "s"));
            return;
        }

        double maxHealth = victim.getAttribute(Attribute.MAX_HEALTH).getValue();
        double damage = maxHealth * 0.33;

        // Apply damage that ignores armor by directly reducing the player's health.
        double currentHealth = victim.getHealth();
        double newHealth = Math.max(0.0, currentHealth - damage);
        // If newHealth is 0, set to 0 to trigger death; otherwise set the reduced health.
        victim.setHealth(newHealth);

        // Keep knockback, sounds and other features unchanged
        victim.setVelocity(victim.getVelocity().setY(0.5));
        Bukkit.getOnlinePlayers().forEach(p -> p.playSound(victim.getLocation(), Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1, 1));
        // Play a noteblock sound for both user and victim
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1, 1);
        victim.playSound(victim.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, SoundCategory.PLAYERS, 1, 1);
        cooldowns.put(uuid, cooldownSeconds);
        player.sendTitle(ChatColor.RED + "§lExecutioner's Axe", ChatColor.GREEN + "Dealt " + (int)damage + " damage!", 10, 40, 10);
        victim.sendTitle(ChatColor.RED + "§lUNDER ATTACK!", ChatColor.RED + player.getName() + " stole 33% of your health!", 10, 40, 10);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.axe30.used", "&c&lExecutioner's Axe used! Dealt %damage% damage! Cooldown: %cooldown% seconds").replace("%damage%", String.valueOf((int)damage)).replace("%cooldown%", String.valueOf(cooldownSeconds))));
    }
    private boolean isInWorldGuardRegion(Location loc) {
        try {
            // WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object wgInstance = worldGuardClass.getMethod("getInstance").invoke(null);
            Object platform = wgInstance.getClass().getMethod("getPlatform").invoke(wgInstance);
            Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);
            Object query = regionContainer.getClass().getMethod("createQuery").invoke(regionContainer);

            // adapt location: com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(Location)
            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Object adapted = bukkitAdapterClass.getMethod("adapt", Location.class).invoke(null, loc);

            // query.getApplicableRegions(adapted)
            Object applicable = query.getClass().getMethod("getApplicableRegions", adapted.getClass()).invoke(query, adapted);

            // applicable.getRegions().iterator().hasNext() -> returns true if in a region
            Object regions = applicable.getClass().getMethod("getRegions").invoke(applicable);
            if (regions instanceof Iterable) {
                return ((Iterable<?>) regions).iterator().hasNext();
            } else {
                // Fallback: try calling size() or isEmpty() by reflection
                try {
                    int size = (int) regions.getClass().getMethod("size").invoke(regions);
                    return size > 0;
                } catch (NoSuchMethodException nsme) {
                    return false;
                }
            }
        } catch (ClassNotFoundException cnfe) {
            // WorldGuard or BukkitAdapter not present
            return false;
        } catch (Throwable t) {
            // Any other reflection error -> treat as "not in region" to avoid blocking functionality
            return false;
        }
    }
}