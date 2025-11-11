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

public class CubanSkullCommand implements CommandExecutor {
    public static final String SKULL_NAME = ChatColor.translateAlternateColorCodes('&',
            "&x&2&C&2&C&2&C&lC&x&7&C&7&C&7&C&lu&x&C&C&C&C&C&C&lb&x&8&2&8&2&8&2&la&x&3&8&3&8&3&8&ln");
    public static final String[] SKULL_LORE = Arrays.stream(new String[]{
            "&7Uses remaining: &a3",
            "&7Click a player to trap them"
    }).map(s -> ChatColor.translateAlternateColorCodes('&', s)).toArray(String[]::new);
    private static final int CUSTOM_MODEL_DATA = 110;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;
        ItemStack head = player.getInventory().getItemInMainHand();
        if (head.getType() != Material.PLAYER_HEAD) {
            player.sendMessage(ChatColor.RED + "You must be holding a player head!");
            return true;
        }
        ItemMeta meta = head.getItemMeta();
        meta.setDisplayName(SKULL_NAME);
        meta.setLore(Arrays.asList(SKULL_LORE));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "cuban_skull"),
                PersistentDataType.BYTE, (byte) 1
        );
        head.setItemMeta(meta);
        player.setItemInHand(head);
        player.sendMessage(ChatColor.GREEN + "Your skull has been enchanted with " + SKULL_NAME + ChatColor.GREEN + " powers!");
        return true;
    }
}