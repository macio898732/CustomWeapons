package com.customweapons;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.enchantments.Enchantment;

import java.util.Collections;

public class DashSwordCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final NamespacedKey key;

    public DashSwordCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "dash_sword");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }
        Player p = (Player) sender;

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§x§0§0§F§B§F§3§lD§x§0§0§E§9§F§5§la§x§0§0§D§8§F§6§ls§x§0§0§C§6§F§8§lh §x§0§0§B§5§F§9§lS§x§0§0§A§3§F§B§lw§x§0§0§9§1§F§C§lo§x§0§0§8§0§F§E§lr§x§0§0§6§E§F§F§ld");
            meta.setLore(Collections.singletonList("§7Right-click to dash forward"));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.setUnbreakable(true);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "dash_sword");
            sword.setItemMeta(meta);
        }

        p.getInventory().addItem(sword);
        p.sendMessage("Given Dash Sword.");
        return true;
    }
}