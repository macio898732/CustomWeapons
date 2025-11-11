package com.customweapons;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

public class WarmMilkListener implements Listener {
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BOWL || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();

        // Check for the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", WarmMilkCommand.TAG_KEY);
        if (meta == null || !meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) return;

        event.setCancelled(true);
        item.setAmount(item.getAmount() - 1);

        // Remove negative effects
        PotionEffectType[] toRemove = {
                PotionEffectType.POISON, PotionEffectType.WEAKNESS, PotionEffectType.MINING_FATIGUE,
                PotionEffectType.SLOWNESS, PotionEffectType.NAUSEA, PotionEffectType.BLINDNESS,
                PotionEffectType.DARKNESS, PotionEffectType.GLOWING, PotionEffectType.UNLUCK, PotionEffectType.WITHER
        };
        for (PotionEffectType type : toRemove) {
            player.removePotionEffect(type);
        }
        player.sendMessage("§aAll negative effects have been removed!");
    }
}