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

package plugily.projects.villagedefense.arena.villager;

import com.destroystokyo.paper.entity.Pathfinder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.minigamesbox.classic.arena.ArenaState;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.villager.trait.AlchemistTrait;
import plugily.projects.villagedefense.arena.villager.trait.BabyTrait;
import plugily.projects.villagedefense.arena.villager.trait.BraveTrait;
import plugily.projects.villagedefense.arena.villager.trait.FishermanTrait;
import plugily.projects.villagedefense.arena.villager.trait.GenericTrait;
import plugily.projects.villagedefense.arena.villager.trait.ScaredTrait;
import plugily.projects.villagedefense.utils.NearbyUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class VillagerAiManager {

  public static final String VILLAGER_PERSONALITY_METADATA = "VD_VILLAGER_PERSONA";
  public static final String VILLAGER_PERSONALITY_CHOSEN_HOME_ID = "VD_VILLAGER_PERSONA_CHOSEN_HOME";

  private final @Getter Map<Place, List<Location>> places = new EnumMap<>(Place.class);
  private final List<Personality> personalities = new ArrayList<>();
  private final List<GenericTrait> registeredTraits = new ArrayList<>();
  private final Arena arena;

  public VillagerAiManager(Arena arena) {
    this.arena = arena;
    registeredTraits.add(new ScaredTrait(this));
    registeredTraits.add(new FishermanTrait(this));
    registeredTraits.add(new BabyTrait());
    registeredTraits.add(new AlchemistTrait(this));
    registeredTraits.add(new BraveTrait(this));
  }

  public void registerArenaPlace(Place place, Location location) {
    if (!places.containsKey(place)) {
      places.put(place, new ArrayList<>(Collections.singletonList(location)));
    } else {
      places.get(place).add(location);
    }
  }

  public void doApplyVillagerPersonality(Villager villager) {
    //based on 10 villagers per arena we will apply personalities
    //first set brave villager in the village then others, all else as scared
    if (personalities.stream().noneMatch(personality -> personality == Personality.BRAVE)) {
      villager.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
      villager.setMetadata(VILLAGER_PERSONALITY_METADATA, new FixedMetadataValue(arena.getPlugin(), Personality.BRAVE.name()));
      personalities.add(Personality.BRAVE);
    } else if (personalities.stream().noneMatch(personality -> personality == Personality.ALCHEMIST)) {
      villager.getEquipment().setItemInMainHand(new ItemStack(Material.SPLASH_POTION));
      villager.setMetadata(VILLAGER_PERSONALITY_METADATA, new FixedMetadataValue(arena.getPlugin(), Personality.ALCHEMIST.name()));
      personalities.add(Personality.ALCHEMIST);
    } else if (personalities.stream().noneMatch(personality -> personality == Personality.FISHERMAN)) {
      villager.getEquipment().setItemInMainHand(new ItemStack(Material.FISHING_ROD));
      villager.setMetadata(VILLAGER_PERSONALITY_METADATA, new FixedMetadataValue(arena.getPlugin(), Personality.FISHERMAN.name()));
      personalities.add(Personality.FISHERMAN);
    } else if (personalities.stream().filter(personality -> personality == Personality.BABY).count() < 2) {
      villager.setMetadata(VILLAGER_PERSONALITY_METADATA, new FixedMetadataValue(arena.getPlugin(), Personality.BABY.name()));
      villager.setBaby();
      personalities.add(Personality.BABY);
    } else {
      villager.setMetadata(VILLAGER_PERSONALITY_METADATA, new FixedMetadataValue(arena.getPlugin(), Personality.SCARED.name()));
      personalities.add(Personality.SCARED);
    }
    List<Location> homes = places.get(Place.VILLAGER_HOME_ZONE);
    villager.setMetadata(VILLAGER_PERSONALITY_CHOSEN_HOME_ID, new FixedMetadataValue(arena.getPlugin(), ThreadLocalRandom.current().nextInt(homes.size())));
  }

  public void clearCache() {
    personalities.clear();
  }

  public void doRetreatVillagers() {
    for (Villager villager : arena.getVillagers()) {
      Personality personality = Personality.valueOf(villager.getMetadata(VILLAGER_PERSONALITY_METADATA).get(0).asString());
      for (GenericTrait trait : registeredTraits) {
        if (trait.getPersonality() == personality) {
          trait.onRetreat(arena, villager);
          break;
        }
      }
    }
  }

  public void doStartPathfinder(Villager villager, Location location, PathfinderCallback callback) {
    Pathfinder pathfinder = villager.getPathfinder();
    double speed = ThreadLocalRandom.current().nextDouble(0.05, 0.25);
    new BukkitRunnable() {
      @Override
      public void run() {
        if (!villager.isValid() || arena.getArenaState() != ArenaState.IN_GAME) {
          this.cancel();
          return;
        }
        pathfinder.moveTo(location, 1.35 + speed);
        for (Block block : NearbyUtils.getNearbyBlocks(villager.getLocation(), 2)) {
          if (block.getBlockData() instanceof Door door) {
            if (door.getHalf() == Bisected.Half.BOTTOM || block.hasMetadata("VD_DOOR_LOCK")) {
              continue;
            }
            door.setOpen(true);
            block.setBlockData(door, true);
            block.setMetadata("VD_DOOR_LOCK", new FixedMetadataValue(arena.getPlugin(), true));
            Bukkit.getScheduler().runTaskLater(arena.getPlugin(), () -> {
              block.removeMetadata("VD_DOOR_LOCK", arena.getPlugin());
              Door doorBlock = (Door) block.getBlockData();
              if (block.getType() == Material.AIR || !doorBlock.isOpen()) {
                return;
              }
              doorBlock.setOpen(false);
              block.setBlockData(doorBlock);
            }, 40);
          }
        }
        if (villager.getLocation().distanceSquared(location) <= 2) {
          callback.onPathComplete(villager, location);
          this.cancel();
        }
      }
    }.runTaskTimer(arena.getPlugin(), 0, 5);
  }

  public void doSocializeVillagers() {
    for (Villager villager : arena.getVillagers()) {
      Personality personality = Personality.valueOf(villager.getMetadata(VILLAGER_PERSONALITY_METADATA).get(0).asString());
      for (GenericTrait trait : registeredTraits) {
        if (trait.getPersonality() == personality) {
          trait.onSocialize(arena, villager);
          break;
        }
      }
    }
  }

  public enum Place {
    VILLAGER_SOCIAL_ZONE,
    VILLAGER_HOME_ZONE,
    VILLAGER_ESCAPE_ZONE,
    VILLAGER_FISHING_ZONE
  }

  public enum Personality {
    BABY,
    SCARED,
    BRAVE,
    ALCHEMIST,
    FISHERMAN
  }

}
