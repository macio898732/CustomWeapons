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

public class DragonFireworkCommand implements CommandExecutor {
    public static final String FIREWORK_NAME = ChatColor.translateAlternateColorCodes('&', "&5&lDragon Firework");
    public static final String[] FIREWORK_LORE = Arrays.stream(new String[]{
            "&dAn overpowered firework!",
            "&dLaunches you further!",
            "&dInfinite usage!"
    }).map(s -> ChatColor.translateAlternateColorCodes('&', s)).toArray(String[]::new);
    private static final int CUSTOM_MODEL_DATA = 113;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET, 1);
        ItemMeta meta = firework.getItemMeta();
        meta.setDisplayName(FIREWORK_NAME);
        meta.setLore(Arrays.asList(FIREWORK_LORE));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "dragon_firework"),
                PersistentDataType.BYTE, (byte) 1
        );
        firework.setItemMeta(meta);
        player.getInventory().addItem(firework);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "You received a Dragon Firework!");
        return true;
    }
}