package com.customweapons;

import com.customweapons.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import com.customweapons.*;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.Sound;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.inventory.meta.ItemMeta;

public class TrollsRoseListener implements Listener {
    private final NamespacedKey roseKey;
    private final Set<UUID> holdingRose = new HashSet<>();

    public TrollsRoseListener(NamespacedKey roseKey) {
        this.roseKey = roseKey;
        startRoseTask();
    }

    private void startRoseTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    boolean hasRose = isHoldingRose(player.getInventory().getItemInMainHand()) ||
                            isHoldingRose(player.getInventory().getItemInOffHand());
                    if (hasRose) {
                        if (!holdingRose.contains(player.getUniqueId())) {
                            holdingRose.add(player.getUniqueId());
                            player.sendMessage(ColorUtil.color("&aYou feel the power of the &x&0&0&9&9&2&B&lT&x&0&D&9&1&2&B&lr&x&1&9&8&9&2&C&lo&x&2&6&8&2&2&C&ll&x&3&2&7&A&2&D&ll&x&3&F&7&2&2&D&l'&x&4&B&6&A&2&D&ls &x&5&8&6&2&2&E&lr&x&6&4&5&B&2&E&lo&x&7&1&5&3&2&F&ls&x&7&D&4&B&2&F&le!"));
                        }
                        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 25, 0, true, false, true));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 25, 0, true, false, true));
                    } else if (holdingRose.contains(player.getUniqueId())) {
                        holdingRose.remove(player.getUniqueId());
                        player.sendMessage(ColorUtil.color("&cThe power of the &x&0&0&9&9&2&B&lT&x&0&D&9&1&2&B&lr&x&1&9&8&9&2&C&lo&x&2&6&8&2&2&C&ll&x&3&2&7&A&2&D&ll&x&3&F&7&2&2&D&l'&x&4&B&6&A&2&D&ls &x&5&8&6&2&2&E&lr&x&6&4&5&B&2&E&lo&x&7&1&5&3&2&F&ls&x&7&D&4&B&2&F&le fades away..."));
                    }
                }
            }
        }.runTaskTimer(CustomWeapons.getInstance(), 0L, 20L);
    }

    private boolean isHoldingRose(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(roseKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        holdingRose.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(roseKey, PersistentDataType.BYTE)) return;
        // Cancel placement
        event.setCancelled(true);
        Player player = event.getPlayer();
        // Action bar message
        player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
            new net.md_5.bungee.api.chat.TextComponent(ColorUtil.color("&cYou cannot place the Troll's Rose!")));
        // Play sound
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 1.0f);
    }
}