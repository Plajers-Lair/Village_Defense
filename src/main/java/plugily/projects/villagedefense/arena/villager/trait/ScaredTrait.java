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

package plugily.projects.villagedefense.arena.villager.trait;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewCreatureUtils;
import plugily.projects.villagedefense.arena.villager.CompletionCallback;
import plugily.projects.villagedefense.arena.villager.VillagerAiAnimations;
import plugily.projects.villagedefense.arena.villager.VillagerAiManager;
import plugily.projects.villagedefense.utils.NearbyUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ScaredTrait implements GenericTrait {

  private final Main plugin = JavaPlugin.getPlugin(Main.class);
  private final List<ItemStack> randomFoods = Arrays.asList(
    new ItemStack(Material.BREAD),
    new ItemStack(Material.COOKED_CHICKEN),
    new ItemStack(Material.COOKIE),
    new ItemStack(Material.CARROT),
    new ItemStack(Material.BEETROOT_SOUP),
    new ItemStack(Material.POTION)
  );
  private final VillagerAiManager aiManager;

  public ScaredTrait(VillagerAiManager aiManager) {
    this.aiManager = aiManager;
  }

  @Override
  public VillagerAiManager.Personality getPersonality() {
    return VillagerAiManager.Personality.SCARED;
  }

  @Override
  public void onSocialize(Arena arena, Villager villager) {
    List<Location> socialZones = aiManager.getPlaces().get(VillagerAiManager.Place.VILLAGER_SOCIAL_ZONE);
    Location target = NearbyUtils.getRandomNearbyLocation(socialZones.get(ThreadLocalRandom.current().nextInt(socialZones.size())), 3);
    collectNearbyBabyAndMoveTo(villager, target, () -> {
      int roll = ThreadLocalRandom.current().nextInt(0, 3);
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        if (roll == 1) {
          doReadingSession(villager);
        } else if (roll == 2) {
          doEatingSession(villager);
        } else {
          int generousRoll = ThreadLocalRandom.current().nextInt(0, 4);
          if (generousRoll == 3) {
            doGenerousDonation(villager);
          }
        }
      }, 20L * ThreadLocalRandom.current().nextInt(2, 4));
    });
  }

  private void doReadingSession(Villager villager) {
    villager.getEquipment().setItemInMainHand(new ItemStack(Material.BOOK));
    new BukkitRunnable() {
      int ticks = 0;

      @Override
      public void run() {
        if (ticks >= 20 * 8) {
          villager.getEquipment().setItemInMainHand(null);
          this.cancel();
          return;
        }
        Location location = villager.getLocation();
        VillagerAiAnimations.makeEntityLookAt(villager, location.clone()
          .add(
            location.getDirection().getX() * 0.5,
            -0.35,
            location.getDirection().getZ() * 0.5
          ));
        if (ticks % 15 == 0) {
          villager.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, villager.getEyeLocation().add(0, 0.25, 0), 4, 0.2, 0.1, 0.2, 0);
        }
        ticks += 5;
      }
    }.runTaskTimer(plugin, 0, 5);
  }

  private void doEatingSession(Villager villager) {
    ItemStack rolledFood = randomFoods.get(ThreadLocalRandom.current().nextInt(randomFoods.size()));
    villager.getEquipment().setItemInMainHand(rolledFood);
    int eatTime = ThreadLocalRandom.current().nextInt(40, 65);
    VillagerAiAnimations.makeEntityEat(villager, rolledFood, eatTime, () -> {
      villager.setHealth(Math.min(villager.getHealth() + 1.0, VersionUtils.getMaxHealth(villager)));
      villager.setCustomName(NewCreatureUtils.getHealthNameTag(villager));
      if (ThreadLocalRandom.current().nextBoolean()) {
        villager.getWorld().playSound(villager.getLocation(), XSound.ENTITY_VILLAGER_YES.parseSound(), 1.0f, 1.0f);
      } else {
        villager.getWorld().playSound(villager.getLocation(), XSound.ENTITY_VILLAGER_AMBIENT.parseSound(), 1.0f, 1.0f);
      }
    });
  }

  private void doGenerousDonation(Villager villager) {
    for (Entity entity : villager.getNearbyEntities(12, 5, 12)) {
      if (!(entity instanceof Player player)) {
        continue;
      }
      if (player.getFoodLevel() < 20) {
        aiManager.doStartPathfinder(villager, player.getLocation(), (v, l) -> {
          villager.getWorld().playSound(villager.getLocation(), XSound.ENTITY_VILLAGER_TRADE.parseSound(), 1.0f, 1.0f);
          VillagerAiAnimations.makeEntityLookAt(villager, player.getEyeLocation());
          villager.getEquipment().setItemInMainHand(new ItemStack(Material.APPLE));
          Bukkit.getScheduler().runTaskLater(plugin, () -> {
            //generous donation towards player
            VillagerAiAnimations.throwItemTowardsEntity(new ItemStack(Material.APPLE), villager.getEyeLocation(), player);
            villager.getEquipment().setItemInMainHand(null);
          }, 20 * 2);
        });
        return;
      }
    }
  }

  @Override
  public void onRetreat(Arena arena, Villager villager) {
    int homeIndex = villager.getMetadata(VillagerAiManager.VILLAGER_PERSONALITY_CHOSEN_HOME_ID).get(0).asInt();
    Location home = NearbyUtils.getRandomNearbyLocation(aiManager.getPlaces().get(VillagerAiManager.Place.VILLAGER_HOME_ZONE).get(homeIndex), 2);
    collectNearbyBabyAndMoveTo(villager, home, () -> {
    });
  }

  private void collectNearbyBabyAndMoveTo(Villager villager, Location moveLocation, CompletionCallback callback) {
    if (villager.getPassengers().isEmpty()) {
      for (Entity nearby : villager.getNearbyEntities(7, 7, 7)) {
        if (!(nearby instanceof Villager nearbyVillager)) {
          continue;
        }
        if (nearbyVillager.isAdult() || nearbyVillager.hasMetadata("VD_BABY_ESCORTED")) {
          continue;
        }
        nearbyVillager.setMetadata("VD_BABY_ESCORTED", new FixedMetadataValue(plugin, true));
        aiManager.doStartPathfinder(villager, nearbyVillager.getLocation(), (v, l) -> {
          nearbyVillager.setInvulnerable(true);
          villager.addPassenger(nearbyVillager);
          aiManager.doStartPathfinder(villager, moveLocation, (v2, l2) -> {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
              if (villager.getPassengers().isEmpty()) {
                return;
              }
              villager.removePassenger(nearbyVillager);
              nearbyVillager.setInvulnerable(false);
              nearbyVillager.removeMetadata("VD_BABY_ESCORTED", plugin);
            }, 20 * 2);
          });
        });
        return;
      }
    }
    aiManager.doStartPathfinder(villager, moveLocation, (v, l) -> {
      callback.onComplete();
    });
  }

}
