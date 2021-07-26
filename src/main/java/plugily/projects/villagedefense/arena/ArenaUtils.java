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

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import plugily.projects.commonsbox.minecraft.compat.VersionUtils;
import plugily.projects.commonsbox.minecraft.serialization.InventorySerializer;
import plugily.projects.villagedefense.ConfigPreferences;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.api.event.player.VillagePlayerRespawnEvent;
import plugily.projects.villagedefense.handlers.language.Messages;
import plugily.projects.villagedefense.user.User;
import plugily.projects.villagedefense.utils.Utils;

/**
 * @author Plajer
 * <p>
 * Created at 13.03.2018
 */
public class ArenaUtils {

  private static Main plugin;

  private ArenaUtils() {
  }

  public static void init(Main plugin) {
    ArenaUtils.plugin = plugin;
  }

  public static void hidePlayer(Player p, Arena arena) {
    for(Player player : arena.getPlayers()) {
      VersionUtils.hidePlayer(plugin, player, p);
    }
  }

  public static void showPlayer(Player p, Arena arena) {
    for(Player player : arena.getPlayers()) {
      VersionUtils.showPlayer(plugin, player, p);
    }
  }

  public static void resetPlayerAfterGame(Player player) {
    for(Player players : plugin.getServer().getOnlinePlayers()) {
      VersionUtils.showPlayer(plugin, players, player);
      VersionUtils.showPlayer(plugin, player, players);
    }
    VersionUtils.setGlowing(player, false);
    player.setGameMode(GameMode.SURVIVAL);
    player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
    player.setFlying(false);
    player.setAllowFlight(false);
    player.getInventory().clear();
    player.getInventory().setArmorContents(null);
    VersionUtils.setMaxHealth(player, 20);
    player.setHealth(VersionUtils.getMaxHealth(player));
    player.setFireTicks(0);
    player.setFoodLevel(20);
    if(plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INVENTORY_MANAGER_ENABLED)) {
      InventorySerializer.loadInventory(plugin, player);
    }
  }

  public static void bringDeathPlayersBack(Arena arena) {
    for(Player player : arena.getPlayers()) {
      if(arena.getPlayersLeft().contains(player)) {
        continue;
      }

      User user = plugin.getUserManager().getUser(player);
      if(!plugin.getConfigPreferences().getOption(ConfigPreferences.Option.INGAME_JOIN_RESPAWN) && user.isPermanentSpectator()) {
        continue;
      }

      VillagePlayerRespawnEvent event = new VillagePlayerRespawnEvent(player, arena);
      Bukkit.getPluginManager().callEvent(event);
      if(event.isCancelled()) {
        continue;
      }

      user.setSpectator(false);

      player.teleport(arena.getStartLocation());
      player.setFlying(false);
      player.setAllowFlight(false);
      //the default fly speed
      player.setFlySpeed(0.1f);
      player.setGameMode(GameMode.SURVIVAL);
      player.removePotionEffect(PotionEffectType.NIGHT_VISION);
      player.removePotionEffect(PotionEffectType.SPEED);
      player.getInventory().clear();
      ArenaUtils.showPlayer(player, arena);
      user.getKit().giveKitItems(player);
      player.updateInventory();
      player.sendMessage(plugin.getChatManager().colorMessage(Messages.BACK_IN_GAME));
    }
  }

  public static void removeSpawnedZombies(Arena arena) {
    boolean eachThree = arena.getZombies().size() > 70;
    int i = 0;
    for(Creature zombie : arena.getZombies()) {
      if(eachThree && (i % 3) == 0) {
        VersionUtils.sendParticles("LAVA", arena.getPlayers(), zombie.getLocation(), 20);
      }
      zombie.remove();
      i++;
    }
  }

  public static void arenaForceStart(Player player) {
    if(!Utils.hasPermission(player, "villagedefense.admin.forcestart")) {
      player.sendMessage(plugin.getChatManager().colorMessage(Messages.COMMANDS_NO_PERMISSION));
      return;
    }
    if(!Utils.checkIsInGameInstance(player)) {
      player.sendMessage(plugin.getChatManager().colorMessage(Messages.COMMANDS_NOT_PLAYING));
      return;
    }
    Arena arena = ArenaRegistry.getArena(player);
    if(arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == ArenaState.STARTING) {
      arena.setArenaState(ArenaState.STARTING);
      arena.setForceStart(true);
      arena.setTimer(0);
      plugin.getChatManager().broadcast(arena, Messages.ADMIN_MESSAGES_SET_STARTING_IN_TO_0);
    }
  }

}
