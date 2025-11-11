package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.Random;

public class BloodthirsterListener implements Listener {
    private static final String SWORD_NAME = BloodthirsterCommand.SWORD_NAME;
    private static final double HEAL_AMOUNT = 3.0; // 1.5 hearts
    private static final int STEAL_CHANCE = 30;
    private final Random random = new Random();

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();
        ItemStack item = attacker.getInventory().getItemInMainHand();
        if (item == null || item.getType() != org.bukkit.Material.DIAMOND_SWORD) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(SWORD_NAME))) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "bloodthirster_sword"), PersistentDataType.BYTE)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        LivingEntity victim = (LivingEntity) event.getEntity();
        int chance = random.nextInt(100) + 1;
        if (chance > STEAL_CHANCE) return;
        double newHealth = Math.min(attacker.getHealth() + HEAL_AMOUNT, attacker.getMaxHealth());
        attacker.setHealth(newHealth);
        attacker.getWorld().playSound(attacker.getLocation(), Sound.ENTITY_WITCH_DRINK, 1, 1);
        attacker.getWorld().playSound(victim.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
        attacker.getWorld().spawnParticle(Particle.DUST, victim.getLocation().add(0, 1, 0), 36, 1, 0.5, 1, 1, new Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
        attacker.getWorld().spawnParticle(Particle.HEART, attacker.getLocation().add(0, 1, 0), 36, 1, 0.5, 1, 1);
        attacker.sendMessage(SWORD_NAME + ChatColor.GRAY + " stole some health from your enemy!");
        if (victim instanceof Player) {
            ((Player) victim).sendMessage(ChatColor.RED + attacker.getName() + " drained some of your health!");
        }
        attacker.sendTitle(ChatColor.DARK_RED + "Bloodthirst!", ChatColor.GRAY + "You stole health from your enemy!", 10, 40, 10);
    }
}