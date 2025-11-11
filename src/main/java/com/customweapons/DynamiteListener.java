// ...existing code...
package com.customweapons;

import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashSet;
import java.util.Set;

public class DynamiteListener implements Listener {
    private static final String DYNAMITE_NAME = DynamiteCommand.DYNAMITE_NAME;
    private static final Set<Item> activeDynamite = new HashSet<>();
    private static final NamespacedKey DYNAMITE_KEY = new NamespacedKey(CustomWeapons.getInstance(), "dynamite");

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.RED_CANDLE) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (!meta.getPersistentDataContainer().has(DYNAMITE_KEY, PersistentDataType.BYTE)) return;

        event.setCancelled(true);

        // Remove one dynamite from the player's inventory (robust removal)
        removeOneDynamite(player);

        spawnThrownDynamite(player);
    }

    private void removeOneDynamite(Player player) {
        PlayerInventory inv = player.getInventory();

        // First try main hand and off hand explicitly (common case)
        ItemStack main = inv.getItemInMainHand();
        if (isDynamiteStack(main)) {
            main.setAmount(Math.max(0, main.getAmount() - 1));
            inv.setItemInMainHand(main.getAmount() > 0 ? main : null);
            return;
        }
        ItemStack off = inv.getItemInOffHand();
        if (isDynamiteStack(off)) {
            off.setAmount(Math.max(0, off.getAmount() - 1));
            inv.setItemInOffHand(off.getAmount() > 0 ? off : null);
            return;
        }

        // Otherwise search inventory contents and decrement the first match
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack stack = contents[i];
            if (isDynamiteStack(stack)) {
                stack.setAmount(Math.max(0, stack.getAmount() - 1));
                inv.setItem(i, stack.getAmount() > 0 ? stack : null);
                return;
            }
        }
    }

    private boolean isDynamiteStack(ItemStack stack) {
        if (stack == null || stack.getType() != Material.RED_CANDLE) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(DYNAMITE_KEY, PersistentDataType.BYTE);
    }

    private void spawnThrownDynamite(Player player) {
        Location dropLoc = player.getLocation().add(player.getLocation().getDirection().normalize().multiply(1));
        ItemStack thrown = new ItemStack(Material.RED_CANDLE, 1);
        ItemMeta thrownMeta = thrown.getItemMeta();
        if (thrownMeta != null) {
            thrownMeta.setDisplayName(DYNAMITE_NAME);
            thrownMeta.getPersistentDataContainer().set(DYNAMITE_KEY, PersistentDataType.BYTE, (byte) 1);
            thrown.setItemMeta(thrownMeta);
        }
        Item dropped = player.getWorld().dropItem(dropLoc, thrown);
        dropped.setPickupDelay(400000);
        dropped.setVelocity(player.getLocation().getDirection().normalize().multiply(0.6));
        activeDynamite.add(dropped);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!dropped.isDead() && dropped.isValid()) {
                    dropped.getWorld().createExplosion(dropped.getLocation(), 4F, false, true, player);
                    dropped.remove();
                }
                activeDynamite.remove(dropped);
            }
        }.runTaskLater(CustomWeapons.getInstance(), 60L);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (activeDynamite.contains(event.getItem())) {
            event.setCancelled(true);
        }
    }
}