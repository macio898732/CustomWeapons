package com.customweapons;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import java.util.Collections;

public class SwapBallCommand implements CommandExecutor {
    private static final int CUSTOM_MODEL_DATA = 127;
    public static final String TAG_KEY = "swap_ball";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        if (!player.hasPermission("swapball.give")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        ItemStack ball = new ItemStack(Material.SNOWBALL);
        ItemMeta meta = ball.getItemMeta();
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        meta.setDisplayName("§x§E§E§F§9§F§B§lS§x§E§9§F§7§F§3§lW§x§E§3§F§5§E§B§lA§x§D§E§F§3§E§3§lP §x§D§C§F§2§D§F§lS§x§E§1§F§0§E§3§lN§x§E§6§E§F§E§8§lO§x§E§B§E§D§E§C§lW§x§E§5§E§F§E§F§lB§x§D§A§F§1§F§1§lA§x§C§E§F§4§F§4§lL§x§C§3§F§6§F§6§lL");
        meta.setLore(Collections.singletonList("§7Throw this to swap places with a player!"));
        // Add the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", TAG_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        ball.setItemMeta(meta);
        player.getInventory().addItem(ball);
        player.sendMessage("§aYou have received the §x§E§E§F§9§F§B§lS§x§E§9§F§7§F§3§lW§x§E§3§F§5§E§B§lA§x§D§E§F§3§E§3§lP §x§D§C§F§2§D§F§lS§x§E§1§F§0§E§3§lN§x§E§6§E§F§E§8§lO§x§E§B§E§D§E§C§lW§x§E§5§E§F§E§F§lB§x§D§A§F§1§F§1§lA§x§C§E§F§4§F§4§lL§x§C§3§F§6§F§6§lL§a!");
        return true;
    }
}