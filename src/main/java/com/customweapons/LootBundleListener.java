package com.customweapons;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.*;
import java.util.function.Consumer;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;

public class LootBundleListener implements Listener {
    private static final String BUNDLE_NAME = ChatColor.translateAlternateColorCodes('&',
            "&x&A&4&7&3&2&A&lL&x&9&6&6&7&2&6&lo&x&8&8&5&A&2&1&lo&x&7&9&4&E&1&D&lt &x&6&B&4&1&1&8&lB&x&6&3&3&C&1&9&lu&x&6&1&3&E&2&0&ln&x&6&0&4&0&2&6&ld&x&5&E&4&2&2&D&ll&x&5&C&4&4&3&3&le");

    // stored per-player loot — fixed-size list length = MAX_PAGES * PAGE_SIZE (null = empty slot)
    private final Map<UUID, List<ItemStack>> lootbags = new HashMap<>();
    private final Set<UUID> lootbagOpen = new HashSet<>();
    private final Map<UUID, Integer> lootbagPage = new HashMap<>();

    // 28 loot slots per page (4 rows × 7 columns) -> 2 pages = 56 total slots
    private static final int PAGE_SIZE = 28;
    private static final int MAX_PAGES = 2;

    private static final ItemStack PAGE_UP = createNavItem(Material.LIME_DYE, ChatColor.GREEN + "Next Page");
    private static final ItemStack PAGE_DOWN = createNavItem(Material.RED_DYE, ChatColor.RED + "Previous Page");
    private static final ItemStack SPIT_OUT = createNavItem(Material.TNT, ChatColor.GOLD + "Spit Out All Loot");
    private static final ItemStack GRAY_PANE = createNavItem(Material.GRAY_STAINED_GLASS_PANE, " ");

