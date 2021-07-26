/*
 * Village Defense - Protect villagers from hordes of zombies
 * Copyright (C) 2021  Plugily Projects - maintained by 2Wild4You, Tigerpanzer_02 and contributors
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugily.projects.commonsbox.minecraft.configuration.ConfigUtils;
import plugily.projects.commonsbox.minecraft.serialization.LocationSerializer;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.handlers.language.Messages;
import plugily.projects.villagedefense.utils.Debugger;
import plugily.projects.villagedefense.utils.constants.Constants;

/**
 * Created by Tom on 27/07/2014.
 */
public class ArenaRegistry {

  private static final List<Arena> arenas = new ArrayList<>();
  private static Main plugin;
  private static final List<World> arenaIngameWorlds = new ArrayList<>();

  private static int bungeeArena = -999;

  private ArenaRegistry() {
  }

  public static void init(Main plugin) {
    ArenaRegistry.plugin = plugin;
  }

  /**
   * Checks if player is in any arena
   *
   * @param player player to check
   * @return true when player is in arena, false if otherwise
   */
  public static boolean isInArena(@NotNull Player player) {
    return getArena(player) != null;
  }

  /**
   * Returns arena where the player is
   *
   * @param player target player
   * @return Arena or null if not playing
   * @see #isInArena(Player) to check if player is playing
   */
  @Nullable
  public static Arena getArena(Player player) {
    if(player == null || !player.isOnline()) {
      return null;
    }

    for(Arena loopArena : arenas) {
      for(Player arenaPlayer : loopArena.getPlayers()) {
        if(arenaPlayer.getUniqueId().equals(player.getUniqueId())) {
          return loopArena;
        }
      }
    }

    return null;
  }

  /**
   * Returns arena based by ID
   *
   * @param id name of arena
   * @return Arena or null if not found
   */
  @Nullable
  public static Arena getArena(String id) {
    for(Arena loopArena : arenas) {
      if(loopArena.getId().equalsIgnoreCase(id)) {
        return loopArena;
      }
    }
    return null;
  }

  public static int getArenaPlayersOnline() {
    int players = 0;
    for(Arena arena : arenas) {
      players += arena.getPlayers().size();
    }
    return players;
  }

  public static void registerArena(Arena arena) {
    Debugger.debug("[{0}] Instance registered", arena.getId());
    arenas.add(arena);

    World startLocWorld = arena.getStartLocation().getWorld();
    if (startLocWorld != null)
      arenaIngameWorlds.add(startLocWorld);
  }

  public static void unregisterArena(Arena arena) {
    Debugger.debug("[{0}] Instance unregistered", arena.getId());
    arenas.remove(arena);

    World startLocWorld = arena.getStartLocation().getWorld();
    if (startLocWorld != null)
      arenaIngameWorlds.remove(startLocWorld);
  }

