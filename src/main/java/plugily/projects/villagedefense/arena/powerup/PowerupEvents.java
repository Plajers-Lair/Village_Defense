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

package plugily.projects.villagedefense.arena.powerup;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.ArenaMetadata;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewCreatureUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Plajer
 * <p>
 * Created at 06.10.2023
 */
public class PowerupEvents implements Listener {

  private final List<Powerup> powerups = new ArrayList<>();
  private final Random random = new Random();
  private Main plugin;

  public PowerupEvents(Main plugin) {
    this.plugin = plugin;
    powerups.add(new IronDeliveryPowerup(plugin));
    powerups.add(new MoneyPouchPowerup(plugin));
    powerups.add(new RandomPowerup(plugin));
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onPowerupDrop(EntityDeathEvent event) {
    if (!NewCreatureUtils.isEnemy(event.getEntity())) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getEnemies().contains(event.getEntity())) {
        continue;
      }
      if (ThreadLocalRandom.current().nextDouble(0.0, 100.0) <= 1.5) {
        int powerupsDropped = arena.getMetadata(ArenaMetadata.POWERUPS_WAVE_COUNT, 0);
        if (powerupsDropped >= 4) {
          return;
        }
        arena.setMetadata(ArenaMetadata.POWERUPS_WAVE_COUNT, powerupsDropped + 1);
        arena.setMetadata(ArenaMetadata.LAST_POWERUP_DROP_MILLIS, System.currentTimeMillis());
        while (true) {
          Powerup powerup = powerups.get(random.nextInt(powerups.size()));
          if (!powerup.canSpawn(arena)) {
            continue;
          }
          powerup.spawn(arena, event.getEntity().getLocation().clone().add(0, 0.1, 0));
          return;
        }
      }
    }
  }


}
