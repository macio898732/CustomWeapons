package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PoisonPencilListener implements Listener {
    private static final String PENCIL_NAME = PoisonPencilCommand.PENCIL_NAME;
    private static final String TAG_KEY = PoisonPencilCommand.TAG_KEY;
    private static final int COOLDOWN = 30; // seconds
    private final Map<UUID, Boolean> cooldowns = new HashMap<>();

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        ItemStack tool = attacker.getInventory().getItemInMainHand();
        if (tool == null || tool.getType() != Material.WOODEN_SWORD) return;
        ItemMeta meta = tool.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.isUnbreakable()) return;
        // Check for the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", TAG_KEY);
        if (!meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) return;

        UUID uuid = attacker.getUniqueId();
        if (cooldowns.getOrDefault(uuid, false)) {
            attacker.sendActionBar(ChatColor.RED + "Cooldown!");
            attacker.playSound(attacker.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        // Apply poison
        victim.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 10 * 20, 2));
        attacker.sendTitle(ChatColor.GREEN + "Poisoned!", ChatColor.GRAY + "You poisoned " + victim.getName() + "!", 10, 40, 10);
        victim.sendTitle(ChatColor.RED + "You are poisoned!", ChatColor.GRAY + "By " + attacker.getName() + "!", 10, 40, 10);
        attacker.playSound(attacker.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);

        // Set cooldown
        cooldowns.put(uuid, true);
        new BukkitRunnable() {
            @Override
            public void run() {
                cooldowns.put(uuid, false);
                attacker.sendMessage(ChatColor.GREEN + "Your Poison Pencil is ready!");
            }
        }.runTaskLater(org.bukkit.Bukkit.getPluginManager().getPlugin("CustomWeapons"), COOLDOWN * 20L);
    }
}