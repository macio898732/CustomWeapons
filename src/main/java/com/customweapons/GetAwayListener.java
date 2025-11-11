package com.customweapons;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class GetAwayListener implements Listener {
    private static final String GETAWAY_NAME = GetAwayCommand.GETAWAY_NAME;
    private static final NamespacedKey GETAWAY_KEY = new NamespacedKey(CustomWeapons.getInstance(), "getaway_compass");
    private static final int COOLDOWN_SECONDS = 60;
    private static final long COOLDOWN_MS = COOLDOWN_SECONDS * 1000L;
    private static final int INVISIBILITY_DURATION_TICKS = 160; // 8 seconds total (3s wait + 5s after teleport)
    private static final int SLOWNESS_DURATION_TICKS = 80; // 4 seconds (only during wait)
    private static final int WAIT_TICKS = 60; // 3 seconds
    private static final int POST_TELEPORT_INVISIBILITY_TICKS = 100; // 5 seconds after teleport
    private static final int DECOY_COUNT = 30;
    private static final int DECOY_RADIUS = 20;
    private static final Random random = new Random();

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Boolean> activeTeleports = new HashMap<>(); // Prevent multiple teleports at once
    private final Map<UUID, Team> invisibleTeams = new HashMap<>(); // Store teams for invisibility
    private final Map<UUID, ItemStack[]> storedArmor = new HashMap<>(); // Store armor during invisibility

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.COMPASS) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (!meta.getPersistentDataContainer().has(GETAWAY_KEY, PersistentDataType.BYTE)) return;
// Animation Made By Lusik21556/@Lusik21556
        event.setCancelled(true);

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Check cooldown
        if (cooldowns.containsKey(uuid) && (now - cooldowns.get(uuid)) < COOLDOWN_MS) {
            long timeLeft = COOLDOWN_SECONDS - ((now - cooldowns.get(uuid)) / 1000);
            player.sendActionBar(ChatColor.RED + "Cooldown: " + timeLeft + "s");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }// Animation Made By Lusik21556/@Lusik21556

        // Prevent multiple teleports at once
        if (activeTeleports.containsKey(uuid) && activeTeleports.get(uuid)) {
            player.sendActionBar(ChatColor.RED + "Teleport already active!");
            return;
        }

        // WorldGuard check: disallow using ability inside ANY WorldGuard region
        try {
            if (isInWorldGuardRegion(player.getLocation())) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        CustomWeapons.getInstance().getConfig().getString("messages.getaway.disabled_region",
                                "&cYou cannot use this inside a WorldGuard region.")));
                return;
            }
        } catch (Throwable ignored) {
            // If something goes wrong with reflection/worldguard detection, just continue
        }

        // Activate get away ability
        activateGetAway(player);

        // Set cooldown
        cooldowns.put(uuid, now);
    }// Animation Made By Lusik21556/@Lusik21556

    private void activateGetAway(Player player) {
        UUID uuid = player.getUniqueId();
        activeTeleports.put(uuid, true);

        Location startLoc = player.getLocation();
        World world = player.getWorld();

        // Play activation sounds
        world.playSound(startLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.2f);
        world.playSound(startLoc, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 0.8f);
        world.playSound(startLoc, Sound.ENTITY_ILLUSIONER_MIRROR_MOVE, 0.8f, 1.5f);
        // Animation Made By Lusik21556/@Lusik21556
        // Spawn red dust particles at activation
        spawnRedDustParticles(world, startLoc, 20, 1.5);

        // Execute catchme:_/create function at player location
        executeFunction(player, "catchme:_/create");
        
        // Execute catchme:a/shrink/play function at player location
        executeFunction(player, "catchme:a/shrink/play");

        // Apply potion effects (4 seconds = 80 ticks)
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, INVISIBILITY_DURATION_TICKS, 0, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, SLOWNESS_DURATION_TICKS, 255, false, false));
        
        // Make player completely invisible including armor
        makePlayerFullyInvisible(player);
        
        // Create continuous red dust effect during wait period
        createRedDustEffect(player, startLoc);
