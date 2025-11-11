package com.customweapons;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class ShockwingListener implements Listener {
    private static final NamespacedKey WING_KEY = new NamespacedKey(CustomWeapons.getInstance(), "shockwing");
    private static final double MAX_CHARGE = 100.0; // Maximum charge percentage
    private static final double CHARGE_RATE = 2.5; // Charge per second (per tick: 2.0 / 20 = 0.1)
    private static final double MIN_CHARGE_FOR_SHOCKWAVE = 10.0; // Minimum charge to trigger shockwave
    private static final double MAX_SHOCKWAVE_RADIUS = 8.0; // Maximum shockwave radius at 100% charge
    private static final double MAX_SHOCKWAVE_DAMAGE = 22.5; // Maximum damage at 100% charge (7.5 hearts)
    private static final long COOLDOWN_MS = 5000L; // 5 second cooldown between shockwaves

    private final Map<UUID, Double> playerCharges = new HashMap<>(); // Current charge percentage
    private final Map<UUID, Boolean> wasFlying = new HashMap<>(); // Track if player was flying last tick
    private final Map<UUID, Boolean> wasOnGround = new HashMap<>(); // Track if player was on ground last tick
    private final Map<UUID, Long> lastShockwave = new HashMap<>(); // Cooldown tracking
    private final Map<UUID, BukkitRunnable> chargeTasks = new HashMap<>(); // Active charge tasks
    private final Map<UUID, Integer> chargeSoundTickCounters = new HashMap<>(); // Tick counter for charge sounds (resets per charge session)
    private final Map<UUID, Double> lastChargeThreshold = new HashMap<>(); // Last charge threshold reached (for milestone sounds)

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Check if player is wearing shockwing elytra
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null || chestplate.getType() != Material.ELYTRA) {
            stopCharging(uuid);
            wasFlying.put(uuid, false);
            return;
        }

        ItemMeta meta = chestplate.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(WING_KEY, PersistentDataType.BYTE)) {
            stopCharging(uuid);
            wasFlying.put(uuid, false);
            return;
        }

        // Check if player is gliding
        boolean isGliding = player.isGliding();
        boolean wasGliding = wasFlying.getOrDefault(uuid, false);

        boolean isOnGround = player.isOnGround();
        boolean wasOnGroundBefore = wasOnGround.getOrDefault(uuid, false);

        if (isGliding && !wasGliding) {
            // Started gliding - start charging
            startCharging(player);
        } else if (!isGliding && wasGliding) {
            // Stopped gliding - will check for landing when they hit ground
            stopCharging(uuid);
        } else if (isGliding) {
            // Continue gliding - update particles
            updateChargeParticles(player);
        }

        // Check for landing (was in air, now on ground, and has charge)
        if (!wasOnGroundBefore && isOnGround && !isGliding) {
            double charge = playerCharges.getOrDefault(uuid, 0.0);
            if (charge >= MIN_CHARGE_FOR_SHOCKWAVE) {
                createShockwave(player);
            }
        }

        wasFlying.put(uuid, isGliding);
        wasOnGround.put(uuid, isOnGround);
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();
        
        // Check if player is wearing shockwing
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null || chestplate.getType() != Material.ELYTRA) return;
        
        ItemMeta meta = chestplate.getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(WING_KEY, PersistentDataType.BYTE)) return;
        
        // Check if player has charge
        double charge = playerCharges.getOrDefault(uuid, 0.0);
        if (charge >= MIN_CHARGE_FOR_SHOCKWAVE) {
            // Cancel fall damage and create shockwave
            event.setCancelled(true);
            createShockwave(player);
        }
    }

    private void startCharging(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Stop any existing charge task
        stopCharging(uuid);

        // Initialize charge if needed
        if (!playerCharges.containsKey(uuid)) {
            playerCharges.put(uuid, 0.0);
        }

        // Reset sound tick counter when starting to charge
        chargeSoundTickCounters.put(uuid, 0);

        // Start charge task
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !player.isGliding()) {
                    stopCharging(uuid);
                    cancel();
                    return;
                }

                // Check if still wearing shockwing
                ItemStack chestplate = player.getInventory().getChestplate();
                if (chestplate == null || chestplate.getType() != Material.ELYTRA) {
                    stopCharging(uuid);
                    cancel();
                    return;
                }

                ItemMeta meta = chestplate.getItemMeta();
                if (meta == null || !meta.getPersistentDataContainer().has(WING_KEY, PersistentDataType.BYTE)) {
                    stopCharging(uuid);
                    cancel();
                    return;
                }

                // Increase charge
                double currentCharge = playerCharges.getOrDefault(uuid, 0.0);
                double newCharge = Math.min(MAX_CHARGE, currentCharge + (CHARGE_RATE / 20.0));
                playerCharges.put(uuid, newCharge);

                // Increment and get tick counter
                int tickCount = chargeSoundTickCounters.getOrDefault(uuid, 0);
                chargeSoundTickCounters.put(uuid, tickCount + 1);

                // Play charging sounds
                playChargingSounds(player, newCharge, currentCharge, tickCount);

                // Update particles
                updateChargeParticles(player);
            }
        };
        task.runTaskTimer(CustomWeapons.getInstance(), 0L, 1L);
        chargeTasks.put(uuid, task);
    }

    private void stopCharging(UUID uuid) {
        BukkitRunnable task = chargeTasks.remove(uuid);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        // Clean up sound tracking
        chargeSoundTickCounters.remove(uuid);
        lastChargeThreshold.remove(uuid);
    }

    private void playChargingSounds(Player player, double newCharge, double oldCharge, int tickCount) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        double chargePercent = newCharge / MAX_CHARGE;
        UUID uuid = player.getUniqueId();

        // Play milestone sounds when crossing thresholds (25%, 50%, 75%, 100%)
        double lastThreshold = lastChargeThreshold.getOrDefault(uuid, -1.0);
        if (chargePercent >= 1.0 && lastThreshold < 1.0) {
            // Fully charged - play powerful sound
            world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.6f, 1.8f);
            world.playSound(loc, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f);
            lastChargeThreshold.put(uuid, 1.0);
        } else if (chargePercent >= 0.75 && lastThreshold < 0.75) {
            // High charge milestone
            world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 1.5f);
            world.playSound(loc, Sound.BLOCK_BEACON_AMBIENT, 0.8f, 1.2f);
            lastChargeThreshold.put(uuid, 0.75);
        } else if (chargePercent >= 0.5 && lastThreshold < 0.5) {
            // Medium-high charge milestone
            world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.5f);
            world.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.6f, 1.3f);
            lastChargeThreshold.put(uuid, 0.5);
        } else if (chargePercent >= 0.25 && lastThreshold < 0.25) {
            // Low-medium charge milestone
            world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.2f);
            lastChargeThreshold.put(uuid, 0.25);
        }

        // Play periodic charging sounds based on charge level
        int soundInterval;
        
        if (chargePercent >= 0.75) {
            // High charge - faster, more intense sounds (every 30 ticks = 1.5 seconds)
            soundInterval = 30;
            if (tickCount % soundInterval == 0 && tickCount > 0) {
                world.playSound(loc, Sound.BLOCK_BEACON_AMBIENT, 0.4f, 1.8f + (float)(chargePercent * 0.2));
                world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.3f, 2.0f);
            }
        } else if (chargePercent >= 0.5) {
            // Medium charge - moderate sounds (every 40 ticks = 2 seconds)
            soundInterval = 40;
            if (tickCount % soundInterval == 0 && tickCount > 0) {
                world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 0.4f, 1.3f + (float)(chargePercent * 0.3));
                world.playSound(loc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.3f, 1.2f);
            }
        } else if (chargePercent >= 0.1) {
            // Low charge - subtle sounds (every 50 ticks = 2.5 seconds)
            soundInterval = 50;
            if (tickCount % soundInterval == 0 && tickCount > 0) {
                world.playSound(loc, Sound.BLOCK_NOTE_BLOCK_PLING, 0.3f, 1.0f + (float)(chargePercent * 0.4));
            }
        }
    }


    private void createShockwave(Player player) {
        UUID uuid = player.getUniqueId();
        double charge = playerCharges.getOrDefault(uuid, 0.0);

        if (charge < MIN_CHARGE_FOR_SHOCKWAVE) {
            return;
        }

        // Check cooldown
        long now = System.currentTimeMillis();
        if (lastShockwave.containsKey(uuid) && (now - lastShockwave.get(uuid)) < COOLDOWN_MS) {
            return;
        }

        // WorldGuard check
        try {
            if (isInWorldGuardRegion(player.getLocation())) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        CustomWeapons.getInstance().getConfig().getString("messages.shockwing.disabled_region",
                                "&cYou cannot use this inside a WorldGuard region.")));
                playerCharges.put(uuid, 0.0);
                return;
            }
        } catch (Throwable ignored) {
            // Continue if WorldGuard check fails
        }

        // Calculate shockwave properties based on charge
        double chargePercent = charge / MAX_CHARGE;
        double radius = MAX_SHOCKWAVE_RADIUS * chargePercent;
        double damage = MAX_SHOCKWAVE_DAMAGE * chargePercent;

        Location center = player.getLocation();

        // Play sound
        player.getWorld().playSound(center, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.5f, 0.8f);
        player.getWorld().playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.2f);

        // Create shockwave effect
        createShockwaveEffect(center, radius, chargePercent);

        // Damage and knockback entities
        damageEntities(center, radius, damage, player);

        // Reset charge and threshold tracking
        playerCharges.put(uuid, 0.0);
        lastShockwave.put(uuid, now);
        lastChargeThreshold.remove(uuid); // Reset threshold tracking after shockwave

        // Action bar message
        player.sendActionBar(ChatColor.YELLOW + "Shockwave! " + ChatColor.GOLD + 
                String.format("%.0f", charge) + "%" + ChatColor.YELLOW + " charge released!");
    }

    private void createShockwaveEffect(Location center, double radius, double chargePercent) {
        World world = center.getWorld();
        if (world == null) return;

        // Determine particle type and color based on charge
        Particle primaryParticle;
        Particle secondaryParticle;
        Particle tertiaryParticle;
        
        if (chargePercent >= 0.75) {
            // High charge - lightning/electric effects
            primaryParticle = Particle.ELECTRIC_SPARK;
            secondaryParticle = Particle.END_ROD;
            tertiaryParticle = Particle.REVERSE_PORTAL;
        } else if (chargePercent >= 0.5) {
            // Medium-high charge - explosive effects
            primaryParticle = Particle.EXPLOSION;
            secondaryParticle = Particle.FIREWORK;
            tertiaryParticle = Particle.ELECTRIC_SPARK;
        } else {
            // Low-medium charge - basic effects
            primaryParticle = Particle.CLOUD;
            secondaryParticle = Particle.SMOKE;
            tertiaryParticle = Particle.END_ROD;
        }

        // Create enhanced expanding ring effect with multiple layers
        int rings = 8;
        for (int ring = 1; ring <= rings; ring++) {
            final int currentRing = ring;
            final double ringRadius = (radius / rings) * currentRing;
            final double ringDelay = currentRing * 1.5;
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    int particles = (int) (30 * ringRadius * (1 + chargePercent));
                    for (int i = 0; i < particles; i++) {
                        double angle = (2 * Math.PI * i) / particles;
                        double x = ringRadius * Math.cos(angle);
                        double z = ringRadius * Math.sin(angle);
                        double yOffset = Math.sin(angle * 2) * 0.2; // Wave effect
                        Location particleLoc = center.clone().add(x, 0.1 + yOffset, z);
                        
                        // Primary particles
                        world.spawnParticle(primaryParticle, particleLoc, 2, 0.05, 0.05, 0.05, 0.02);
                        
                        // Secondary particles with probability
                        if (Math.random() < 0.4) {
                            world.spawnParticle(secondaryParticle, particleLoc, 1, 0.1, 0.1, 0.1, 0.05);
                        }
                        
                        // Tertiary particles for high charge
                        if (chargePercent >= 0.5 && Math.random() < 0.2) {
                            world.spawnParticle(tertiaryParticle, particleLoc, 1, 0.08, 0.08, 0.08, 0.03);
                        }
                    }
                }
            }.runTaskLater(CustomWeapons.getInstance(), (long) ringDelay);
        }

        // Enhanced center explosion effect
        world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 1, 0, 0, 0, 0);
        world.spawnParticle(Particle.EXPLOSION, center, (int) (3 * chargePercent), 0.2, 0.2, 0.2, 0.1);
        world.spawnParticle(primaryParticle, center, (int) (50 * chargePercent), radius * 0.4, 0.6, radius * 0.4, 0.15);
        world.spawnParticle(secondaryParticle, center, (int) (30 * chargePercent), radius * 0.3, 0.5, radius * 0.3, 0.1);
        
        // Add vertical column effect
        for (int i = 0; i < 10; i++) {
            final double y = i * 0.3;
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location columnLoc = center.clone().add(0, y, 0);
                    world.spawnParticle(primaryParticle, columnLoc, (int) (5 * chargePercent), radius * 0.2, 0.1, radius * 0.2, 0.05);
                    if (chargePercent >= 0.5) {
                        world.spawnParticle(secondaryParticle, columnLoc, (int) (3 * chargePercent), radius * 0.15, 0.1, radius * 0.15, 0.03);
                    }
                }
            }.runTaskLater(CustomWeapons.getInstance(), i);
        }
    }

    private void damageEntities(Location center, double radius, double damage, Player attacker) {
        World world = center.getWorld();
        if (world == null) return;

        for (Entity entity : world.getNearbyEntities(center, radius, radius, radius)) {
            if (entity.equals(attacker)) continue;
            if (!(entity instanceof LivingEntity)) continue;

            LivingEntity target = (LivingEntity) entity;
            double distance = entity.getLocation().distance(center);

            if (distance > radius) continue;

            // Calculate damage falloff (closer = more damage)
            double distancePercent = 1.0 - (distance / radius);
            double finalDamage = damage * distancePercent;

            // Apply damage - damage() method handles event creation and firing internally
            // We use a custom damage cause by creating the event manually for better control
            try {
                // Create and call damage event
                EntityDamageByEntityEvent damageEvent = new EntityDamageByEntityEvent(
                        attacker, target, EntityDamageEvent.DamageCause.CUSTOM, finalDamage
                );
                Bukkit.getPluginManager().callEvent(damageEvent);
                
                // Only apply damage if event wasn't cancelled
                if (!damageEvent.isCancelled() && damageEvent.getFinalDamage() > 0) {
                    target.setHealth(Math.max(0.0, target.getHealth() - damageEvent.getFinalDamage()));
                }
            } catch (NoSuchMethodError | Exception e) {
                // Fallback for older/newer API versions: use standard damage method
                try {
                    target.damage(finalDamage, attacker);
                } catch (Exception ignored) {}
            }

            // Knockback
            Vector direction = entity.getLocation().toVector().subtract(center.toVector()).normalize();
            double knockbackStrength = (radius - distance) / radius * 1.5;
            direction.multiply(knockbackStrength);
            direction.setY(0.5);
            entity.setVelocity(entity.getVelocity().add(direction));

            // Visual effects
            world.spawnParticle(Particle.DAMAGE_INDICATOR, target.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.1);
        }
    }

    private void updateChargeParticles(Player player) {
        UUID uuid = player.getUniqueId();
        double charge = playerCharges.getOrDefault(uuid, 0.0);
        double chargePercent = charge / MAX_CHARGE;

        if (chargePercent < 0.1) return; // Don't show particles for very low charge

        Location loc = player.getLocation();
        World world = loc.getWorld();
        if (world == null) return;

        // Particle intensity based on charge
        int particleCount = (int) (5 + (chargePercent * 15));
        double spread = 0.2 + (chargePercent * 0.5);

        // Single particle trail that changes based on charge percentage
        Particle trailParticle;
        if (chargePercent >= 0.75) {
            // High charge - electric spark
            trailParticle = Particle.ELECTRIC_SPARK;
        } else if (chargePercent >= 0.5) {
            // Medium-high charge - firework
            trailParticle = Particle.FIREWORK;
        } else {
            // Low-medium charge - cloud
            trailParticle = Particle.CLOUD;
        }

        // Spawn single particle trail
        world.spawnParticle(trailParticle, loc, particleCount, spread, 0.3, spread, 0.05);

        // Action bar with charge percentage
        String color;
        if (chargePercent >= 0.75) {
            color = ChatColor.GOLD + "" + ChatColor.BOLD;
        } else if (chargePercent >= 0.5) {
            color = ChatColor.YELLOW + "" + ChatColor.BOLD;
        } else {
            color = ChatColor.GRAY + "";
        }
        player.sendActionBar(color + "Charge: " + String.format("%.0f", charge) + "%");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        stopCharging(uuid);
        playerCharges.remove(uuid);
        wasFlying.remove(uuid);
        wasOnGround.remove(uuid);
        lastShockwave.remove(uuid);
        chargeSoundTickCounters.remove(uuid);
        lastChargeThreshold.remove(uuid);
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

