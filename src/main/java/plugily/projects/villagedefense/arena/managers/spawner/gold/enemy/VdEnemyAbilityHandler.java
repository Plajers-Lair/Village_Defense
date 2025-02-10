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

package plugily.projects.villagedefense.arena.managers.spawner.gold.enemy;

import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import plugily.projects.villagedefense.Main;

public class VdEnemyAbilityHandler {

  public static boolean canCastAbility(Entity entity) {
    return entity.getMetadata("VD_ENEMY_ABILITY_COOLDOWN").isEmpty();
  }

  public static int increaseVariable(Entity entity, String key) {
    if (entity.getMetadata(key).isEmpty()) {
      entity.setMetadata(key, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(Main.class), 1));
      return 1;
    }
    int value = entity.getMetadata(key).get(0).asInt() + 1;
    entity.setMetadata(key, new FixedMetadataValue(JavaPlugin.getProvidingPlugin(Main.class), value));
    return value;
  }

  public static void resetVariable(Entity entity, String key) {
    entity.removeMetadata(key, JavaPlugin.getProvidingPlugin(Main.class));
  }

}
