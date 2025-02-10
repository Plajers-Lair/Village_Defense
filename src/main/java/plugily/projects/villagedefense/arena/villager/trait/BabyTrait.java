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

import org.bukkit.entity.Villager;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.villager.VillagerAiManager;

public class BabyTrait implements GenericTrait {

  @Override
  public VillagerAiManager.Personality getPersonality() {
    return VillagerAiManager.Personality.BABY;
  }

  @Override
  public void onSocialize(Arena arena, Villager villager) {
    //baby doesn't socialize
  }

  @Override
  public void onRetreat(Arena arena, Villager villager) {
    //baby doesn't retreat
  }

}
