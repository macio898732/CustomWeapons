package com.customweapons;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DragonFireworkListener implements Listener {
    private static final String FIREWORK_NAME = DragonFireworkCommand.FIREWORK_NAME;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Long> noFallUntil = new HashMap<>();

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main == null || main.getType() != Material.FIREWORK_ROCKET) return;
        ItemMeta meta = main.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "dragon_firework"), PersistentDataType.BYTE)) return;

        long now = System.currentTimeMillis();
        if (cooldowns.containsKey(player.getUniqueId()) && now - cooldowns.get(player.getUniqueId()) < 3000) {
            player.sendActionBar(ChatColor.RED + "You must wait " + ((3000 - (now - cooldowns.get(player.getUniqueId()))) / 1000 + 1) + "s before using this again!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        cooldowns.put(player.getUniqueId(), now);
        noFallUntil.put(player.getUniqueId(), now + 30000);

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 2 * 20, 0));
        player.setVelocity(player.getLocation().getDirection().setY(0.7).multiply(1.5));
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 1);

        // Remove cooldown after 3 seconds (handled by time check above)
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        Player player = (Player) event.getEntity();
        Long until = noFallUntil.get(player.getUniqueId());
        if (until != null && System.currentTimeMillis() < until) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1, 1);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.4f, 1);
            player.getWorld().spawnParticle(org.bukkit.Particle.END_ROD, player.getLocation(), 30, 0.5, 0.5, 0.5, 0.1);
        }
    }
}