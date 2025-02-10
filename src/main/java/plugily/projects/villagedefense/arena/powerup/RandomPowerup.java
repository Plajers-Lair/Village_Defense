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

package plugily.projects.villagedefense.arena.powerup;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XParticle;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewCreatureUtils;
import plugily.projects.villagedefense.handlers.hologram.ArmorStandHologram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

public class RandomPowerup implements Powerup {

  private final List<LocalRandomPowerup> localPowerups = new ArrayList<>();
  private final Main plugin;

  public RandomPowerup(Main plugin) {
    this.plugin = plugin;
    registerLocalPowerups();
  }

  private void registerLocalPowerups() {
    String powerupMessage = color("&7You received %name%&7 powerup from %player%!");
    localPowerups.add(new LocalRandomPowerup(
      color("&e&lREJUVENATION"),
      color("&7Heal and absorption for 5s for you and your pets!"),
      (arena, player) -> {
        for (Player target : arena.getPlayersLeft()) {
          target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 0));
          target.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 5, 0));
          player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.25f);
          player.sendMessage(powerupMessage.replace("%name%", color("&e&lREJUVENATION")).replace("%player%", player.getName()));
        }
        List<LivingEntity> pets = new ArrayList<>();
        pets.addAll(arena.getWolves());
        pets.addAll(arena.getIronGolems());
        for (LivingEntity pet : pets) {
          pet.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 0));
          pet.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 5, 0));
        }
      }
    ));
    localPowerups.add(new LocalRandomPowerup(
      color("&b&lSWIFTNESS"),
      color("&7Speed boost for 5s for you and your pets!"),
      (arena, player) -> {
        for (Player target : arena.getPlayersLeft()) {
          target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 0));
          player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.25f);
          player.sendMessage(powerupMessage.replace("%name%", color("&b&lSWIFTNESS")).replace("%player%", player.getName()));
        }
        List<LivingEntity> pets = new ArrayList<>();
        pets.addAll(arena.getWolves());
        pets.addAll(arena.getIronGolems());
        for (LivingEntity pet : pets) {
          pet.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 5, 0));
          pet.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 5, 0));
        }
      }
    ));
    localPowerups.add(new LocalRandomPowerup(
      color("&d&lTIME STOP"),
      color("&7Alive enemies are frozen in time for 3 seconds!"),
      (arena, player) -> {
        for (Creature creature : arena.getEnemies()) {
          NewCreatureUtils.doStunEnemy(creature, 3);
        }
      }
    ));
    localPowerups.add(new LocalRandomPowerup(
      color("&6&lJACKPOT"),
      color("&7You won jackpot prize of 300 orbs!"),
      (arena, player) -> {
        int orbs = 300;
        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.25f);
        plugin.getUserManager().getUser(player).adjustStatistic(plugin.getStatsStorage().getStatisticType("ORBS"), orbs);
        player.sendMessage(powerupMessage.replace("%name%", color("&6&lJACKPOT")).replace("%player%", player.getName()));
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a+" + orbs + " &7orbs"));
      }
    ));
    //todo test divine barrier
    localPowerups.add(new LocalRandomPowerup(
      color("&f&lDIVINE BARRIER"),
      color("&7Villagers are protected from damage for 10s!"),
      (arena, player) -> {
        String villagerPowerupMessage = color("&7Villagers received %name%&7 powerup from %player%!");
        for (Villager villager : arena.getVillagers()) {
          villager.setNoDamageTicks(20 * 10);
          for (Player target : arena.getPlayers()) {
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.25f);
            player.sendMessage(villagerPowerupMessage.replace("%name%", color("&f&lDIVINE BARRIER")).replace("%player%", player.getName()));
            try {
              plugin.getGlowingEntities().setGlowing(villager, target, ChatColor.YELLOW);
            } catch (ReflectiveOperationException ignored) {
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
              try {
                plugin.getGlowingEntities().unsetGlowing(villager, target);
              } catch (ReflectiveOperationException ignored) {
              }
            }, 20 * 10);
          }
        }
      }
    ));
  }

  private String color(String text) {
    return ChatColor.translateAlternateColorCodes('&', text);
  }

  @Override
  public boolean canSpawn(Arena arena) {
    return true;
  }

  @Override
  public void spawn(Arena arena, Location location) {
    ArmorStandHologram hologram = new ArmorStandHologram(location.clone().add(0, -1.5, 0))
      .appendItem(XMaterial.BLAZE_POWDER.parseItem())
      .appendLine(ChatColor.translateAlternateColorCodes('&', "&e&lPOWERUP"));
    hologram.setPickupHandler(player -> {
      if (!plugin.getArenaRegistry().getArena(player).equals(arena)) {
        return;
      }
      XSound.ENTITY_PLAYER_LEVELUP.play(player, 1, 0);
      XSound.ENTITY_VILLAGER_YES.play(player);
      hologram.delete();
      onPickup(arena, player);
    });
    List<ItemStack> materials = Arrays.asList(XMaterial.BLAZE_POWDER.parseItem(), XMaterial.COCOA_BEANS.parseItem(), XMaterial.ANVIL.parseItem(), XMaterial.NETHER_STAR.parseItem());

    new BukkitRunnable() {
      boolean toggle = true;
      boolean grounded = false;
      boolean flyReverse = false;
      int tick = 0;
      int index = 0;

      @Override
      public void run() {
        if (tick == 200 || hologram.isDeleted() || hologram.getArmorStands().isEmpty()) {
          hologram.getLocation().getWorld().spawnParticle(XParticle.getParticle("EXPLOSION_HUGE"), hologram.getLocation(), 1);
          hologram.delete();
          cancel();
          return;
        }
        if (hologram.getEntityItem().isOnGround() && !grounded) {
          grounded = true;
          hologram.getEntityItem().setGravity(false);
          hologram.getEntityItem().teleport(hologram.getEntityItem().getLocation().add(0, 0.75, 0));
        }
        if (grounded) {
          hologram.getEntityItem().setGravity(true);
          Location newLocation = hologram.getEntityItem().getLocation().clone().add(0, 0.015 * (flyReverse ? -1 : 1), 0);
          hologram.getEntityItem().teleport(newLocation);
          hologram.getEntityItem().setGravity(false);
          if (tick % 60 == 0) {
            flyReverse = !flyReverse;
          }
        }
        ArmorStand stand = hologram.getArmorStands().get(0);
        int modulo = tick < 100 ? 10 : tick > 150 ? 3 : 5;
        if (tick % modulo == 0) {
          String message;
          if (toggle) {
            message = ChatColor.translateAlternateColorCodes('&', "&c&lRANDOM POWERUP (" + ((200 - tick) / 20) + "s)");
          } else {
            message = ChatColor.translateAlternateColorCodes('&', "&e&lRANDOM POWERUP (" + ((200 - tick) / 20) + "s)");
          }
          toggle = !toggle;
          stand.setCustomName(message);
        }
        if (tick % 10 == 0) {
          hologram.getEntityItem().setItemStack(materials.get(index));
          index++;
          if (index >= materials.size()) {
            index = 0;
          }

          stand.getLocation().getWorld().spawnParticle(XParticle.getParticle("LAVA"), hologram.getEntityItem().getLocation(), 2);
        }
        stand.teleport(hologram.getEntityItem().getLocation().clone().add(0, -1.25, 0));
        tick++;
      }
    }.runTaskTimer(plugin, 1, 1);
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (!hologram.isDeleted()) {
        hologram.delete();
      }
    }, /* remove after 40 seconds to prevent staying even if arena is finished */ 20 * 40);
  }

  private void onPickup(Arena arena, Player player) {
    Collections.shuffle(localPowerups);
    for (LocalRandomPowerup powerup : localPowerups) {
      powerup.onPickup().accept(arena, player);
      break;
    }
  }

  private record LocalRandomPowerup(String name, String description, BiConsumer<Arena, Player> onPickup) {
  }

}
