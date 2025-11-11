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
import java.util.Collections;

public class GrapplingHookCommand implements CommandExecutor {
    public static final String HOOK_NAME = ChatColor.GOLD + "" + ChatColor.BOLD + "Grappling Hook";
    public static final String HOOK_LORE = ChatColor.GRAY + "» Use this hook to pull yourself around!";
    private static final int CUSTOM_MODEL_DATA = 118;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player target;
        if (args.length > 0) {
            target = sender.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found!");
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            sender.sendMessage(ChatColor.RED + "You must specify a player!");
            return true;
        }
        ItemStack rod = new ItemStack(Material.FISHING_ROD, 1);
        ItemMeta meta = rod.getItemMeta();
        meta.setDisplayName(HOOK_NAME);
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.setLore(Collections.singletonList(HOOK_LORE));
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        // Add PDC tag for secure identification
        meta.getPersistentDataContainer().set(
                new NamespacedKey(CustomWeapons.getInstance(), "grappling_hook"),
                PersistentDataType.BYTE, (byte) 1
        );
        rod.setItemMeta(meta);
        target.getInventory().addItem(rod);
        sender.sendMessage(ChatColor.GREEN + "You have SUCCESSFULLY given " + target.getName() + " a grappling hook!");
        return true;
    }
}