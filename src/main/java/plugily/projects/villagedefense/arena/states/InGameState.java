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

package plugily.projects.villagedefense.arena.states;

import plugily.projects.minigamesbox.classic.arena.ArenaState;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.arena.states.PluginInGameState;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.handlers.language.TitleBuilder;
import plugily.projects.minigamesbox.classic.utils.helper.SoundHelper;
import plugily.projects.villagedefense.arena.Arena;

/**
 * @author Plajer
 * <p>
 * Created at 03.06.2019
 */
public class InGameState extends PluginInGameState {

  @Override
  public void handleCall(PluginArena arena) {
    super.handleCall(arena);
    Arena pluginArena = (Arena) getPlugin().getArenaRegistry().getArena(arena.getId());
    if(pluginArena == null) {
      return;
    }
    pluginArena.getNewEnemySpawnerManager().doGlitchCheck();

    if(pluginArena.getVillagers().isEmpty() || arena.getPlayersLeft().isEmpty() && arena.getArenaState() != ArenaState.ENDING) {
      getPlugin().getArenaManager().stopGame(false, arena);
      return;
    }
    int zombiesLeft = pluginArena.getZombiesLeft();
    getPlugin().getDebugger().debug("Arena {0} Zombies to spawn {1} Zombies left {2} Fighting {3}", arena.getId(), arena.getArenaOption("ZOMBIES_TO_SPAWN"), zombiesLeft, pluginArena.isFighting());
    if(pluginArena.isFighting()) {
      if(zombiesLeft <= 0) {
        pluginArena.setFighting(false);
        pluginArena.getPlugin().getArenaManager().endWave(pluginArena);
      } else {
        pluginArena.getNewEnemySpawnerManager().doSpawnEnemies();
        if (!pluginArena.getNewEnemySpawnerManager().hasSpawningStarted()) {
          setArenaTimer(500);
        }
      }
      int zombiesLeftFrom = getPlugin().getConfig().getInt("Glowing-Status.Creatures-Left");
      int startingWave;
      if (zombiesLeftFrom > 0 && zombiesLeft <= zombiesLeftFrom
        && (startingWave = getPlugin().getConfig().getInt("Glowing-Status.Starting-Wave")) > 0
        && pluginArena.getWave() >= startingWave) {
        for (org.bukkit.entity.Creature remaining : pluginArena.getEnemies()) {
          if (!remaining.isGlowing()) { // To avoid setting glowing property every time
            remaining.setGlowing(true);
          }
        }
      }
      if(arena.getTimer() == 0) {
        pluginArena.getMapRestorerManager().clearEnemiesFromArena();
        if(pluginArena.getZombiesLeft() > 0) {
          new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_STUCK_ZOMBIES").asKey().arena(arena).sendArena();
        }
        arena.setArenaOption("ZOMBIES_TO_SPAWN", 0);
      }
      if(arena.getArenaOption("ZOMBIES_TO_SPAWN") < 0) {
        arena.setArenaOption("ZOMBIES_TO_SPAWN", 0);
      }
      if (arena.getTimer() % 4 == 0) {
        pluginArena.getVillagerAiManager().doRetreatVillagers();
      }
    } else if(arena.getTimer() <= 3) {
      SoundHelper.playArenaCountdown(arena);
      if(arena.getTimer() <= 0) {
        pluginArena.setFighting(true);
        pluginArena.getPlugin().getArenaManager().startWave(pluginArena);
      } else if(arena.getTimer() >= 1) {
        new TitleBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_TITLE_STARTING_" + arena.getTimer()).asKey().arena(arena).sendArena();
      }
    }
  }

}
