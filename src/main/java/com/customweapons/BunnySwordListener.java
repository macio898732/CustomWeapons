package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BunnySwordListener implements Listener {
    private static final String SWORD_NAME = BunnySwordCommand.SWORD_NAME;
    private static final int COOLDOWN = 56; // seconds
    private final Map<UUID, Integer> cooldowns = new HashMap<>();
    private final Map<UUID, Long> noJumpUntil = new HashMap<>();

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();
        Player victim = (Player) event.getEntity();

        ItemStack item = attacker.getInventory().getItemInMainHand();
        if (item == null || item.getType() != org.bukkit.Material.DIAMOND_SWORD) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.isUnbreakable()) return;
        if (!ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(SWORD_NAME))) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "bunny_sword"), PersistentDataType.BYTE)) return;

        UUID attackerId = attacker.getUniqueId();
        if (cooldowns.containsKey(attackerId)) {
            int timeLeft = cooldowns.get(attackerId);
            attacker.sendActionBar(ChatColor.RED + "Cooldown: " + timeLeft + "s");
            attacker.playSound(attacker.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
            return;
        }

        // Prevent jumping for 4 seconds
        long blockUntil = System.currentTimeMillis() + 4_000L;
        noJumpUntil.put(victim.getUniqueId(), blockUntil);

        attacker.playSound(attacker.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        victim.playSound(victim.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_BELL, 1, 1);

        attacker.sendTitle(ChatColor.AQUA + "Jump Blocked!", ChatColor.GRAY + "You blocked " + victim.getName() + "'s ability to jump!", 10, 40, 10);
        victim.sendTitle(ChatColor.RED + "Jump Blocked!", ChatColor.GRAY + "Your ability to jump was blocked by " + attacker.getName() + "!", 10, 40, 10);

        // Start cooldown
        cooldowns.put(attackerId, COOLDOWN);
        new BukkitRunnable() {
            int time = COOLDOWN;
            @Override
            public void run() {
                time--;
                if (time <= 0) {
                    cooldowns.remove(attackerId);
                    attacker.sendMessage(ChatColor.GREEN + "Your Bunny Sword is ready to use again!");
                    cancel();
                } else {
                    cooldowns.put(attackerId, time);
                }
            }
        }.runTaskTimer(CustomWeapons.getInstance(), 20L, 20L);

        // Remove jump block after 4 seconds
        new BukkitRunnable() {
            @Override
            public void run() {
                noJumpUntil.remove(victim.getUniqueId());
            }
        }.runTaskLater(CustomWeapons.getInstance(), 4 * 20L);
    }

    // Detect upward movement from ground and put player back to previous location while affected.
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Long until = noJumpUntil.get(player.getUniqueId());
        if (until == null) return;
        if (System.currentTimeMillis() > until) {
            noJumpUntil.remove(player.getUniqueId());
            return;
        }

        if (event.getFrom() == null || event.getTo() == null) return;

        // Ignore small Y fluctuations and only react to noticeable upward movement
        double dy = event.getTo().getY() - event.getFrom().getY();
        if (dy <= 0.15) return;

        // Make sure player was on solid ground before attempting to jump
        Material below = event.getFrom().getBlock().getRelative(0, -1, 0).getType();
        if (below == Material.AIR || below == Material.CAVE_AIR || below == Material.VOID_AIR) return;

        // Teleport back to previous position to cancel the jump and prevent going up
        // Use the from-location to preserve rotation and horizontal position
        player.teleport(event.getFrom());
        event.setCancelled(true);
        player.sendActionBar(ChatColor.RED + "You can't jump right now!");
    }
}