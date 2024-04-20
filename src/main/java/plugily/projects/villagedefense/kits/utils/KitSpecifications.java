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

package plugily.projects.villagedefense.kits.utils;

import plugily.projects.villagedefense.arena.Arena;

/**
 * @author Plajer
 * <p>
 * Created at 01.09.2022
 */
public class KitSpecifications {

  public static final double LETHAL_DAMAGE = 10000000.0;

  public static GameTimeState getTimeState(Arena arena) {
    if(arena.getWave() <= 15) {
      return GameTimeState.EARLY;
    } else if(arena.getWave() <= 30) {
      return GameTimeState.MID;
    } else {
      return GameTimeState.LATE;
    }
  }

  public enum GameTimeState {
    LATE(31), MID(16), EARLY(0);

    private int startWave;

    GameTimeState(int startWave) {
      this.startWave = startWave;
    }

    public int getStartWave() {
      return startWave;
    }
  }

}
