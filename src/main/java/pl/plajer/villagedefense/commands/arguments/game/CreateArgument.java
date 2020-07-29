/*
 * Village Defense - Protect villagers from hordes of zombies
 * Copyright (C) 2020  Plugily Projects - maintained by 2Wild4You, Tigerpanzer_02 and contributors
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

package pl.plajer.villagedefense.commands.arguments.game;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import pl.plajer.villagedefense.arena.Arena;
import pl.plajer.villagedefense.arena.ArenaRegistry;
import pl.plajer.villagedefense.arena.ArenaUtils;
import pl.plajer.villagedefense.commands.arguments.ArgumentsRegistry;
import pl.plajer.villagedefense.commands.arguments.data.CommandArgument;
import pl.plajer.villagedefense.commands.arguments.data.LabelData;
import pl.plajer.villagedefense.commands.arguments.data.LabeledCommandArgument;
import pl.plajer.villagedefense.handlers.language.Messages;
import pl.plajer.villagedefense.handlers.setup.SetupInventory;
import pl.plajer.villagedefense.utils.constants.Constants;
import pl.plajerlair.commonsbox.minecraft.configuration.ConfigUtils;
import pl.plajerlair.commonsbox.minecraft.serialization.LocationSerializer;

import java.util.ArrayList;

/**
 * @author Plajer
 * <p>
 * Created at 24.11.2018
 */
public class CreateArgument {

  private final ArgumentsRegistry registry;

  public CreateArgument(ArgumentsRegistry registry) {
    this.registry = registry;
    registry.mapArgument("villagedefense", new LabeledCommandArgument("create", "villagedefense.admin.create", CommandArgument.ExecutorType.PLAYER,
        new LabelData("/vd create &6<arena>", "/vd create <arena>",
            "&7Create new arena\n&6Permission: &7villagedefense.admin.create")) {
      @Override
      public void execute(CommandSender sender, String[] args) {
        if (args.length == 1) {
          sender.sendMessage(registry.getPlugin().getChatManager().colorMessage(Messages.COMMANDS_TYPE_ARENA_NAME));
          return;
        }
        Player player = (Player) sender;
        for (Arena arena : ArenaRegistry.getArenas()) {
          if (arena.getId().equalsIgnoreCase(args[1])) {
            player.sendMessage(ChatColor.DARK_RED + "Arena with that ID already exists!");
            player.sendMessage(ChatColor.DARK_RED + "Usage: /vd create <ID>");
            return;
          }
        }
        if (ConfigUtils.getConfig(registry.getPlugin(), Constants.Files.ARENAS.getName()).contains("instances." + args[1])) {
          player.sendMessage(ChatColor.DARK_RED + "Instance/Arena already exists! Use another ID or delete it first!");
        } else {
          createInstanceInConfig(args[1], player.getWorld().getName());
          player.sendMessage(ChatColor.BOLD + "------------------------------------------");
          player.sendMessage(ChatColor.YELLOW + "      Instance " + args[1] + " created!");
          player.sendMessage("");
          player.sendMessage(ChatColor.GREEN + "Edit this arena via " + ChatColor.GOLD + "/vd " + args[1] + " edit" + ChatColor.GREEN + "!");
          player.sendMessage(ChatColor.GOLD + "Don't know where to start? Check out tutorial video:");
          player.sendMessage(ChatColor.GOLD + SetupInventory.VIDEO_LINK);
          player.sendMessage(ChatColor.BOLD + "------------------------------------------- ");
        }
      }
    });
  }

  private void createInstanceInConfig(String id, String worldName) {
    String path = "instances." + id + ".";
    FileConfiguration config = ConfigUtils.getConfig(registry.getPlugin(), Constants.Files.ARENAS.getName());
    LocationSerializer.saveLoc(registry.getPlugin(), config, Constants.Files.ARENAS.getName(), path + "lobbylocation", Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
    LocationSerializer.saveLoc(registry.getPlugin(), config, Constants.Files.ARENAS.getName(), path + "Startlocation", Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
    LocationSerializer.saveLoc(registry.getPlugin(), config, Constants.Files.ARENAS.getName(), path + "Endlocation", Bukkit.getServer().getWorlds().get(0).getSpawnLocation());
    config.set(path + "minimumplayers", 1);
    config.set(path + "maximumplayers", 10);
    config.set(path + "mapname", id);
    config.set(path + "signs", new ArrayList<>());
    config.set(path + "isdone", false);
    config.set(path + "world", worldName);
    ConfigUtils.saveConfig(registry.getPlugin(), config, Constants.Files.ARENAS.getName());

    Arena arena = ArenaUtils.initializeArena(id);

    arena.setMinimumPlayers(config.getInt(path + "minimumplayers"));
    arena.setMaximumPlayers(config.getInt(path + "maximumplayers"));
    arena.setMapName(config.getString(path + "mapname"));
    arena.setLobbyLocation(LocationSerializer.getLocation(config.getString(path + "lobbylocation")));
    arena.setStartLocation(LocationSerializer.getLocation(config.getString(path + "Startlocation")));
    arena.setEndLocation(LocationSerializer.getLocation(config.getString(path + "Endlocation")));
    ArenaUtils.setWorld(arena);
    arena.setReady(false);

    ArenaRegistry.registerArena(arena);
  }

}
