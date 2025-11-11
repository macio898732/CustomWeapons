package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class CoffeeListener implements Listener {
    private static final String COFFEE_NAME = CoffeeCommand.COFFEE_NAME;

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.POTION) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "radioactive_coffee"), PersistentDataType.BYTE)) return;
        event.setCancelled(true);
        // Apply positive effects
        player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 10 * 20, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 1));
        player.sendActionBar(ChatColor.GREEN + "Feeling radioactively energized!");
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_DRINK, 1, 1);
        item.setAmount(item.getAmount() - 1);
        // Apply negative effects after 10 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 15 * 20, 1));
                player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 15 * 20, 0));
                player.sendActionBar(ChatColor.RED + "Radioactive crash!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BREATH, 1, 1);
            }
        }.runTaskLater(org.bukkit.Bukkit.getPluginManager().getPlugin("CustomWeapons"), 10 * 20);
    }
}