// Animation Made By Lusik21556/@Lusik21556
        // Wait 3 seconds, then create decoys and teleport
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isValid() || !player.isOnline()) {
                    activeTeleports.remove(uuid);
                    cancel();
                    return;
                }
// Animation Made By Lusik21556/@Lusik21556
                // Create decoy locations
                Location teleportLoc = null;
                for (int i = 0; i < DECOY_COUNT; i++) {
                    Location decoyLoc = createDecoyLocation(player, startLoc);
                    if (decoyLoc != null) {
                        // Execute catchme:_/create function at decoy location
                        executeFunctionAtPosition(world, decoyLoc, "catchme:_/create");
                        
                        // Add red dust particles at decoy location
                        spawnRedDustParticles(world, decoyLoc, 8, 0.8);
                        
                        // Play subtle sound at decoy location
                        world.playSound(decoyLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.2f, 1.8f);
                        
                        // Choose one random location for teleport (last one or random)
                        if (i == DECOY_COUNT - 1 || random.nextDouble() < 0.1) {
                            teleportLoc = decoyLoc;
                        }
                    }
                }
// Animation Made By Lusik21556/@Lusik21556
                // Execute catchme:a/rise/play function at player location
                executeFunction(player, "catchme:a/rise/play");
                
                // Play rise sound
                world.playSound(startLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f);
                world.playSound(startLoc, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.2f);
                world.playSound(startLoc, Sound.ENTITY_ILLUSIONER_CAST_SPELL, 1.0f, 1.2f);
                
                // Final red dust burst at start location
                spawnRedDustParticles(world, startLoc, 30, 2.0);
