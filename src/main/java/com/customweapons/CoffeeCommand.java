package com.customweapons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;

public class CoffeeCommand implements CommandExecutor {
    public static final String COFFEE_NAME = ChatColor.translateAlternateColorCodes('&', "&x&8&0&F&F&0&0&lR&x&7&8&F&F&0&0&la&x&7&1&F&F&0&0&ld&x&6&9&F&F&0&0&li&x&6&1&F&F&0&0&lo&x&5&9&F&F&0&0&la&x&5&2&F&F&0&0&lc&x&4&A&F&F&0&0&lt&x&4&2&F&F&0&0&li&x&3&A&F&3&0&7&lv&x&3&2&E&7&0&E&le &x&2&9&D&B&1&4&lC&x&2&1&D&0&1&B&lo&x&1&9&C&4&2&2&lf&x&1&1&B&8&2&9&lf&x&0&8&A&C&2&F&le&x&0&0&A&0&3&6&le");
    public static final String[] COFFEE_LORE = Arrays.stream(new String[]{
            "&aGives Haste II and Speed II for 10 seconds",
            "&cCaffeine Crash: Slowness II and Mining Fatigue I for 15 seconds"
    }).map(s -> ChatColor.translateAlternateColorCodes('&', s)).toArray(String[]::new);
    private static final int CUSTOM_MODEL_DATA = 108;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack coffee = new ItemStack(Material.POTION, 1);
        ItemMeta meta = coffee.getItemMeta();
        meta.setDisplayName(COFFEE_NAME);
        meta.setLore(Arrays.asList(COFFEE_LORE));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "radioactive_coffee"),
                PersistentDataType.BYTE, (byte) 1
        );
        coffee.setItemMeta(meta);
        player.getInventory().addItem(coffee);
        player.sendMessage(ChatColor.GREEN + "You received a Radioactive Coffee!");
        return true;
    }
}