  public static void registerArenas() {
    Debugger.debug("[ArenaRegistry] Initial arenas registration");
    long start = System.currentTimeMillis();

    if(!arenas.isEmpty()) {
      for(Arena arena : new ArrayList<>(arenas)) {
        arena.getMapRestorerManager().clearEnemiesFromArena();
        arena.getMapRestorerManager().clearVillagersFromArena();
        arena.getMapRestorerManager().clearWolvesFromArena();
        arena.getMapRestorerManager().clearGolemsFromArena();
        unregisterArena(arena);
      }
    }

    FileConfiguration config = ConfigUtils.getConfig(plugin, Constants.Files.ARENAS.getName());
    ConfigurationSection section = config.getConfigurationSection("instances");
    if(section == null) {
      Debugger.sendConsoleMsg(plugin.getChatManager().colorMessage(Messages.VALIDATOR_NO_INSTANCES_CREATED));
      return;
    }

    for(String id : section.getKeys(false)) {
      if(id.equalsIgnoreCase("default")) {
        continue;
      }

      Arena arena = new Arena(id);

      arena.setMinimumPlayers(section.getInt(id + ".minimumplayers", 1));
      arena.setMaximumPlayers(section.getInt(id + ".maximumplayers", 2));
      arena.setMapName(section.getString(id + ".mapname", "none"));

      Location startLoc = LocationSerializer.getLocation(section.getString(id + ".Startlocation", "world,364.0,63.0,-72.0,0.0,0.0"));
      Location lobbyLoc = LocationSerializer.getLocation(section.getString(id + ".lobbylocation", "world,364.0,63.0,-72.0,0.0,0.0"));
      Location endLoc = LocationSerializer.getLocation(section.getString(id + ".Endlocation", "world,364.0,63.0,-72.0,0.0,0.0"));
      if(lobbyLoc == null || lobbyLoc.getWorld() == null || startLoc == null || startLoc.getWorld() == null
          || endLoc == null || endLoc.getWorld() == null) {
        section.set(id + ".isdone", false);
      } else {
        arena.setLobbyLocation(lobbyLoc);
        arena.setStartLocation(startLoc);
        arena.setEndLocation(endLoc);
      }

      if(!section.getBoolean(id + ".isdone")) {
        Debugger.sendConsoleMsg(plugin.getChatManager().colorMessage(Messages.VALIDATOR_INVALID_ARENA_CONFIGURATION).replace("%arena%", id).replace("%error%", "NOT VALIDATED"));
        arena.setReady(false);
        registerArena(arena);
        continue;
      }

      ConfigurationSection zombieSection = section.getConfigurationSection(id + ".zombiespawns");
      if(zombieSection != null) {
        for(String string : zombieSection.getKeys(false)) {
          arena.addZombieSpawn(LocationSerializer.getLocation(zombieSection.getString(string)));
        }
      } else {
        Debugger.sendConsoleMsg(plugin.getChatManager().colorMessage(Messages.VALIDATOR_INVALID_ARENA_CONFIGURATION).replace("%arena%", id).replace("%error%", "ZOMBIE SPAWNS"));
        arena.setReady(false);
        registerArena(arena);
        continue;
      }

      ConfigurationSection villagerSection = section.getConfigurationSection(id + ".villagerspawns");
      if(villagerSection != null) {
        for(String string : villagerSection.getKeys(false)) {
          arena.addVillagerSpawn(LocationSerializer.getLocation(villagerSection.getString(string)));
        }
      } else {
        Debugger.sendConsoleMsg(plugin.getChatManager().colorMessage(Messages.VALIDATOR_INVALID_ARENA_CONFIGURATION).replace("%arena%", id).replace("%error%", "VILLAGER SPAWNS"));
        arena.setReady(false);
        registerArena(arena);
        continue;
      }

      ConfigurationSection doorSection = section.getConfigurationSection(id + ".doors");
      if(doorSection != null) {
        for(String string : doorSection.getKeys(false)) {
          arena.getMapRestorerManager().addDoor(LocationSerializer.getLocation(doorSection.getString(string + ".location")),
              (byte) doorSection.getInt(string + ".byte"));
        }
      } else {
        Debugger.sendConsoleMsg(plugin.getChatManager().colorMessage(Messages.VALIDATOR_INVALID_ARENA_CONFIGURATION).replace("%arena%", id).replace("%error%", "DOORS"));
        arena.setReady(false);
        registerArena(arena);
        continue;
      }

      World startLocWorld = arena.getStartLocation().getWorld();
      if (startLocWorld == null) {
        Debugger.sendConsoleMsg("Arena world of " + id + " does not exist or not loaded.");
        continue;
      }

      if(startLocWorld.getDifficulty() == Difficulty.PEACEFUL) {
        Debugger.sendConsoleMsg(plugin.getChatManager().colorMessage(Messages.VALIDATOR_INVALID_ARENA_CONFIGURATION).replace("%arena%", id).replace("%error%", "THERE IS A WRONG " +
            "DIFFICULTY -> SET IT TO ANOTHER ONE THAN PEACEFUL"));
        arena.setReady(false);
        registerArena(arena);
        continue;
      }

      registerArena(arena);
      arena.start();
      Debugger.sendConsoleMsg(plugin.getChatManager().colorMessage(Messages.VALIDATOR_INSTANCE_STARTED).replace("%arena%", id));
    }
    ConfigUtils.saveConfig(plugin, config, Constants.Files.ARENAS.getName());
    Debugger.debug("[ArenaRegistry] Arenas registration completed took {0}ms", System.currentTimeMillis() - start);
  }

  @NotNull
  public static List<Arena> getArenas() {
    return arenas;
  }

  public static List<World> getArenaIngameWorlds() {
    return arenaIngameWorlds;
  }

  public static void shuffleBungeeArena() {
    if(!arenas.isEmpty()) {
      bungeeArena = ThreadLocalRandom.current().nextInt(arenas.size());
    }
  }

  public static int getBungeeArena() {
    if(bungeeArena == -999 && !arenas.isEmpty()) {
      bungeeArena = ThreadLocalRandom.current().nextInt(arenas.size());
    }
    return bungeeArena;
  }
}
