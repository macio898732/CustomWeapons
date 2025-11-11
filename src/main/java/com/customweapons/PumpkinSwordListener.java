package com.customweapons;

import com.customweapons.CustomWeapons;
import com.customweapons.ColorUtil;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldedit.bukkit.BukkitAdapter;

import java.util.*;

public class PumpkinSwordListener implements Listener {
    private final NamespacedKey pumpkinSwordKey;
    private final Map<UUID, Boolean> cursedPumpkin = new HashMap<>();

    public PumpkinSwordListener(NamespacedKey pumpkinSwordKey) {
        this.pumpkinSwordKey = pumpkinSwordKey;
    }

    // Removed manual inventory click handler since Curse of Binding handles this automatically

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        ItemStack tool = attacker.getInventory().getItemInMainHand();
        if (tool == null || !tool.hasItemMeta() || !tool.getItemMeta().getPersistentDataContainer().has(pumpkinSwordKey, PersistentDataType.BYTE)) return;

        // Block usage in protected regions (attacker or victim inside any WorldGuard region)
        if (isInAnyRegion(attacker.getLocation()) || isInAnyRegion(victim.getLocation())) {
            attacker.sendMessage(ColorUtil.color("&cYou cannot use the Pumpkin Sword in protected regions!"));
            return;
        }

        // Do not work on Crown of Anarchy wearer
        ItemStack victimHelmetCheck = victim.getInventory().getHelmet();
        if (victimHelmetCheck != null) {
            ItemMeta vhMeta = victimHelmetCheck.getItemMeta();
            if (vhMeta != null && vhMeta.getPersistentDataContainer().has(CustomWeapons.getInstance().getCrownOfAnarchyKey(), PersistentDataType.BYTE)) {
                attacker.sendMessage(ColorUtil.color("&cYou cannot place a pumpkin on a crowned player!"));
                return;
            }
        }

        if (new Random().nextInt(100) < 15) {
            PlayerInventory inv = victim.getInventory();
            ItemStack oldHelmet = inv.getHelmet();
            
            // Create pumpkin with Curse of Binding
            ItemStack cursedPumpkinItem = new ItemStack(Material.CARVED_PUMPKIN);
            ItemMeta pumpkinMeta = cursedPumpkinItem.getItemMeta();
            pumpkinMeta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            cursedPumpkinItem.setItemMeta(pumpkinMeta);
            
            inv.setHelmet(cursedPumpkinItem);
            this.cursedPumpkin.put(victim.getUniqueId(), true);

            if (oldHelmet != null && oldHelmet.getType() != Material.AIR) {
                HashMap<Integer, ItemStack> left = inv.addItem(oldHelmet);
                if (!left.isEmpty()) {
                    for (ItemStack item : left.values()) {
                        victim.getWorld().dropItemNaturally(victim.getLocation(), item);
                    }
                }
            }

            victim.playSound(victim.getLocation(), Sound.BLOCK_WOOD_PLACE, 1, 1);
            // (Optional) Add a circle of pumpkin particles here if desired

            victim.sendMessage(ColorUtil.color("&6&lA pumpkin has been placed on your head!"));
            attacker.sendMessage(ColorUtil.color("&6&lYou placed a pumpkin on your enemy's head!"));

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (cursedPumpkin.getOrDefault(victim.getUniqueId(), false)) {
                        cursedPumpkin.remove(victim.getUniqueId());
                        inv.setHelmet(null);
                        victim.sendMessage(ColorUtil.color("&6&lThe cursed pumpkin has worn off!"));
                        victim.playSound(victim.getLocation(), Sound.BLOCK_WOOD_BREAK, 1, 1);
                    }
                }
            }.runTaskLater(CustomWeapons.getInstance(), 20 * 15);
        }
    }

    /**
     * Check if a location is in any WorldGuard region
     * @param location The location to check
     * @return true if the location is in any region, false otherwise
     */
    private boolean isInAnyRegion(org.bukkit.Location location) {
        try {
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            var adaptedLoc = BukkitAdapter.adapt(location);
            return !query.getApplicableRegions(adaptedLoc).getRegions().isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cursedPumpkin.remove(event.getPlayer().getUniqueId());
    }
}