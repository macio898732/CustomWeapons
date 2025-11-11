package com.customweapons;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class VoidSceptreListener implements Listener {
    private static final String SCEPTRE_NAME = VoidSceptreCommand.SCEPTRE_NAME;
    private static final NamespacedKey SCEPTRE_KEY = new NamespacedKey(CustomWeapons.getInstance(), "void_sceptre");
    private static final NamespacedKey VOID_PEARL_KEY = new NamespacedKey(CustomWeapons.getInstance(), "void_pearl");
    private static final int COOLDOWN_SECONDS = 120;
    private static final long COOLDOWN_MS = COOLDOWN_SECONDS * 1000L;
    private static final int BLACK_HOLE_DURATION_TICKS = 10 * 20; // 10 seconds
    private static final double BLACK_HOLE_RADIUS = 5.5;
    private static final double PULL_STRENGTH = 0.20;
    private static final double DAMAGE_PER_TICK = 5.3; // 2.5 hearts per tick

    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<Location, BlackHole> activeBlackHoles = new HashMap<>();
    private final Set<EnderPearl> trackedPearls = new HashSet<>();
    private final Map<EnderPearl, UUID> pearlOwners = new HashMap<>();

    private static class BlackHole {
        Location location;
        Player owner;
        int ticksRemaining;
        int tickCounter;
        BukkitRunnable task;

        BlackHole(Location location, Player owner, int duration) {
            this.location = location;
            this.owner = owner;
            this.ticksRemaining = duration;
            this.tickCounter = 0;
        }
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.DIAMOND_HOE) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        if (!meta.getPersistentDataContainer().has(SCEPTRE_KEY, PersistentDataType.BYTE)) return;

        event.setCancelled(true);

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Check cooldown
        if (cooldowns.containsKey(uuid) && (now - cooldowns.get(uuid)) < COOLDOWN_MS) {
            long timeLeft = COOLDOWN_SECONDS - ((now - cooldowns.get(uuid)) / 1000);
            player.sendActionBar(ChatColor.RED + "Cooldown: " + timeLeft + "s");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }

        // WorldGuard check: disallow using ability inside ANY WorldGuard region
        try {
            if (isInWorldGuardRegion(player.getLocation())) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        CustomWeapons.getInstance().getConfig().getString("messages.void_sceptre.disabled_region",
                                "&cYou cannot use this inside a WorldGuard region.")));
                return;
            }
        } catch (Throwable ignored) {
            // If something goes wrong with reflection/worldguard detection, just continue (do not crash plugin).
        }

        // Launch ender pearl
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize();
        EnderPearl pearl = player.launchProjectile(EnderPearl.class, direction.multiply(1.2));

        // Tag the pearl so we can identify it
        pearl.getPersistentDataContainer().set(VOID_PEARL_KEY, PersistentDataType.BYTE, (byte) 1);
        trackedPearls.add(pearl);
        pearlOwners.put(pearl, uuid);

        // Set cooldown
        cooldowns.put(uuid, now);

        // Play sound
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
        player.sendActionBar(ChatColor.DARK_PURPLE + "Void Sceptre activated!");
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof EnderPearl)) return;

        EnderPearl pearl = (EnderPearl) event.getEntity();
        if (!pearl.getPersistentDataContainer().has(VOID_PEARL_KEY, PersistentDataType.BYTE)) return;

        trackedPearls.remove(pearl);
        UUID ownerUuid = pearlOwners.remove(pearl);

        Location hitLocation = pearl.getLocation();
        if (hitLocation.getBlock().getType() != Material.AIR) {
            // Adjust location to be above the block
            hitLocation = hitLocation.getBlock().getLocation().add(0.5, 1, 0.5);
        }

        // Find the player who shot this pearl
        Player owner = null;
        if (ownerUuid != null) {
            owner = Bukkit.getPlayer(ownerUuid);
        }
        if (owner == null && pearl.getShooter() instanceof Player) {
            owner = (Player) pearl.getShooter();
        }

        if (owner == null) return;

        // WorldGuard check: disallow creating black hole inside ANY WorldGuard region
        try {
            if (isInWorldGuardRegion(hitLocation)) {
                if (owner.isOnline()) {
                    owner.sendMessage(ChatColor.translateAlternateColorCodes('&',
                            CustomWeapons.getInstance().getConfig().getString("messages.void_sceptre.disabled_region",
                                    "&cYou cannot use this inside a WorldGuard region.")));
                }
                pearl.remove();
                return;
            }
        } catch (Throwable ignored) {
            // If something goes wrong with reflection/worldguard detection, just continue (do not crash plugin).
        }

        // Create black hole
        createBlackHole(hitLocation, owner);

        // Remove the pearl
        pearl.remove();

        // Clean up any remaining references
        trackedPearls.remove(pearl);
        pearlOwners.remove(pearl);
    }

    private void createBlackHole(Location location, Player owner) {
        BlackHole blackHole = new BlackHole(location, owner, BLACK_HOLE_DURATION_TICKS);

        // Store black hole
        activeBlackHoles.put(location, blackHole);

        // Initial explosion effect
        location.getWorld().playSound(location, Sound.ENTITY_WITHER_SPAWN, 1, 0.5f);
        location.getWorld().spawnParticle(Particle.EXPLOSION, location, 3, 0, 0, 0, 0);

        // Create repeating task for black hole effects
        blackHole.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (blackHole.ticksRemaining <= 0) {
                    // Black hole expires
                    expireBlackHole(blackHole);
                    cancel();
                    return;
                }

                // Spawn particles for black hole effect
                spawnBlackHoleParticles(location);

                // Pull and damage entities
                pullAndDamageEntities(location, owner, blackHole);

                blackHole.ticksRemaining--;
                blackHole.tickCounter++;
            }
        };
        blackHole.task.runTaskTimer(CustomWeapons.getInstance(), 0L, 1L);

        // Schedule cleanup after duration
        new BukkitRunnable() {
            @Override
            public void run() {
                if (activeBlackHoles.containsKey(location)) {
                    expireBlackHole(blackHole);
                }
            }
        }.runTaskLater(CustomWeapons.getInstance(), BLACK_HOLE_DURATION_TICKS);
    }

    private void spawnBlackHoleParticles(Location location) {
        World world = location.getWorld();
        if (world == null) return;

        // Create a dense black sphere in the center using multiple layers
        // Use particles that don't drift upward (SQUID_INK, PORTAL, BLACK_DUST)
        double sphereRadius = 1.5;
        int particlesPerLayer = 20;

        // Multiple layers of the black sphere
        for (int layer = 0; layer < 6; layer++) {
            double layerRadius = sphereRadius * (layer + 1) / 6.0;
            for (int i = 0; i < particlesPerLayer; i++) {
                double theta = 2 * Math.PI * i / particlesPerLayer;
                double phi = Math.PI * (layer + 1) / 7.0;

                double x = layerRadius * Math.sin(phi) * Math.cos(theta);
                double y = layerRadius * Math.cos(phi);
                double z = layerRadius * Math.sin(phi) * Math.sin(theta);

                Location particleLoc = location.clone().add(x, y, z);

                // Black particles that stay in place (no upward drift)
                world.spawnParticle(Particle.SQUID_INK, particleLoc, 2, 0, 0, 0, 0);
                world.spawnParticle(Particle.PORTAL, particleLoc, 1, 0, 0, 0, 0);
                world.spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 1, 0, 0, 0, 0);
            }
        }

        // Dense center core with stationary particles
        world.spawnParticle(Particle.SQUID_INK, location, 40, 0.3, 0.3, 0.3, 0);
        world.spawnParticle(Particle.PORTAL, location, 25, 0.3, 0.3, 0.3, 0);
        world.spawnParticle(Particle.REVERSE_PORTAL, location, 20, 0.3, 0.3, 0.3, 0);

        // Portal particles for the void effect around the sphere
        world.spawnParticle(Particle.PORTAL, location, 20, 1.0, 1.0, 1.0, 0.1);
        world.spawnParticle(Particle.REVERSE_PORTAL, location, 15, 1.0, 1.0, 1.0, 0.1);

        // Orbiting particles around the black hole
        for (int i = 0; i < 8; i++) {
            double angle = (System.currentTimeMillis() / 50.0) % 360 + (i * 45);
            double radians = Math.toRadians(angle);
            double x = BLACK_HOLE_RADIUS * 0.7 * Math.cos(radians);
            double z = BLACK_HOLE_RADIUS * 0.7 * Math.sin(radians);
            Location particleLoc = location.clone().add(x, 0, z);
            world.spawnParticle(Particle.PORTAL, particleLoc, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.REVERSE_PORTAL, particleLoc, 1, 0, 0, 0, 0);
        }
    }

    private void pullAndDamageEntities(Location blackHoleLoc, Player owner, BlackHole blackHole) {
        World world = blackHoleLoc.getWorld();
        if (world == null) return;

        for (Entity entity : world.getNearbyEntities(blackHoleLoc, BLACK_HOLE_RADIUS, BLACK_HOLE_RADIUS, BLACK_HOLE_RADIUS)) {
            // Don't affect the owner
            if (entity.equals(owner)) continue;

            // Don't affect items or projectiles
            if (entity instanceof Item || entity instanceof Projectile) continue;

            Location entityLoc = entity.getLocation();
            double distance = entityLoc.distance(blackHoleLoc);

            if (distance > BLACK_HOLE_RADIUS) continue;

            // Calculate pull vector
            Vector pullVector = blackHoleLoc.toVector().subtract(entityLoc.toVector()).normalize();
            pullVector.multiply(PULL_STRENGTH * (1.0 - (distance / BLACK_HOLE_RADIUS)));

            // Apply pull
            Vector currentVelocity = entity.getVelocity();
            entity.setVelocity(currentVelocity.add(pullVector));

            // Damage entities (every 10 ticks to avoid too frequent damage)
            if (entity instanceof LivingEntity && blackHole.tickCounter % 10 == 0) {
                LivingEntity living = (LivingEntity) entity;

                // Check if player has totem before damaging
                boolean hasTotem = false;
                if (living instanceof Player) {
                    Player player = (Player) living;
                    ItemStack offHand = player.getInventory().getItemInOffHand();
                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    if ((offHand != null && offHand.getType() == Material.TOTEM_OF_UNDYING) ||
                            (mainHand != null && mainHand.getType() == Material.TOTEM_OF_UNDYING)) {
                        hasTotem = true;
                    }
                }

                // Apply damage - if they have totem, use damage() with 3x multiplier (respects armor)
                // If no totem, bypass armor by directly setting health
                if (hasTotem) {
                    // Use damage() method with 3x damage - respects armor but deals more damage
                    living.damage(DAMAGE_PER_TICK * 3.3, owner);
                } else {
                    // Directly reduce health to bypass armor
                    double currentHealth = living.getHealth();
                    if (currentHealth > 0) {
                        double newHealth = Math.max(0, currentHealth - DAMAGE_PER_TICK);
                        living.setHealth(newHealth);
                    }
                }

                // Visual effect for damage
                world.spawnParticle(Particle.DAMAGE_INDICATOR, entityLoc.add(0, 1, 0), 3, 0.3, 0.5, 0.3, 0.1);
            }
        }
    }

    private void expireBlackHole(BlackHole blackHole) {
        if (blackHole.task != null && !blackHole.task.isCancelled()) {
            blackHole.task.cancel();
        }

        Location location = blackHole.location;
        activeBlackHoles.remove(location);

        // Final explosion effect
        if (location.getWorld() != null) {
            location.getWorld().playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.5f);
            location.getWorld().spawnParticle(Particle.EXPLOSION, location, 5, 1, 1, 1, 0.1);
            location.getWorld().spawnParticle(Particle.PORTAL, location, 30, 1, 1, 1, 0.2);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Prevent ender pearl teleportation from our void sceptre pearls
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            UUID playerUuid = event.getPlayer().getUniqueId();
            // Check if this player has any active tracked pearls
            for (Map.Entry<EnderPearl, UUID> entry : pearlOwners.entrySet()) {
                if (entry.getValue().equals(playerUuid)) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cooldowns.remove(uuid);

        // Remove any tracked pearls from this player
        pearlOwners.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(uuid)) {
                trackedPearls.remove(entry.getKey());
                return true;
            }
            return false;
        });

        // Remove any black holes owned by this player
        activeBlackHoles.entrySet().removeIf(entry -> {
            if (entry.getValue().owner.equals(event.getPlayer())) {
                expireBlackHole(entry.getValue());
                return true;
            }
            return false;
        });
    }

    private boolean isInWorldGuardRegion(Location loc) {
        try {
            // WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery()
            Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
            Object wgInstance = worldGuardClass.getMethod("getInstance").invoke(null);
            Object platform = wgInstance.getClass().getMethod("getPlatform").invoke(wgInstance);
            Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);
            Object query = regionContainer.getClass().getMethod("createQuery").invoke(regionContainer);

            // adapt location: com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(Location)
            Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
            Object adapted = bukkitAdapterClass.getMethod("adapt", Location.class).invoke(null, loc);

            // query.getApplicableRegions(adapted)
            Object applicable = query.getClass().getMethod("getApplicableRegions", adapted.getClass()).invoke(query, adapted);

            // applicable.getRegions().iterator().hasNext() -> returns true if in a region
            Object regions = applicable.getClass().getMethod("getRegions").invoke(applicable);
            if (regions instanceof Iterable) {
                return ((Iterable<?>) regions).iterator().hasNext();
            } else {
                // Fallback: try calling size() or isEmpty() by reflection
                try {
                    int size = (int) regions.getClass().getMethod("size").invoke(regions);
                    return size > 0;
                } catch (NoSuchMethodException nsme) {
                    return false;
                }
            }
        } catch (ClassNotFoundException cnfe) {
            // WorldGuard or BukkitAdapter not present
            return false;
        } catch (Throwable t) {
            // Any other reflection error -> treat as "not in region" to avoid blocking functionality
            return false;
        }
    }
}

