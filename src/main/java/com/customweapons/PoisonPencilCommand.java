package com.customweapons;

import org.bukkit.ChatColor;
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
import java.util.Arrays;

public class PoisonPencilCommand implements CommandExecutor {
    private static final int CUSTOM_MODEL_DATA = 123;
    public static final String PENCIL_NAME = ChatColor.translateAlternateColorCodes('&',
            "&x&0&0&F&B&0&A&lP&x&0&6&E&2&0&7&lo&x&0&D&C&A&0&5&li&x&1&3&B&1&0&2&ls&x&1&5&A&1&0&5&lo&x&0&F&9&F&1&3&ln &x&0&8&9&E&2&2&lP&x&0&2&9&C&3&0&le&x&0&0&A&C&3&2&ln&x&0&0&C&5&2&F&lc&x&0&0&D&D&2&B&li&x&0&0&F&6&2&7&ll");
    public static final String[] PENCIL_LORE = Arrays.stream(new String[]{
            "&7Hit players to poison them!",
            "&7Cooldown: 30 seconds"
    }).map(s -> ChatColor.translateAlternateColorCodes('&', s)).toArray(String[]::new);
    public static final String TAG_KEY = "poison_pencil";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack pencil = new ItemStack(Material.WOODEN_SWORD, 1);
        ItemMeta meta = pencil.getItemMeta();
        meta.setDisplayName(PENCIL_NAME);
        meta.setLore(Arrays.asList(PENCIL_LORE));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ATTRIBUTES);
        // Add the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", TAG_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        pencil.setItemMeta(meta);
        player.getInventory().addItem(pencil);
        player.sendMessage(ChatColor.GREEN + "You have received a Poison Pencil!");
        return true;
    }
}