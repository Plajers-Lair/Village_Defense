/*
 * Village Defense - Protect villagers from hordes of zombies
 * Copyright (c) 2025  Plugily Projects - maintained by Tigerpanzer_02 and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package plugily.projects.villagedefense.arena.managers.spawner.gold.enemy;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewCreatureEvents;
import plugily.projects.villagedefense.arena.managers.spawner.gold.RideableCreatureEvents;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.type.BlinkerZombie;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.type.GravityZombie;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.type.PopZombie;
import plugily.projects.villagedefense.kits.utils.KitHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EnemiesRegistry {

  private final Set<VDEnemy> registeredEnemies = new HashSet<>();
  private final Main plugin;

  public EnemiesRegistry(Main plugin) {
    this.plugin = plugin;
    registerEnemies();
    registerNewEnemies();
  }

  private void registerNewEnemies() {
    registeredEnemies.add(new BlinkerZombie().getEnemy());
    registeredEnemies.add(new GravityZombie().getEnemy());
    registeredEnemies.add(new PopZombie().getEnemy());
  }

  public void registerEnemies() {
    new NewCreatureEvents(plugin);
    new RideableCreatureEvents(plugin);
    registeredEnemies.add(
      new VDEnemyBuilder("VD_ZOMBIE")
        .withName("Zombie")
        .withWaveMinimum(0)
        .withWaveMaximum(50)
        .withFullArmor(VDEnemyBuilder.ArmorType.LEATHER, 11, 1)
        .withFullArmor(VDEnemyBuilder.ArmorType.GOLD, 16, 2)
        .withFullArmor(VDEnemyBuilder.ArmorType.CHAINMAIL, 26, 3)
        .withFullArmor(VDEnemyBuilder.ArmorType.IRON, 36, 4)
        .withFullArmor(VDEnemyBuilder.ArmorType.IRON, 41, 5, 75.0, new ArmorPiece.Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1))
        .withFullArmor(VDEnemyBuilder.ArmorType.DIAMOND, 46, 6, 35.0)
        .withScalingHealth(wave -> {
          if (wave <= 15) {
            return 20.0;
          }
          return 20.0 + ((wave - 15) * 2.0);
        })
        .onSpawn(creature -> {
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
        }).build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_HORDE_ZOMBIE")
        .withName("Horde Zombie")
        .withWaveMinimum(10)
        .withWaveMaximum(40)
        .withFullArmor(VDEnemyBuilder.ArmorType.LEATHER, 26)
        .withScalingHealth(wave -> {
          if (wave <= 15) {
            return 20.0;
          }
          return 20.0 + ((wave - 15) * 1.5);
        })
        .onSpawn(creature -> {
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
        }).build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_BABY_ZOMBIE")
        .withName("Baby Zombie")
        .withWaveMinimum(3)
        .withWaveMaximum(35)
        .setBaby()
        .withScalingHealth(wave -> 3.0)
        .onSpawn(creature -> {
          creature.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(2.0);
          creature.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(5.0);
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
        }).build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_PLAYER_BUSTER")
        .withName("Player Buster")
        .withWaveMinimum(6)
        .withWaveMaximum(35)
        .withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.TNT)).setWaveMinimum(6))
        .withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.GOLDEN_CHESTPLATE)).setWaveMinimum(6))
        .withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.GOLDEN_LEGGINGS)).setWaveMinimum(6))
        .withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.GOLDEN_BOOTS)).setWaveMinimum(6))
        .withScalingHealth(wave -> 1.0)
        .onSpawn(creature -> {
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
        })
        .onDamageByEntity((event, arena) -> {
          if (!(event.getDamager() instanceof Player player)) {
            return;
          }
          event.setCancelled(true);
          Creature yourself = (Creature) event.getEntity();
          yourself.setInvulnerable(true);
          yourself.setAI(false);
          yourself.getWorld().playSound(yourself.getLocation(), Sound.ENTITY_TNT_PRIMED, 1, 0.75f);
          new BukkitRunnable() {
            boolean toggle = false;
            int ticks = 0;

            @SneakyThrows
            @Override
            public void run() {
              if (ticks >= 40) {
                yourself.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, yourself.getLocation().add(0, 0.5, 0), 2);
                for (Entity nearby : yourself.getNearbyEntities(3, 3, 3)) {
                  if (!(nearby instanceof Player nearbyPlayer)) {
                    continue;
                  }
                  nearbyPlayer.playSound(nearbyPlayer, Sound.ENTITY_GENERIC_EXPLODE, 1, 1.5f);
                  nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 5, 0));
                  nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 20 * 6, 0));
                  nearbyPlayer.damage(0, yourself);
                  KitHelper.maxHealthPercentDamage(player, yourself, 12.5);
                }
                yourself.remove();
                yourself.setKiller(player);
                Bukkit.getServer().getPluginManager().callEvent(new EntityDeathEvent(yourself, new ArrayList<>(Collections.singletonList(new ItemStack(Material.ROTTEN_FLESH)))));
                cancel();
                return;
              }
              ChatColor color = toggle ? ChatColor.YELLOW : ChatColor.RED;
              for (Player targetPlayer : arena.getPlayers()) {
                plugin.getGlowingEntities().setGlowing(yourself, targetPlayer, color);
              }
              toggle = !toggle;
              ticks += 10;
            }
          }.runTaskTimer(plugin, 0, 10);
        })
        .build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_RUNNER_ZOMBIE")
        .withName("Runner Zombie")
        .withWaveMinimum(10)
        .withWaveMaximum(25)
        .withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.LEATHER_BOOTS)).setWaveMinimum(10).setPriority(1))
        .withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.GOLDEN_BOOTS)).setWaveMinimum(16).setPriority(2))
        .withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.CHAINMAIL_BOOTS)).setWaveMinimum(21).setPriority(3))
        .withScalingHealth(wave -> {
          if (wave <= 15) {
            return 15.0;
          }
          return 15.0 + ((wave - 15) * 1.75);
        })
        .onSpawn(creature -> {
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.26);
        }).build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_MEDIUM_ZOMBIE")
        .withName("Medium Zombie")
        .withWaveMinimum(5)
        .withWaveMaximum(30)
        .withFullArmor(VDEnemyBuilder.ArmorType.LEATHER, 5, 1)
        .withFullArmor(VDEnemyBuilder.ArmorType.GOLD, 11, 2)
        .withFullArmor(VDEnemyBuilder.ArmorType.CHAINMAIL, 16, 3)
        .withFullArmor(VDEnemyBuilder.ArmorType.IRON, 26, 4)
        .withScalingHealth(wave -> {
          if (wave <= 15) {
            return 20.0;
          }
          return 20.0 + ((wave - 15) * 2.25);
        })
        .onSpawn(creature -> {
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
        }).build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_GOLEM_BUSTER")
        .withName("Golem Buster")
        .withWaveMinimum(10)
        .withWaveMaximum(35)
        .withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.TNT)).setWaveMinimum(10))
        .withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.IRON_CHESTPLATE)).setWaveMinimum(10))
        .withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.IRON_CHESTPLATE)).setWaveMinimum(10))
        .withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.IRON_CHESTPLATE)).setWaveMinimum(10))
        .onSpawn(creature -> {
          creature.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(15.0);
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
        })
        .onDamageByEntity((event, arena) -> {
          if (!(event.getDamager() instanceof IronGolem ironGolem)) {
            return;
          }
          Creature yourself = (Creature) event.getEntity();
          ironGolem.damage(20.0);
          yourself.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, yourself.getLocation().add(0, 0.5, 0), 2);
          for (Player player : arena.getPlayers()) {
            player.playSound(ironGolem.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
          }
          yourself.remove();
          Player owner = Bukkit.getPlayer(UUID.fromString(ironGolem.getMetadata("VD_OWNER_UUID").get(0).asString()));
          yourself.setKiller(owner);
          Bukkit.getServer().getPluginManager().callEvent(new EntityDeathEvent(yourself, new ArrayList<>(Collections.singletonList(new ItemStack(Material.ROTTEN_FLESH)))));
        })
        .canSpawn(arena -> !arena.getIronGolems().isEmpty())
        .build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_HARD_ZOMBIE")
        .withName("Hard Zombie")
        .withWaveMinimum(15)
        .withWaveMaximum(35)
        .withFullArmor(VDEnemyBuilder.ArmorType.GOLD, 15, 1)
        .withFullArmor(VDEnemyBuilder.ArmorType.CHAINMAIL, 21, 2)
        .withFullArmor(VDEnemyBuilder.ArmorType.IRON, 26, 3)
        .withFullArmor(VDEnemyBuilder.ArmorType.IRON, 31, 4, 75.0, new ArmorPiece.Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1))
        .withScalingHealth(wave -> 20.0 + ((wave - 15) * 2.5))
        .onSpawn(creature -> {
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
        }).build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_TANK_ZOMBIE")
        .withName("Tank Zombie")
        .withWaveMinimum(20)
        .withWaveMaximum(40)
        .withFullArmor(VDEnemyBuilder.ArmorType.CHAINMAIL, 20, 1)
        .withFullArmor(VDEnemyBuilder.ArmorType.IRON, 26, 2)
        .withFullArmor(VDEnemyBuilder.ArmorType.IRON, 31, 3, 75.0, new ArmorPiece.Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1))
        .withFullArmor(VDEnemyBuilder.ArmorType.DIAMOND, 36, 4, 35.0)
        .withScalingHealth(wave -> {
          return 20.0 + ((wave - 15) * 3.0);
        })
        .onSpawn(creature -> {
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
        }).build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_INVISIBLE_BABY_ZOMBIE")
        .withName("Invisible Baby")
        .withWaveMinimum(36)
        .withWaveMaximum(50)
        .withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.CHAINMAIL_BOOTS)).setWaveMinimum(36))
        .setBaby()
        .withScalingHealth(wave -> 3.0)
        .onSpawn(creature -> {
          creature.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(2.0);
          creature.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(5.0);
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
        })
        .onSpawn(creature -> creature.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false))).build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_KAMIKAZE_ZOMBIE")
        .withName("Kamikaze Zombie")
        .withWaveMinimum(25)
        .withWaveMaximum(50)
        .withTargetPriority(EntityType.PLAYER)
        .withPersistentTargeting()
        .withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.TNT)).withArmorDye(Color.BLACK).setWaveMinimum(25))
        .withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.LEATHER_CHESTPLATE)).withArmorDye(Color.BLACK).setWaveMinimum(25))
        .withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.LEATHER_LEGGINGS)).withArmorDye(Color.BLACK).setWaveMinimum(25))
        .withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.LEATHER_BOOTS)).withArmorDye(Color.BLACK).setWaveMinimum(25))
        .withScalingHealth(wave -> 5.0)
        .onDamagingEntity((event, arena) -> {
          if (!(event.getEntity() instanceof Player player)) {
            return;
          }
          Creature yourself = (Creature) event.getDamager();
          TNTPrimed primed = (TNTPrimed) yourself.getWorld().spawnEntity(yourself.getLocation(), EntityType.PRIMED_TNT);
          primed.setSource(yourself);
          primed.setMetadata("VD_PRIMED_TNT", new FixedMetadataValue(arena.getPlugin(), true));
          primed.setMetadata("VD_TNT_DAMAGE_PERCENT", new FixedMetadataValue(arena.getPlugin(), 20.0));
          primed.setFuseTicks(65);
          yourself.remove();
          yourself.setKiller(player);
          Bukkit.getServer().getPluginManager().callEvent(new EntityDeathEvent(yourself, new ArrayList<>(Collections.singletonList(new ItemStack(Material.ROTTEN_FLESH)))));
          new BukkitRunnable() {
            boolean toggle = false;
            int ticks = 0;

            @SneakyThrows
            @Override
            public void run() {
              if (ticks >= 60) {
                cancel();
                return;
              }
              ChatColor color = toggle ? ChatColor.YELLOW : ChatColor.RED;
              for (Player targetPlayer : arena.getPlayers()) {
                plugin.getGlowingEntities().setGlowing(primed, targetPlayer, color);
              }
              toggle = !toggle;
              ticks += 10;
            }
          }.runTaskTimer(plugin, 0, 10);
        })
        .onSpawn(creature -> {
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.32);
        })
        .build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_INVISIBLE_RUNNER_ZOMBIE")
        .withName("Invisible Runner")
        .withWaveMinimum(30)
        .withWaveMaximum(50)
        .withScalingHealth(wave -> 15.0 + ((wave - 15) * 1.75))
        .withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.CHAINMAIL_LEGGINGS)).setWaveMinimum(30))
        .withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.IRON_BOOTS)).setWaveMinimum(30))
        .onSpawn(creature -> {
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.26);
          creature.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
        }).build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_CONQUEROR_ZOMBIE")
        .withName("Conqueror Zombie")
        .withWaveMinimum(36)
        .withWaveMaximum(50)
        .setImpostorHead()
        .withFullArmor(VDEnemyBuilder.ArmorType.IRON, 36, 1)
        .withFullArmor(VDEnemyBuilder.ArmorType.IRON, 41, 2, 75.0, new ArmorPiece.Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1))
        .withFullArmor(VDEnemyBuilder.ArmorType.DIAMOND, 46, 3, 35.0)
        .withScalingHealth(wave -> 25.0 + ((wave - 15) * 2.75))
        .onSpawn(creature -> {
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
        }).build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_CONQUEROR_BOSS_ZOMBIE")
        .withName("Conqueror Zombie (&7&l✶&f)")
        .withWaveMinimum(36)
        .withWaveMaximum(50)
        .setImpostorHead()
        .withFullArmor(VDEnemyBuilder.ArmorType.IRON, 36, 1)
        .withFullArmor(VDEnemyBuilder.ArmorType.IRON, 41, 2, 75.0, new ArmorPiece.Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1))
        .withFullArmor(VDEnemyBuilder.ArmorType.DIAMOND, 46, 3, 35.0)
        .withScalingHealth(wave -> 25.0 + ((wave - 15) * 2.75))
        .onSpawn(creature -> {
          creature.setMetadata("VD_UNSTUNNABLE", new FixedMetadataValue(plugin, true));
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
          creature.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1);
        }).build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_VILLAGER_SLAYER")
        .withName("Villager Slayer (&4&l☄ &7&l✶&f)")
        .withWaveMinimum(45)
        .withWaveMaximum(50)
        .withTargetPriority(EntityType.VILLAGER)
        .withPersistentTargeting()
        .withFullArmor(VDEnemyBuilder.ArmorType.CHAINMAIL, 45, 1)
        .withFullArmor(VDEnemyBuilder.ArmorType.CHAINMAIL, 45, 2, 75.0, new ArmorPiece.Enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 1))
        .withScalingHealth(wave -> 35.0 + ((wave - 15) * 2.75))
        .onSpawn(creature -> {
          creature.setMetadata("VD_UNSTUNNABLE", new FixedMetadataValue(plugin, true));
          creature.setMetadata("VD_UNPOPPABLE", new FixedMetadataValue(plugin, true));
          creature.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
        }).build()
    );
    registeredEnemies.add(
      new VDEnemyBuilder("VD_INVISIBLE_VILLAGER_SLAYER")
        .withName("Invisible Slayer (&4&l☄ &7&l✶&f)")
        .withWaveMinimum(50)
        .withWaveMaximum(50)
        .withTargetPriority(EntityType.VILLAGER)
        .withPersistentTargeting()
        .withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.CHAINMAIL_BOOTS)).setWaveMinimum(50))
        .withScalingHealth(wave -> 35.0 + ((wave - 15) * 2.75))
        .onSpawn(creature -> {
          creature.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 0, false, false));
          creature.setMetadata("VD_UNSTUNNABLE", new FixedMetadataValue(plugin, true));
          creature.setMetadata("VD_UNPOPPABLE", new FixedMetadataValue(plugin, true));
          creature.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(1.0);
          creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
        }).build()
    );
  }

  public VDEnemy getEnemyById(String id) {
    return registeredEnemies.stream()
      .filter(e -> e.getId().equals(id))
      .findFirst()
      .orElse(null);
  }

}