    // central loot slots used by the GUI (these are the only interactable/pickable slots)
    private static final int[] LOOT_SLOTS = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34,
            37,38,39,40,41,42,43
    };

    private static ItemStack createNavItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private static ItemStack createPageBook(int page, int totalPages) {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "Page " + (page + 1) + " of " + totalPages);
            book.setItemMeta(meta);
        }
        return book;
    }

    private boolean isNavItem(ItemStack item) {
        if (item == null) return false;
        Material t = item.getType();
        // treat UI elements as nav items so they are never saved as loot
        return t == Material.LIME_DYE
                || t == Material.RED_DYE
                || t == Material.TNT
                || t == Material.GRAY_STAINED_GLASS_PANE
                || t == Material.BOOK;
    }

    // ensure storage list exists and is exactly capacity size (null entries allowed)
    private List<ItemStack> getStorage(UUID id) {
        int capacity = MAX_PAGES * PAGE_SIZE;
        List<ItemStack> list = lootbags.get(id);
        if (list == null) {
            list = new ArrayList<>(Collections.nCopies(capacity, null));
            lootbags.put(id, list);
            return list;
        }
        // expand/shrink to exact capacity while preserving existing entries in order
        if (list.size() < capacity) {
            List<ItemStack> expanded = new ArrayList<>(list);
            while (expanded.size() < capacity) expanded.add(null);
            lootbags.put(id, expanded);
            return expanded;
        } else if (list.size() > capacity) {
            List<ItemStack> truncated = new ArrayList<>(list.subList(0, capacity));
            lootbags.put(id, truncated);
            return truncated;
        }
        return list;
    }

    // save the given page's central slots from the provided inventory into storage
    private void savePage(UUID id, Inventory inv, int page) {
        if (inv == null) return;
        List<ItemStack> storage = getStorage(id);
        int totalCapacity = MAX_PAGES * PAGE_SIZE;
        for (int slotIdx = 0; slotIdx < LOOT_SLOTS.length; slotIdx++) {
            int guiSlot = LOOT_SLOTS[slotIdx];
            int globalIndex = page * PAGE_SIZE + slotIdx;
            if (globalIndex >= totalCapacity) continue;
            ItemStack item = inv.getItem(guiSlot);
            if (item == null || item.getType() == Material.AIR || isNavItem(item)) {
                storage.set(globalIndex, null);
            } else {
                storage.set(globalIndex, item.clone());
            }
        }
        lootbags.put(id, storage);
    }

    private void openLootBundle(Player player, int page) {
        UUID id = player.getUniqueId();
        List<ItemStack> storage = getStorage(id);

        // Always allow MAX_PAGES pages (so navigation buttons are visible even when empty)
        int totalPages = MAX_PAGES;

        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        lootbagPage.put(id, page);

        Inventory gui = Bukkit.createInventory(null, 54, BUNDLE_NAME);

        // Fill only border slots with gray panes
        for (int i = 0; i < 54; i++) {
            int row = i / 9, col = i % 9;
            if (row == 0 || row == 5 || col == 0 || col == 8) {
                gui.setItem(i, GRAY_PANE);
            }
        }

        // Page indicator book at slot 4
        gui.setItem(4, createPageBook(page, totalPages));

        // Place loot items in center area (slots defined by LOOT_SLOTS)
        int start = page * PAGE_SIZE;
        for (int slotIdx = 0; slotIdx < LOOT_SLOTS.length; slotIdx++) {
            int globalIndex = start + slotIdx;
            ItemStack it = null;
            if (globalIndex < storage.size()) it = storage.get(globalIndex);
            if (it != null && it.getType() != Material.AIR && !isNavItem(it)) {
                gui.setItem(LOOT_SLOTS[slotIdx], it.clone());
            } else {
                gui.setItem(LOOT_SLOTS[slotIdx], null);
            }
        }

        // Navigation buttons: red on left of last slot (index 52), green on last slot (index 53)
        // Show green only on first page (page == 0)
        // Show red only on second page (page == 1)
        if (totalPages > 1 && page == 0) {
            gui.setItem(52, GRAY_PANE);
            gui.setItem(53, PAGE_UP);
        } else if (totalPages > 1 && page == 1) {
            gui.setItem(52, PAGE_DOWN);
            gui.setItem(53, GRAY_PANE);
        } else {
            gui.setItem(52, GRAY_PANE);
            gui.setItem(53, GRAY_PANE);
        }

        // Spit out button stays at center bottom (49)
        gui.setItem(49, SPIT_OUT);

        player.openInventory(gui);
        lootbagOpen.add(id);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.BOOK) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "loot_bundle"), PersistentDataType.BYTE)) return;
        event.setCancelled(true);
        openLootBundle(player, 0);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null) return;

        // If victim has the Totem of Pardon with the correct PDC key, don't collect drops
        boolean hasTotemOfPardon = false;
        NamespacedKey pardonKey = new NamespacedKey(CustomWeapons.getInstance(), "totem_of_pardon");
        for (ItemStack item : victim.getInventory().getContents()) {
            if (item != null && item.getType() == Material.TOTEM_OF_UNDYING && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.getPersistentDataContainer().has(pardonKey, PersistentDataType.BYTE)) {
                    hasTotemOfPardon = true;
                    break;
                }
            }
        }
        if (hasTotemOfPardon) return;

        // Check killer has a loot bundle
        boolean hasBundle = false;
        for (ItemStack item : killer.getInventory().getContents()) {
            if (item != null && item.getType() == Material.BOOK && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                if (pdc.has(new NamespacedKey(CustomWeapons.getInstance(), "loot_bundle"), PersistentDataType.BYTE)) {
                    hasBundle = true;
                    break;
                }
            }
        }
        if (!hasBundle) return;

        // Collect victim's inventory into killer's bundle (do not drop items)
        event.getDrops().clear();
        UUID kid = killer.getUniqueId();
        List<ItemStack> storage = getStorage(kid);

        // Helper to place items into first available null slots (preserves positions of existing items)
        Consumer<ItemStack> placeItem = (ItemStack it) -> {
            if (it == null || it.getType() == Material.AIR || isNavItem(it)) return;
            for (int i = 0; i < storage.size(); i++) {
                if (storage.get(i) == null) {
                    storage.set(i, it.clone());
                    return;
                }
            }
            // no space -> item skipped
        };

        // Add main inventory items first
        ItemStack[] mainContents = victim.getInventory().getContents();
        for (ItemStack item : mainContents) {
            placeItem.accept(item);
        }

        // Add armor only if it's not already present in the main inventory (avoids double-adding)
        for (ItemStack armor : victim.getInventory().getArmorContents()) {
            if (armor == null || armor.getType() == Material.AIR) continue;
            boolean alreadyPresent = false;
            for (ItemStack m : mainContents) {
                if (m == null) continue;
                // check reference or similarity to avoid duplicate collection
                if (m == armor || m.isSimilar(armor)) {
                    alreadyPresent = true;
                    break;
                }
            }
            if (!alreadyPresent) placeItem.accept(armor);
        }

        // Also handle offhand item (if not already counted)
        ItemStack offhand = victim.getInventory().getItemInOffHand();
        if (offhand != null && offhand.getType() != Material.AIR) {
            boolean already = false;
            for (ItemStack m : mainContents) {
                if (m == null) continue;
                if (m == offhand || m.isSimilar(offhand)) { already = true; break; }
            }
            if (!already) placeItem.accept(offhand);
        }

        // persist storage (already in map via getStorage)
        lootbags.put(kid, storage);

        victim.getInventory().clear();
        victim.getInventory().setArmorContents(new ItemStack[4]);
        killer.sendMessage(ChatColor.GREEN + "All loot from " + victim.getName() + " has been added to your " + BUNDLE_NAME + ChatColor.GREEN + "!");
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        UUID id = player.getUniqueId();
        if (!lootbagOpen.contains(id)) return;
        if (!event.getView().getTitle().equals(BUNDLE_NAME)) return;

        int page = lootbagPage.getOrDefault(id, 0);
        Inventory inv = event.getInventory();

        // start from existing stored list, ensure exact capacity
        List<ItemStack> storage = getStorage(id);
        int totalCapacity = MAX_PAGES * PAGE_SIZE;

        // Only read central loot slots for the current page and write into the global index
        for (int slotIdx = 0; slotIdx < LOOT_SLOTS.length; slotIdx++) {
            int guiSlot = LOOT_SLOTS[slotIdx];
            ItemStack item = inv.getItem(guiSlot);
            int globalIndex = page * PAGE_SIZE + slotIdx;
            if (globalIndex < totalCapacity) {
                if (item == null || item.getType() == Material.AIR || isNavItem(item)) {
                    storage.set(globalIndex, null);
                } else {
                    storage.set(globalIndex, item.clone());
                }
            }
        }

        // Save storage back (preserve nulls/positions)
        lootbags.put(id, storage);
        lootbagOpen.remove(id);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (!event.getView().getTitle().equals(BUNDLE_NAME)) return;

        // Prevent placing a Loot Bundle into the GUI (cursor or clicked item)
        ItemStack cursor = event.getCursor();
        if (cursor != null && cursor.getType() == Material.BOOK && cursor.hasItemMeta()) {
            ItemMeta meta = cursor.getItemMeta();
            if (meta.getPersistentDataContainer().has(new NamespacedKey(CustomWeapons.getInstance(), "loot_bundle"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot put a Loot Bundle inside another Loot Bundle!");
                return;
            }
        }
        ItemStack current = event.getCurrentItem();
        if (current != null && current.getType() == Material.BOOK && current.hasItemMeta()) {
            ItemMeta meta = current.getItemMeta();
            if (meta.getPersistentDataContainer().has(new NamespacedKey(CustomWeapons.getInstance(), "loot_bundle"), PersistentDataType.BYTE)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot put a Loot Bundle inside another Loot Bundle!");
                return;
            }
        }

        int rawSlot = event.getRawSlot();
        int topSize = event.getView().getTopInventory().getSize(); // should be 54
        if (rawSlot < topSize) { // click in the loot bundle GUI
            boolean isLootSlot = false;
            for (int s : LOOT_SLOTS) if (s == rawSlot) { isLootSlot = true; break; }

            ItemStack clicked = event.getCurrentItem();

            if (!isLootSlot) {
                // navigation / button / border clicks: handle actions but don't allow taking them
                if (clicked != null) {
                    Material m = clicked.getType();
                    if (m == Material.LIME_DYE) { // Next page
                        event.setCancelled(true);
                        UUID id = player.getUniqueId();
                        int cur = lootbagPage.getOrDefault(id, 0);

                        // save current page before switching
                        Inventory top = player.getOpenInventory().getTopInventory();
                        savePage(id, top, cur);

                        openLootBundle(player, cur + 1);
                        return;
                    } else if (m == Material.RED_DYE) { // Previous page
                        event.setCancelled(true);
                        UUID id = player.getUniqueId();
                        int cur = lootbagPage.getOrDefault(id, 0);

                        // save current page before switching
                        Inventory top = player.getOpenInventory().getTopInventory();
                        savePage(id, top, cur);

                        openLootBundle(player, cur - 1);
                        return;
                    } else if (m == Material.TNT) { // Spit out all loot
                        event.setCancelled(true);
                        UUID id = player.getUniqueId();

                        // save current open page first so any items the player placed are persisted
                        int cur = lootbagPage.getOrDefault(id, 0);
                        Inventory topInv = player.getOpenInventory().getTopInventory();
                        savePage(id, topInv, cur);

                        // now read storage and drop all non-UI items
                        List<ItemStack> storage = getStorage(id);
                        boolean hadAny = false;
                        for (ItemStack it : storage) {
                            if (it != null && it.getType() != Material.AIR && !isNavItem(it)) {
                                player.getWorld().dropItemNaturally(player.getLocation(), it.clone());
                                hadAny = true;
                            }
                        }
                        if (!hadAny) {
                            player.sendMessage(ChatColor.YELLOW + "Your Loot Bundle is empty.");
                        } else {
                            player.sendMessage(ChatColor.GREEN + "All loot was spat out.");
                        }

                        // Clear stored list BEFORE closing to avoid onInventoryClose re-saving items
                        List<ItemStack> empty = new ArrayList<>(Collections.nCopies(MAX_PAGES * PAGE_SIZE, null));
                        lootbags.put(id, empty);

                        // Also clear the GUI's central loot slots so onInventoryClose won't read them
                        if (topInv != null) {
                            for (int s : LOOT_SLOTS) topInv.setItem(s, null);
                        }

                        // remove open/page tracking and close
                        lootbagPage.remove(id);
                        lootbagOpen.remove(id);
                        player.closeInventory();
                        return;
                    } else {
                        // gray panes, page book, etc. — block interaction
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    event.setCancelled(true);
                    return;
                }
            } else {
                // Loot slots: allow normal interaction (taking/placing)
                // When player modifies central slots, we let vanilla update the GUI; onInventoryClose will persist positions
                return;
            }
        }
        // Click was in player's own inventory — allow default behavior
    }
}