// Animation Made By Lusik21556/@Lusik21556
                // Teleport player to chosen location
                // Store in final variable for use in inner class
                final Location finalTeleportLoc = teleportLoc;
                final World finalWorld = world;
                if (finalTeleportLoc != null) {
                    // Teleport immediately after rise effect
                    if (player.isValid() && player.isOnline()) {
                        Location tpLoc = finalTeleportLoc.clone();
                        player.teleport(tpLoc);
                        
                        // Play teleport arrival sounds and particles
                        finalWorld.playSound(tpLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f, 1.0f);
                        finalWorld.playSound(tpLoc, Sound.ITEM_CHORUS_FRUIT_TELEPORT, 1.0f, 1.0f);
                        spawnRedDustParticles(finalWorld, tpLoc, 25, 1.8);
                        
                        // Reapply invisibility potion effect after teleport to ensure full 5 seconds
                        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, POST_TELEPORT_INVISIBILITY_TICKS, 0, false, false));
                        
                        // Restore visibility after 5 seconds post-teleport
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                restorePlayerVisibility(player);
                            }
                        }.runTaskLater(CustomWeapons.getInstance(), POST_TELEPORT_INVISIBILITY_TICKS);
                        
                        player.sendActionBar(ChatColor.GREEN + "Teleported away safely! You are invisible for 5 more seconds.");
                    }
                    activeTeleports.remove(uuid);
                } else {
                    // Restore visibility even if teleport failed
                    restorePlayerVisibility(player);
                    activeTeleports.remove(uuid);
                }
            }
        }.runTaskLater(CustomWeapons.getInstance(), WAIT_TICKS);
    }
    // Animation Made By Lusik21556/@Lusik21556
    private void executeFunction(Player player, String functionPath) {
        // Execute a Minecraft function at the player's location
        // Format: execute as <player> at @s run function <namespace:path>
        // This matches: execute at %player% run function catchme:...
        // Note: Using "at @s" should inherit dimension from player context
        String command = String.format("execute as %s at @s run function %s",
                player.getName(), functionPath);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }
    
    private void executeFunctionAtPosition(World world, Location loc, String functionPath) {
        // Execute a Minecraft function at a specific position
        // Format: execute in <dimension> positioned <x> <y> <z> run function <namespace:path>
        // This ensures the function runs in the correct dimension (overworld, nether, end)
        String dimension = getDimensionIdentifier(world);
        String command = String.format("execute in %s positioned %.2f %.2f %.2f run function %s",
                dimension, loc.getX(), loc.getY(), loc.getZ(), functionPath);
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }
    
    private String getDimensionIdentifier(World world) {
        // Convert Bukkit World.Environment to Minecraft dimension identifier
        // This is required for execute commands to work in non-overworld dimensions
        switch (world.getEnvironment()) {
            case NETHER:
                return "minecraft:the_nether";
            case THE_END:
                return "minecraft:the_end";
            case NORMAL:
            default:
                return "minecraft:overworld";
        }
    }
    // Animation Made By Lusik21556/@Lusik21556
    private void spawnRedDustParticles(World world, Location loc, int count, double radius) {
        // Spawn red dust particles at a location
        Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(255, 0, 0), 1.0f);
        Location center = loc.clone().add(0, 1, 0);
        // Animation Made By Lusik21556/@Lusik21556
        // Create a circle/sphere of red dust particles
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double randomRadius = radius * (0.5 + Math.random() * 0.5);
            double x = Math.cos(angle) * randomRadius;
            double z = Math.sin(angle) * randomRadius;
            double y = (Math.random() - 0.5) * radius * 0.8;
            
            Location particleLoc = center.clone().add(x, y, z);
            world.spawnParticle(Particle.DUST, particleLoc, 1, 0, 0, 0, 0, dustOptions);
        }
        
        // Add some particles directly at center
        world.spawnParticle(Particle.DUST, center, count / 4, radius * 0.3, radius * 0.5, radius * 0.3, 0, dustOptions);
    }// Animation Made By Lusik21556/@Lusik21556
    
    private void createRedDustEffect(Player player, Location loc) {
        // Create a continuous red dust effect during the wait period
        World world = loc.getWorld();
        if (world == null) return;
        // Animation Made By Lusik21556/@Lusik21556
        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = WAIT_TICKS + 20; // Continue slightly after teleport
            
            @Override
            public void run() {
                if (ticks >= maxTicks || !player.isValid() || !player.isOnline()) {
                    cancel();
                    return;
                }// Animation Made By Lusik21556/@Lusik21556
                
                // Spawn red dust particles around player location periodically
                if (ticks % 10 == 0) { // Every 0.5 seconds
                    Location currentLoc = player.getLocation();
                    spawnRedDustParticles(world, currentLoc, 5, 1.0);
                }// Animation Made By Lusik21556/@Lusik21556
                
                // Spawn occasional sound
                if (ticks % 30 == 0) { // Every 1.5 seconds
                    world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_BELL, 0.3f, 1.5f);
                }
                
                ticks++;
            }// Animation Made By Lusik21556/@Lusik21556
        }.runTaskTimer(CustomWeapons.getInstance(), 0L, 1L);
    }

    private Location createDecoyLocation(Player player, Location center) {
        World world = center.getWorld();
        if (world == null) return null;

        // Get random offset
        int offsetX = random.nextInt(DECOY_RADIUS * 2) - DECOY_RADIUS;
        int offsetZ = random.nextInt(DECOY_RADIUS * 2) - DECOY_RADIUS;
        // Animation Made By Lusik21556/@Lusik21556
        int newX = center.getBlockX() + offsetX;
        int newZ = center.getBlockZ() + offsetZ;

        // Find highest solid block (similar to Skript's "highest solid block")
        Location testLoc = new Location(world, newX, 0, newZ);
        int maxHeight = world.getMaxHeight();
        int minHeight = world.getMinHeight();
        
        // Start from top and work down to find the highest solid block
        org.bukkit.block.Block highestSolidBlock = null;
        for (int y = maxHeight - 1; y >= minHeight; y--) {
            org.bukkit.block.Block block = world.getBlockAt(newX, y, newZ);
            if (block.getType().isSolid() && block.getType() != Material.BARRIER) {
                highestSolidBlock = block;
                break;
            }
        }// Animation Made By Lusik21556/@Lusik21556
        
        // If no solid block found, use a safe default (sea level or center Y)
        if (highestSolidBlock == null) {
            return new Location(world, newX, Math.max(center.getY(), 64.5), newZ);
        }
        
        // Return location above the solid block
        Location result = highestSolidBlock.getLocation().add(0.5, 1.5, 0.5);
        return result;
    }
