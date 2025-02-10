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

package plugily.projects.villagedefense.boot;

import plugily.projects.minigamesbox.classic.api.StatisticType;
import plugily.projects.minigamesbox.classic.api.StatsStorage;
import plugily.projects.minigamesbox.classic.arena.options.ArenaOption;
import plugily.projects.minigamesbox.classic.arena.options.ArenaOptionManager;
import plugily.projects.minigamesbox.classic.handlers.items.SpecialItemManager;
import plugily.projects.minigamesbox.classic.handlers.permissions.Permission;
import plugily.projects.minigamesbox.classic.handlers.permissions.PermissionCategory;
import plugily.projects.minigamesbox.classic.handlers.permissions.PermissionsManager;
import plugily.projects.minigamesbox.classic.handlers.reward.RewardType;
import plugily.projects.minigamesbox.classic.handlers.reward.RewardsFactory;
import plugily.projects.minigamesbox.classic.preferences.ConfigOption;
import plugily.projects.minigamesbox.classic.preferences.ConfigPreferences;
import plugily.projects.villagedefense.Main;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 15.10.2022
 */
public class AdditionalValueInitializer {

  private final Main plugin;

  public AdditionalValueInitializer(Main plugin) {
    this.plugin = plugin;
    registerConfigOptions();
    registerStatistics();
    registerPermission();
    registerRewards();
    registerSpecialItems();
    registerArenaOptions();
  }

  private void registerConfigOptions() {
    getConfigPreferences().registerOption("UPGRADES", new ConfigOption("Entity-Upgrades", true));
    getConfigPreferences().registerOption("RESPAWN_AFTER_WAVE", new ConfigOption("Respawn.After-Wave", true));
    getConfigPreferences().registerOption("RESPAWN_IN_GAME_JOIN", new ConfigOption("Respawn.In-Game-Join", true));
    getConfigPreferences().registerOption("LIMIT_WAVE_UNLIMITED", new ConfigOption("Limit.Wave.Unlimited", true));
    getConfigPreferences().registerOption("LIMIT_ENTITY_BUY_AFTER_DEATH", new ConfigOption("Limit.Wave.Entity-Buy-After-Death", true));
  }

  private void registerStatistics() {
    getStatsStorage().registerStatistic("KILLS", new StatisticType("kills", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("DEATHS", new StatisticType("deaths", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("HIGHEST_WAVE", new StatisticType("highest_wave", true, "int(11) NOT NULL DEFAULT '0'"));
    getStatsStorage().registerStatistic("ORBS", new StatisticType("orbs", false, "int(11) NOT NULL DEFAULT '0'"));
  }

  private void registerPermission() {
    getPermissionsManager().registerPermissionCategory("ORBS_BOOSTER", new PermissionCategory("Orbs-Boost", null));
    getPermissionsManager().registerPermission("KIT_PREMIUM_UNLOCK", new Permission("Basic.Premium-Kits", "villagedefense.kits.premium"));
  }

  private void registerRewards() {
    getRewardsHandler().registerRewardType("START_WAVE", new RewardType("start-wave"));
    getRewardsHandler().registerRewardType("END_WAVE", new RewardType("end-wave"));
    getRewardsHandler().registerRewardType("ZOMBIE_KILL", new RewardType("zombie-kill"));
    getRewardsHandler().registerRewardType("VILLAGER_DEATH", new RewardType("villager-death"));
    getRewardsHandler().registerRewardType("PLAYER_DEATH", new RewardType("player-death"));
  }

  private void registerSpecialItems() {
    getSpecialItemManager().registerSpecialItem("KIT_SELECTOR_MENU", "Kit-Menu");
  }

  private void registerArenaOptions() {
    /**
     * Current arena wave.
     */
    getArenaOptionManager().registerArenaOption("WAVE", new ArenaOption("null", 1));
    /**
     * Current bonus hearts level based on rotten fleshes
     * donated by players to secret well
     */
    getArenaOptionManager().registerArenaOption("ROTTEN_FLESH_LEVEL", new ArenaOption("null", 0));
    /**
     * Amount of rotten fleshes donated to secret well
     */
    getArenaOptionManager().registerArenaOption("ROTTEN_FLESH_AMOUNT", new ArenaOption("null", 0));
    /**
     * Total amount of orbs (in game currency) spent by all players
     * in that arena in one game
     */
    getArenaOptionManager().registerArenaOption("TOTAL_ORBS_SPENT", new ArenaOption("null", 0));
    /**
     * Total amount of zombies killed by all players
     * in that arena in one game
     */
    getArenaOptionManager().registerArenaOption("TOTAL_KILLED_ZOMBIES", new ArenaOption("null", 0));
    /**
     * Amount of zombies that game still need to spawn before
     * ending current wave and start another
     */
    getArenaOptionManager().registerArenaOption("ZOMBIES_TO_SPAWN", new ArenaOption("null", 0));
    /**
     * Value used to check all alive zombies if they weren't glitched on map
     * i.e. still stay near spawn position but cannot move.
     * <p>
     * Arena itself checks this value each time it reaches 60 (so each 60 seconds).
     */
    getArenaOptionManager().registerArenaOption("ZOMBIE_GLITCH_CHECKER", new ArenaOption("null", 0));
    /**
     * Value that describes progress of zombies spawning in wave in arena.
     * <p>
     * It's counting up to 20 and resets to 0.
     * If value is equal 5 or 15 and wave is enough high special
     * zombie units will be spawned in addition to standard ones.
     *
     * @deprecated subject to removal
     */
    getArenaOptionManager().registerArenaOption("ZOMBIE_SPAWN_COUNTER", new ArenaOption("null", 0));
  }

  private ConfigPreferences getConfigPreferences() {
    return plugin.getConfigPreferences();
  }

  private StatsStorage getStatsStorage() {
    return plugin.getStatsStorage();
  }

  private PermissionsManager getPermissionsManager() {
    return plugin.getPermissionsManager();
  }

  private RewardsFactory getRewardsHandler() {
    return plugin.getRewardsHandler();
  }

  private SpecialItemManager getSpecialItemManager() {
    return plugin.getSpecialItemManager();
  }

  private ArenaOptionManager getArenaOptionManager() {
    return plugin.getArenaOptionManager();
  }

}
