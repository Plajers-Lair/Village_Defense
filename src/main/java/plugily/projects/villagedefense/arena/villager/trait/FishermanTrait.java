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
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Bat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewCreatureUtils;
import plugily.projects.villagedefense.arena.villager.VillagerAiAnimations;
import plugily.projects.villagedefense.arena.villager.VillagerAiManager;
import plugily.projects.villagedefense.utils.NearbyUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FishermanTrait implements GenericTrait {

  private final VillagerAiManager aiManager;
  private final List<ItemStack> fishedItems = Arrays.asList(
    new ItemStack(Material.COD),
    new ItemStack(Material.SALMON),
    new ItemStack(Material.TROPICAL_FISH),
    new ItemStack(Material.ROTTEN_FLESH)
  );

  public FishermanTrait(VillagerAiManager aiManager) {
    this.aiManager = aiManager;
  }

  @Override
  public VillagerAiManager.Personality getPersonality() {
    return VillagerAiManager.Personality.FISHERMAN;
  }

  @Override
  public void onSocialize(Arena arena, Villager villager) {
    //50% chance to socialize or go fishing
    if (ThreadLocalRandom.current().nextBoolean()) {
      List<Location> socialZones = aiManager.getPlaces().get(VillagerAiManager.Place.VILLAGER_SOCIAL_ZONE);
      Location target = NearbyUtils.getRandomNearbyLocation(socialZones.get(ThreadLocalRandom.current().nextInt(socialZones.size())), 3);
      aiManager.doStartPathfinder(villager, target, (v, l) -> {
      });
      return;
    }
    doGoFishing(arena, villager);
  }

  private void doGoFishing(Arena arena, Villager villager) {
    List<Location> fishingZones = aiManager.getPlaces().get(VillagerAiManager.Place.VILLAGER_FISHING_ZONE);
    Location moveLocation = fishingZones.get(ThreadLocalRandom.current().nextInt(fishingZones.size()));
    aiManager.doStartPathfinder(villager, moveLocation, (v, l) -> {
      villager.setAI(false);
      launchRope(arena, villager);
    });
  }

  private void launchRope(Arena arena, Villager villager) {
    for (Block block : NearbyUtils.getNearbyBlocks(villager.getLocation(), 5)) {
      if (block.getType() == Material.WATER) {
        if (block.getRelative(BlockFace.UP).getType() != Material.AIR) {
          continue;
        } else if (block.getRelative(BlockFace.NORTH).getType() != Material.WATER && !isNearbyWater(block.getRelative(BlockFace.NORTH))) {
          continue;
        } else if (block.getRelative(BlockFace.EAST).getType() != Material.WATER && !isNearbyWater(block.getRelative(BlockFace.EAST))) {
          continue;
        } else if (block.getRelative(BlockFace.SOUTH).getType() != Material.WATER && !isNearbyWater(block.getRelative(BlockFace.SOUTH))) {
          continue;
        } else if (block.getRelative(BlockFace.WEST).getType() != Material.WATER && !isNearbyWater(block.getRelative(BlockFace.WEST))) {
          continue;
        }
        Location initialLocation = block.getLocation().add(0, 0.25, 0);
        Bat inWater = (Bat) villager.getWorld().spawnEntity(initialLocation, EntityType.BAT, CreatureSpawnEvent.SpawnReason.CUSTOM, e -> {
          e.setInvisible(true);
          e.setInvulnerable(true);
          e.setSilent(true);
        });
        Bat atLand = (Bat) villager.getWorld().spawnEntity(villager.getEyeLocation().add(0, -0.25, 0), EntityType.BAT, CreatureSpawnEvent.SpawnReason.CUSTOM, e -> {
          e.setInvisible(true);
          e.setInvulnerable(true);
          e.setSilent(true);
        });
        inWater.setAI(false);
        atLand.setAI(false);
        inWater.setLeashHolder(atLand);
        VillagerAiAnimations.makeEntityLookAt(villager, initialLocation);
        new BukkitRunnable() {
          int ticks = 0;

          @Override
          public void run() {
            if (ticks % 20 == 0) {
              inWater.teleport(initialLocation.add(ThreadLocalRandom.current().nextDouble(0, 0.15), 0, ThreadLocalRandom.current().nextDouble(0, 0.15)));
            }
            if (ticks == 20 * 10) {
              Item item = VillagerAiAnimations.throwItemTowardsEntity(new ItemStack(Material.COD), inWater.getLocation(), villager);
              item.setCanPlayerPickup(false);
              item.setCanMobPickup(false);
              ItemStack fishedItem = rollFishedItem();
              Bukkit.getScheduler().runTaskLater(arena.getPlugin(), () -> {
                item.remove();
                villager.getEquipment().setItemInMainHand(fishedItem);
              }, 30);
              inWater.remove();
              atLand.remove();
              Bukkit.getScheduler().runTaskLater(arena.getPlugin(), () -> {
                Location location = villager.getLocation();
                VillagerAiAnimations.makeEntityLookAt(villager, location.clone()
                  .add(
                    location.getDirection().getX() * 0.5,
                    -0.35,
                    location.getDirection().getZ() * 0.5
                  ));
                double healthPercent = villager.getHealth() / villager.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                if (healthPercent <= 0.75) {
                  VillagerAiAnimations.makeEntityEat(villager, fishedItem, 50, () -> {
                    if (fishedItem.getType() == Material.ROTTEN_FLESH) {
                      villager.damage(1.0);
                      villager.getWorld().playSound(villager.getLocation(), XSound.ENTITY_VILLAGER_NO.parseSound(), 1.0f, 1.0f);
                    } else {
                      villager.setHealth(Math.min(villager.getHealth() + 2.0, VersionUtils.getMaxHealth(villager)));
                    }
                    villager.setCustomName(NewCreatureUtils.getHealthNameTag(villager));
                    Bukkit.getScheduler().runTaskLater(arena.getPlugin(), () -> {
                      villager.setAI(true);
                      villager.getEquipment().setItemInMainHand(new ItemStack(Material.FISHING_ROD));
                    }, 15);
                  });
                } else {
                  Bukkit.getScheduler().runTaskLater(arena.getPlugin(), () -> {
                    villager.setAI(true);
                    villager.getEquipment().setItemInMainHand(new ItemStack(Material.FISHING_ROD));
                  }, 15);
                }
              }, 20 * 2);
              this.cancel();
              return;
            }
            ticks += 2;
          }
        }.runTaskTimer(arena.getPlugin(), 0, 2);
        return;
      }
    }
  }

  private ItemStack rollFishedItem() {
    return fishedItems.get(ThreadLocalRandom.current().nextInt(fishedItems.size()));
  }

  private boolean isNearbyWater(Block block) {
    if (block.getRelative(BlockFace.NORTH).getType() != Material.WATER) {
      return false;
    } else if (block.getRelative(BlockFace.EAST).getType() != Material.WATER) {
      return false;
    } else if (block.getRelative(BlockFace.SOUTH).getType() != Material.WATER) {
      return false;
    } else {
      return block.getRelative(BlockFace.WEST).getType() == Material.WATER;
    }
  }

  @Override
  public void onRetreat(Arena arena, Villager villager) {
    int homeIndex = villager.getMetadata(VillagerAiManager.VILLAGER_PERSONALITY_CHOSEN_HOME_ID).get(0).asInt();
    Location home = NearbyUtils.getRandomNearbyLocation(aiManager.getPlaces().get(VillagerAiManager.Place.VILLAGER_HOME_ZONE).get(homeIndex), 2);

    if (home.distanceSquared(villager.getLocation()) >= 6) {
      aiManager.doStartPathfinder(villager, home, (v, l) -> {
      });
    }
  }

}
