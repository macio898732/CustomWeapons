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

public class TempestHammerCommand implements CommandExecutor {
    private static final int CUSTOM_MODEL_DATA = 128;
    public static final String TAG_KEY = "tempest_hammer";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }
        if (!player.hasPermission("tempesthammer.get")) {
            player.sendMessage("§cYou don't have permission!");
            return true;
        }
        ItemStack axe = new ItemStack(Material.NETHERITE_AXE);
        ItemMeta meta = axe.getItemMeta();
        meta.setDisplayName("§x§2§1§0§0§8§0§lT§x§1§9§0§C§7§D§le§x§1§1§1§7§7§A§lm§x§0§8§2§3§7§6§lp§x§0§0§2§E§7§3§le§x§0§0§3§2§6§C§ls§x§0§0§3§6§6§5§lt §x§0§0§3§9§5§E§lH§x§0§0§3§D§5§7§la§x§0§0§3§E§5§6§lm§x§0§0§3§F§5§6§lm§x§0§0§3§F§5§5§le§x§0§0§4§0§5§4§lr");
        meta.setLore(Arrays.asList(
                "§7Right-click to summon thunder!",
                "§7Cooldown 45 seconds"
        ));
        meta.setCustomModelData(CUSTOM_MODEL_DATA);
        // Add the unique tag
        NamespacedKey key = new NamespacedKey("customweapons", TAG_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        axe.setItemMeta(meta);
        player.getInventory().addItem(axe);
        player.sendMessage("§bYou received the Tempest Hammer!");
        return true;
    }
}