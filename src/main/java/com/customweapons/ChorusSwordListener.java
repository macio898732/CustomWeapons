package com.customweapons;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChorusSwordListener implements Listener {
    private static final String SWORD_NAME = ChorusSwordCommand.SWORD_NAME;
    private static final int FREEZE_SECONDS = 3;
    private static final int COOLDOWN_SECONDS = 45;
    private final Map<UUID, Integer> cooldowns = new HashMap<>();
    private final Map<UUID, Location> frozenPlayers = new HashMap<>();

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();
        ItemStack item = attacker.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.NETHERITE_SWORD) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.isUnbreakable()) return;
        if (!ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(SWORD_NAME))) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "chorus_sword"), PersistentDataType.BYTE)) return;
        UUID attackerId = attacker.getUniqueId();
        if (cooldowns.containsKey(attackerId)) {
            int timeLeft = cooldowns.get(attackerId);
            attacker.sendActionBar(ChatColor.RED + "Cooldown: " + timeLeft + "s");
            attacker.playSound(attacker.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }
        // Freeze victim
        frozenPlayers.put(victim.getUniqueId(), victim.getLocation());
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, FREEZE_SECONDS * 20, 255, false, false, false));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, FREEZE_SECONDS * 20, 255, false, false, false));
        attacker.playSound(attacker.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        victim.playSound(victim.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);
        attacker.sendTitle(ChatColor.DARK_PURPLE + "Frozen!", ChatColor.GRAY + "You froze " + victim.getName() + " for 3 seconds!", 10, 40, 10);
        victim.sendTitle(ChatColor.RED + "Frozen!", ChatColor.GRAY + "You were frozen by " + attacker.getName() + "!", 10, 40, 10);
        // Start cooldown
        cooldowns.put(attackerId, COOLDOWN_SECONDS);
        new BukkitRunnable() {
            int time = COOLDOWN_SECONDS;
            @Override
            public void run() {
                time--;
                if (time <= 0) {
                    cooldowns.remove(attackerId);
                    attacker.sendMessage(ChatColor.GREEN + "Your Chorus Sword is ready to use again!");
                    cancel();
                } else {
                    cooldowns.put(attackerId, time);
                }
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("CustomWeapons"), 20, 20);
        // Unfreeze after 3 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                frozenPlayers.remove(victim.getUniqueId());
            }
        }.runTaskLater(Bukkit.getPluginManager().getPlugin("CustomWeapons"), FREEZE_SECONDS * 20);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!frozenPlayers.containsKey(player.getUniqueId())) return;
        Location frozenLoc = frozenPlayers.get(player.getUniqueId());
        if (!event.getTo().getBlock().equals(frozenLoc.getBlock())) {
            event.setTo(frozenLoc);
            player.sendActionBar(ChatColor.RED + "You are frozen and cannot move!");
        }
    }
}