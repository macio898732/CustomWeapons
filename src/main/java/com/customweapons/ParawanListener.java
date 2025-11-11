package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public class ParawanListener implements Listener {
    private static final String PARAWAN_NAME = ParawanCommand.PARAWAN_NAME;

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.FEATHER) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "parawan"), PersistentDataType.BYTE)) return;
        event.setCancelled(true);

        // Circle effect
        for (int i = 0; i < 50; i++) {
            double angle = Math.toRadians(i * 10);
            double x = 1 * Math.cos(angle);
            double z = 1 * Math.sin(angle);
            player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation().add(x, 1, z), 5, 0, 0, 0, 0);
        }

        // Push entities
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (entity.equals(player)) continue;
            Vector v = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize().multiply(2).setY(2);
            entity.setVelocity(v);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 1);
        player.getWorld().createExplosion(player.getLocation(), 0, false, false, player);
        item.setAmount(item.getAmount() - 1);
    }
}