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

package plugily.projects.villagedefense.arena.villager.trait;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewCreatureUtils;
import plugily.projects.villagedefense.arena.villager.VillagerAiAnimations;
import plugily.projects.villagedefense.arena.villager.VillagerAiManager;
import plugily.projects.villagedefense.utils.NearbyUtils;

public class BraveTrait implements GenericTrait {

  private final Main plugin = JavaPlugin.getPlugin(Main.class);
  private final VillagerAiManager aiManager;

  public BraveTrait(VillagerAiManager aiManager) {
    this.aiManager = aiManager;
  }

  @Override
  public VillagerAiManager.Personality getPersonality() {
    return VillagerAiManager.Personality.BRAVE;
  }

  @Override
  public void onSocialize(Arena arena, Villager villager) {
  }

  @Override
  public void onRetreat(Arena arena, Villager villager) {
    double healthPercent = villager.getHealth() / villager.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
    if (healthPercent <= 0.5) {
      int homeIndex = villager.getMetadata(VillagerAiManager.VILLAGER_PERSONALITY_CHOSEN_HOME_ID).get(0).asInt();
      Location home = NearbyUtils.getRandomNearbyLocation(aiManager.getPlaces().get(VillagerAiManager.Place.VILLAGER_HOME_ZONE).get(homeIndex), 2);
      aiManager.doStartPathfinder(villager, home, (v, l) -> {
      });
      return;
    }
    new BukkitRunnable() {
      @Override
      public void run() {
        if (!arena.isFighting() || villager.isDead() || villager.hasMetadata("VD_BRAVE_RETREAT")) {
          this.cancel();
          return;
        }
        for (Entity entity : villager.getNearbyEntities(3, 3, 3)) {
          if (!NewCreatureUtils.isEnemy(entity)) {
            continue;
          }
          VillagerAiAnimations.makeEntityLookAt(villager, ((LivingEntity) entity).getEyeLocation());
          villager.setJumping(true);
          ((LivingEntity) entity).damage(2, villager);
          Bukkit.getScheduler().runTaskLater(plugin, () -> {
            villager.setJumping(false);
          }, 20);
          return;
        }
      }
    }.runTaskTimer(plugin, 0, 40);
  }
}
