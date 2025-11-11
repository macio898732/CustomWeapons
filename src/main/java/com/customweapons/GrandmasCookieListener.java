package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class GrandmasCookieListener implements Listener {
    private static final String COOKIE_NAME = GrandmasCookieCommand.COOKIE_NAME;

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.COOKIE) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "grandmas_cookie"), PersistentDataType.BYTE)) return;
        event.setCancelled(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 10 * 20, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 10 * 20, 2));
        player.sendActionBar(COOKIE_NAME + ChatColor.GRAY + " makes you stronger!");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 1);
        item.setAmount(item.getAmount() - 1);
    }
}