package com.customweapons;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;

public class WarmMilkCommand implements CommandExecutor {
    private static final int CUSTOM_MODEL_DATA = 132;
    public static final String TAG_KEY = "warm_milk";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        if (!player.hasPermission("warmmilk.give")) {
            player.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        ItemStack bowl = new ItemStack(Material.BOWL);
        ItemMeta meta = bowl.getItemMeta();
        meta.setDisplayName("§x§B§1§B§9§C§5§lW§x§D§2§D§3§D§C§la§x§F§4§E§C§F§3§lr§x§E§6§E§A§F§1§lm §x§C§0§D§8§E§2§lM§x§B§0§D§1§D§A§li§x§C§D§D§D§E§2§ll§x§E§9§E§9§E§9§lk");
        meta.setLore(Arrays.asList(
                "§7A warm cup of milk",
                "§7that removes negative effects"
        ));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", TAG_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        bowl.setItemMeta(meta);
        player.getInventory().addItem(bowl);
        player.sendMessage("§7You received a §x§B§1§B§9§C§5§lW§x§D§2§D§3§D§C§la§x§F§4§E§C§F§3§lr§x§E§6§E§A§F§1§lm §x§C§0§D§8§E§2§lM§x§B§0§D§1§D§A§li§x§C§D§D§D§E§2§ll§x§E§9§E§9§E§9§lk!");
        return true;
    }
}