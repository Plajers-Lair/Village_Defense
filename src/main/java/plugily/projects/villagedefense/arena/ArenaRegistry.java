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

package plugily.projects.villagedefense.arena;

import org.bukkit.Difficulty;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.arena.PluginArenaRegistry;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.serialization.LocationSerializer;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.villager.VillagerAiManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 27/07/2014.
 */
public class ArenaRegistry extends PluginArenaRegistry {

  private final Main plugin;

  public ArenaRegistry(Main plugin) {
    super(plugin);
    this.plugin = plugin;
  }


  @Override
  public PluginArena getNewArena(String id) {
    return new Arena(id);
  }

  @Override
  public boolean additionalValidatorChecks(ConfigurationSection section, PluginArena arena, String id) {
    boolean checks = super.additionalValidatorChecks(section, arena, id);
    if(!checks) return false;

    if(!section.getBoolean(id + ".isdone")) {
      plugin.getDebugger().sendConsoleMsg(new MessageBuilder("VALIDATOR_INVALID_ARENA_CONFIGURATION").asKey().value("NOT VALIDATED").arena(arena).build());
      return false;
    }

    List<String> zombieSection = section.getStringList(id + ".zombiespawns");
    if(zombieSection.isEmpty()) {
      plugin.getDebugger().sendConsoleMsg(new MessageBuilder("VALIDATOR_INVALID_ARENA_CONFIGURATION").asKey().value("ZOMBIE SPAWNS").arena(arena).build());
      return false;
    } else {
      for(String string : zombieSection) {
        ((Arena) arena).addZombieSpawn(LocationSerializer.getLocation(string));
      }
    }

    List<String> villagerSection = section.getStringList(id + ".villagerspawns");
    if(villagerSection.isEmpty()) {
      plugin.getDebugger().sendConsoleMsg(new MessageBuilder("VALIDATOR_INVALID_ARENA_CONFIGURATION").asKey().value("VILLAGER SPAWNS").arena(arena).build());
      return false;
    } else {
      for(String string : villagerSection) {
        ((Arena) arena).addVillagerSpawn(LocationSerializer.getLocation(string));
      }
    }

    List<String> doorSection = section.getStringList(id + ".doors");
    for(String location : doorSection) {
      ((Arena) arena).getMapRestorerManager().addDoor(LocationSerializer.getLocation(location));
    }


    if(arena.getStartLocation().getWorld().getDifficulty() == Difficulty.PEACEFUL) {
      plugin.getDebugger().sendConsoleMsg(new MessageBuilder("VALIDATOR_INVALID_ARENA_CONFIGURATION").asKey().value("THERE IS A WRONG " +
        "DIFFICULTY -> SET IT TO ANOTHER ONE THAN PEACEFUL - WE SET IT TO EASY").arena(arena).build());
      arena.getStartLocation().getWorld().setDifficulty(Difficulty.EASY);
    }

    for (String location : section.getStringList(id + ".villager-ai.socialZones")) {
      ((Arena) arena).getVillagerAiManager().registerArenaPlace(VillagerAiManager.Place.VILLAGER_SOCIAL_ZONE, LocationSerializer.getLocation(location));
    }
    for (String location : section.getStringList(id + ".villager-ai.homeZones")) {
      ((Arena) arena).getVillagerAiManager().registerArenaPlace(VillagerAiManager.Place.VILLAGER_HOME_ZONE, LocationSerializer.getLocation(location));
    }
    //todo
    /*for(String location : section.getStringList(id + ".villager-ai.escapeZones")) {
      ((Arena) arena).getVillagerAiManager().registerArenaPlace(VillagerAiManager.Place.VILLAGER_ESCAPE_ZONE, LocationSerializer.getLocation(location));
    }*/
    for (String location : section.getStringList(id + ".villager-ai.fishingZones")) {
      ((Arena) arena).getVillagerAiManager().registerArenaPlace(VillagerAiManager.Place.VILLAGER_FISHING_ZONE, LocationSerializer.getLocation(location));
    }
    return true;
  }

  @Override
  public @Nullable Arena getArena(Player player) {
    PluginArena pluginArena = super.getArena(player);
    if(pluginArena instanceof Arena) {
      return (Arena) pluginArena;
    }
    return null;
  }

  @Override
  public @Nullable Arena getArena(String id) {
    PluginArena pluginArena = super.getArena(id);
    if(pluginArena instanceof Arena) {
      return (Arena) pluginArena;
    }
    return null;
  }

  public @NotNull List<Arena> getPluginArenas() {
    List<Arena> arenas = new ArrayList<>();
    for(PluginArena pluginArena : super.getArenas()) {
      if(pluginArena instanceof Arena) {
        arenas.add((Arena) pluginArena);
      }
    }
    return arenas;
  }

}
