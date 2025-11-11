package com.customweapons;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class TotemOfPardonListener implements Listener {
    private final Map<UUID, List<ItemStack>> savedInventories = new HashMap<>();
    private final Map<UUID, ItemStack[]> savedArmor = new HashMap<>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();
        boolean found = false;

        NamespacedKey key = new NamespacedKey("customweapons", TotemOfPardonCommand.TAG_KEY);

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
                    found = true;
                    // Remove one totem
                    if (item.getAmount() > 1) {
                        item.setAmount(item.getAmount() - 1);
                    } else {
                        contents[i] = null;
                    }
                    break;
                }
            }
        }

        if (found) {
            // Save inventory and armor, but filter out the Totem of Pardon
            List<ItemStack> filtered = new ArrayList<>();
            for (ItemStack stack : inv.getContents()) {
                if (stack == null || stack.getType() == Material.AIR) continue;
                if (stack.getType() == Material.TOTEM_OF_UNDYING && stack.hasItemMeta()) {
                    ItemMeta meta = stack.getItemMeta();
                    if (meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE)) {
                        // Skip this totem (do not save it)
                        continue;
                    }
                }
                filtered.add(stack);
            }
            savedInventories.put(player.getUniqueId(), filtered);
            savedArmor.put(player.getUniqueId(), inv.getArmorContents());

            event.getDrops().clear();
            event.setKeepInventory(false);

            player.sendMessage("§6§lYour Totem of Pardon has saved your inventory!");
            Bukkit.broadcastMessage("§6" + player.getName() + " was saved from losing their items by a §lTotem of Pardon!");
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (savedInventories.containsKey(uuid)) {
            Bukkit.getScheduler().runTaskLater(CustomWeapons.getInstance(), () -> {
                player.getInventory().clear();
                List<ItemStack> items = savedInventories.remove(uuid);
                if (items != null) {
                    for (ItemStack item : items) {
                        if (item != null && item.getType() != Material.AIR) {
                            // Don't add armor items to inventory since they're handled separately
                            if (!isArmorItem(item)) {
                                player.getInventory().addItem(item);
                            }
                        }
                    }
                }
                ItemStack[] armor = savedArmor.remove(uuid);
                if (armor != null) {
                    player.getInventory().setArmorContents(armor);
                }
            }, 2L);
        }
    }
    
    private boolean isArmorItem(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type.name().contains("HELMET") || 
               type.name().contains("CHESTPLATE") || 
               type.name().contains("LEGGINGS") || 
               type.name().contains("BOOTS") ||
               type.name().contains("SHIELD");
    }
}