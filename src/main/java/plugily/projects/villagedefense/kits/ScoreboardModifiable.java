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

package plugily.projects.villagedefense.kits;

import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.villagedefense.kits.utils.KitSpecifications;

import java.util.List;

public interface ScoreboardModifiable {

  static String renderAbilityCooldown(User user, String abilityId, String abilityName, int currentWave, KitSpecifications.GameTimeState gameState) {
    if (currentWave < gameState.getStartWave()) {
      return "&7" + abilityName + ": &c❌";
    }
    double cooldown = user.getCooldown(abilityId);
    if (cooldown <= 0.0) {
      return "&f" + abilityName + ": &a✔";
    } else {
      double runningCd = user.getCooldown(abilityId + "_running");
      if (runningCd > 0) {
        return "&f" + abilityName + ": &aActive (&6" + (int) runningCd + "s&a)";
      }
      return "&f" + abilityName + ": &6" + (int) cooldown + "s";
    }
  }

  List<String> getScoreboardLines(User user);

}
