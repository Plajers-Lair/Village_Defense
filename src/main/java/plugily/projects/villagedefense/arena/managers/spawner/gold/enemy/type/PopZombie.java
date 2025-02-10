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

package plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.type;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creature;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewEnemySpawnerManager;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.VDEnemy;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.VDEnemyBuilder;

import java.util.concurrent.ThreadLocalRandom;

public class PopZombie implements RegistrableZombie {

  private final Main plugin = JavaPlugin.getPlugin(Main.class);

  @Override
  public VDEnemy getEnemy() {
    return new VDEnemyBuilder("VD_POP_ZOMBIE")
      .withName("Pop Zombie")
      .withWaveMinimum(0)
      .withWaveMaximum(50)
      .withScalingHealth(wave -> 25.0)
      .onSpawn(creature -> {
        creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
      })
      .onDeath((event, arena) -> {
        VersionUtils.sendParticles("LAVA", arena.getPlayers(), event.getEntity().getLocation(), 20);
        NewEnemySpawnerManager spawnerManager = arena.getNewEnemySpawnerManager();
        VDEnemy baby = plugin.getNewEnemiesRegistry().getEnemyById("VD_BABY_ZOMBIE");
        pushEntity(spawnerManager.doSpawnEnemyOutsideManagement(baby, event.getEntity().getLocation()));
        pushEntity(spawnerManager.doSpawnEnemyOutsideManagement(baby, event.getEntity().getLocation()));
      }).build();
  }

  private void pushEntity(Creature creature) {
    double upwardForce = 0.5;
    double sideForce = 0.3;

    int direction = ThreadLocalRandom.current().nextInt(4);
    double x = 0, z = 0;

    switch (direction) {
      case 0 -> x = sideForce;
      case 1 -> x = -sideForce;
      case 2 -> z = sideForce;
      case 3 -> z = -sideForce;
    }

    Vector velocity = new Vector(x, upwardForce, z);
    creature.setVelocity(velocity);
  }

}
