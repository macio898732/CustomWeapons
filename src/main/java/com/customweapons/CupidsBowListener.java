package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Random;

public class CupidsBowListener implements Listener {
    private static final String BOW_NAME = CupidsBowCommand.BOW_NAME;
    private final Random random = new Random();

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player victim = (Player) event.getEntity();

        // Check if damage is from an arrow
        if (event.getDamager() instanceof org.bukkit.entity.Arrow arrow) {
            if (!(arrow.getShooter() instanceof Player attacker)) return;
            ItemStack bow = attacker.getInventory().getItemInMainHand();
            if (bow == null || bow.getType() != Material.BOW) return;
            ItemMeta meta = bow.getItemMeta();
            if (meta == null || !meta.hasDisplayName() || !meta.isUnbreakable()) return;
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "cupids_bow"), PersistentDataType.BYTE)) return;

            // 33% chance
            if (random.nextInt(100) < 33) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 7 * 20, 1));
                attacker.sendTitle(ChatColor.LIGHT_PURPLE + "Weakened!", ChatColor.GRAY + "You weakened " + victim.getName() + "!", 10, 40, 10);
                victim.sendTitle(ChatColor.RED + "Weakened!", ChatColor.GRAY + "By " + attacker.getName() + "'s Cupid's Bow!", 10, 40, 10);
                victim.getWorld().playSound(victim.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
            } else {
                attacker.sendMessage(ChatColor.RED + "❤ The love arrow had no effect!");
            }
        }
    }
}