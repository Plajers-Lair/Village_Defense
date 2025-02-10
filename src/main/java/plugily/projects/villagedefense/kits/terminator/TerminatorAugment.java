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

package plugily.projects.villagedefense.kits.terminator;

import org.bukkit.inventory.ItemStack;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.kits.utils.KitSpecifications;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public record TerminatorAugment(String id, ItemStack icon, String name, List<String> lore, AugmentType type, KitSpecifications.GameTimeState appliesFrom,
                                Function<Arena, Boolean> synergyApplies, Consumer<User> onApply) {

  public static final String AUGMENTS_COUNT_METADATA_KEY = "VD_TERMINATOR_AUGMENTS_COUNT";
  public static final String METADATA_KEY = "VD_TERMINATOR_AUGMENT_";
  /**
   * Augment is always applicable no matter the environment
   */
  public static final Function<Arena, Boolean> CONSTANT_SYNERGY = (arena) -> true;

  public String getMetadataKey() {
    return METADATA_KEY + id;
  }

  public enum AugmentType {
    BLADE, SYSTEM, SYNERGY
  }

}
