
/*
 * Village Defense - Protect villagers from hordes of zombies
 * Copyright (c) 2024  Plugily Projects - maintained by Tigerpanzer_02 and contributors
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

package plugily.projects.villagedefense.arena.managers.spawner;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.jetbrains.annotations.Nullable;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.creatures.CreatureUtils;

import java.util.Random;

/**
 * The interface for simple enemy spawner
 */
public interface SimpleEnemySpawner extends EnemySpawner {
  /**
   * Get the minimum wave to spawn the enemies
   *
   * @return the wave
   */
  default int getMinWave() {
    return 1;
  }

  /**
   * Get the maximum wave to spawn the enemies (stop spawning when exceeding this value)
   *
   * @return the wave
   */
  default int getMaxWave() {
    return -1;
  }

  /**
   * Can the enemies be applied some holiday effects?
   *
   * @return true if they can
   */
  default boolean canApplyHolidayEffect() {
    return false;
  }

  /**
   * Can the enemies be applied arena attributes?
   *
   * @return true if they can
   */
  default boolean canApplyAttributes() {
    return true;
  }

  /**
   * How often the enemies will be spawned? Amount between 0.0 and 1.0
   *
   * @param arena       the arena
   * @param wave        the current wave
   * @param phase       the current phase
   * @param spawnAmount the raw amount that the arena suggests
   * @return the spawn rate in double
   */
  double getSpawnRate(Arena arena, int wave, int phase, int spawnAmount);

  /**
   * Get the final amount of enemies to spawn, after some workaround
   *
   * @param arena       the arena
   * @param wave        the current wave
   * @param phase       the current phase
   * @param spawnAmount the raw amount that the arena suggests
   * @return the final amount
   */
  int getFinalAmount(Arena arena, int wave, int phase, int spawnAmount);

  /**
   * Check if the enemies can be spawned on this phase
   *
   * @param arena       the arena
   * @param wave        the current wave
   * @param phase       the current phase
   * @param spawnAmount the raw amount that the arena suggests
   * @return true if they can
   */
  boolean checkPhase(Arena arena, int wave, int phase, int spawnAmount);

  /**
   * Spawn the enemy at the location
   *
   * @param location the location
   * @return the spawned enemy
   */
  @Nullable
  Creature spawn(Location location);

  /**
   * Get the weight of the enemy in the arena.
   * Basically mean this enemy is worth how many normal enemies in the arena.
   *
   * @param arena       the arena
   * @param wave        the current wave
   * @param phase       the current phase
   * @param spawnAmount the raw amount that the arena suggests
   * @return the weight of the enemy
   */
  default int getSpawnWeight(Arena arena, int wave, int phase, int spawnAmount) {
    return 1;
  }

  /**
   * Spawn the enemy at the location of the arena.
   *
   * @param location the location
   * @param arena    the arena
   */
  default void spawn(Location location, Arena arena) {
    Creature creature = spawn(location);
    if(creature == null) {
      return;
    }
    //2 seconds of invulnerability to avoid suffocation problem
    creature.setMaximumNoDamageTicks(45);
    creature.setNoDamageTicks(40);
    if(canApplyAttributes()) {
      CreatureUtils.applyAttributes(creature, arena);
    }
    if(canApplyHolidayEffect()) {
      arena.getPlugin().getHolidayManager().applyHolidayCreatureEffects(creature);
    }
    arena.getEnemies().add(creature);
  }

  //TODO Simplify creature spawn reduce to one method e.g. spawn; add weight to creatures configurable!
  @Override
  default void spawn(Random random, Arena arena, int spawn) {
    int wave = arena.getWave();
    int phase = arena.getArenaOption("ZOMBIE_SPAWN_COUNTER");
    arena.getPlugin().getDebugger().debug("Current Wave: " + wave + " Current Phase: " + phase + " Current spawn: " + spawn + " CHECK PHASE: " + checkPhase(arena, wave, phase, spawn));
    if(!checkPhase(arena, wave, phase, spawn)) {
      return;
    }


    int maxWave = getMaxWave();
    arena.getPlugin().getDebugger().debug("Current Wave: " + wave + " Max wave: " + maxWave + " CHECK WAVE: " + (wave < getMinWave() || (maxWave > 0 && wave > maxWave)));

    if(wave < getMinWave() || (maxWave > 0 && wave > maxWave)) {
      return;
    }
    int spawnAmount = getFinalAmount(arena, wave, phase, spawn);
    double spawnRate = getSpawnRate(arena, wave, phase, spawn);
    int weight = getSpawnWeight(arena, wave, phase, spawn);
    arena.getPlugin().getDebugger().debug("Current Wave: " + wave + " Current Spawn amount: " + spawnAmount + " Current spawnRate: " + spawnRate + " Current Spawn Weight: " + weight);

    for(int i = 0; i < spawnAmount; i++) {
      int zombiesToSpawn = arena.getArenaOption("ZOMBIES_TO_SPAWN");
      arena.getPlugin().getDebugger().debug("Current Wave: " + wave + " Current Spawn amount: " + spawnAmount + " Current i: " + i + " CHECK SPAWN: " + (zombiesToSpawn >= weight && spawnRate != 0 && (spawnRate == 1 || random.nextDouble() < spawnRate)));
      if(zombiesToSpawn >= weight && spawnRate != 0 && (spawnRate == 1 || random.nextDouble() < spawnRate)) {
        Location location = arena.getRandomZombieSpawnLocation(random);
        spawn(location, arena);
        arena.setArenaOption("ZOMBIES_TO_SPAWN", zombiesToSpawn - weight);
      }
    }
  }
}