// Animation Made By Lusik21556/@Lusik21556
// Animation Made By Lusik21556/@Lusik21556
    private void makePlayerFullyInvisible(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Store current armor before removing it
        ItemStack[] armor = player.getInventory().getArmorContents();
        storedArmor.put(uuid, armor.clone());
        
        // Use scoreboard team to hide nametag
        try {
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            String teamName = "getaway_inv_" + uuid.toString().substring(0, 8);
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
            }
            
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            team.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            team.addEntry(player.getName());
            
            invisibleTeams.put(uuid, team);
        } catch (Exception e) {
            // Scoreboard method failed, continue
        }
        
        // Remove armor completely - this makes it 100% invisible
        // The armor is stored and will be restored after teleport
        player.getInventory().setArmorContents(new ItemStack[4]);
        
        // Hide and show player to all others to refresh their view
        // This makes the armor removal take effect immediately for all players
        for (Player other : player.getWorld().getPlayers()) {
            if (!other.equals(player) && other.canSee(player)) {
                other.hidePlayer(CustomWeapons.getInstance(), player);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (other.isOnline() && player.isOnline()) {
                            other.showPlayer(CustomWeapons.getInstance(), player);
                        }
                    }
                }.runTaskLater(CustomWeapons.getInstance(), 1L);
            }
        }
    }
    
    private void restorePlayerVisibility(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Restore armor
        ItemStack[] armor = storedArmor.remove(uuid);
        if (armor != null) {
            player.getInventory().setArmorContents(armor);
        }
        
        // Remove from team
        Team team = invisibleTeams.remove(uuid);
        if (team != null) {
            try {
                team.removeEntry(player.getName());
                // Unregister team if empty to clean up
                if (team.getEntries().isEmpty()) {
                    team.unregister();
                }
            } catch (Exception e) {
                // Team already removed or doesn't exist
            }
        }
        
        // Refresh visibility for all players to show restored armor
        for (Player other : player.getWorld().getPlayers()) {
            if (!other.equals(player)) {
                other.hidePlayer(CustomWeapons.getInstance(), player);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (other.isOnline() && player.isOnline()) {
                            other.showPlayer(CustomWeapons.getInstance(), player);
                        }
                    }
                }.runTaskLater(CustomWeapons.getInstance(), 2L);
            }
        }
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cooldowns.remove(uuid);
        activeTeleports.remove(uuid);
        
        // Clean up invisibility if player quits
        restorePlayerVisibility(event.getPlayer());
        storedArmor.remove(uuid);
        invisibleTeams.remove(uuid);
    }

    private boolean isInWorldGuardRegion(Location loc) {
        try {
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object wgInstance = worldGuardClass.getMethod("getInstance").invoke(null);
            Object platform = wgInstance.getClass().getMethod("getPlatform").invoke(wgInstance);
            Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);
            Object query = regionContainer.getClass().getMethod("createQuery").invoke(regionContainer);

            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Object adapted = bukkitAdapterClass.getMethod("adapt", Location.class).invoke(null, loc);

            Object applicable = query.getClass().getMethod("getApplicableRegions", adapted.getClass()).invoke(query, adapted);
            Object regions = applicable.getClass().getMethod("getRegions").invoke(applicable);
            
            if (regions instanceof Iterable) {
                return ((Iterable<?>) regions).iterator().hasNext();
            } else {
                try {
                    int size = (int) regions.getClass().getMethod("size").invoke(regions);
                    return size > 0;
                } catch (NoSuchMethodException nsme) {
                    return false;
                }
            }
        } catch (ClassNotFoundException cnfe) {
            return false;
        } catch (Throwable t) {
            return false;
        }
    }
}

