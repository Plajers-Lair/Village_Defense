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

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import plugily.projects.villagedefense.arena.Arena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
@Setter
public class VDEnemy {

  private String id;
  private String name;
  private EntityType type = EntityType.ZOMBIE;
  private EntityType targetPriority = null;
  private boolean persistentTargeting = false;
  private int waveMinimum;
  private int waveMaximum;
  private Map<ArmorPiece.PiecePart, List<ArmorPiece>> waveArmorPieces = new HashMap<>();
  private List<WeaponPiece> weaponParts = new ArrayList<>();

  private boolean baby = false;
  private boolean impostorHead = false;

  private Function<Arena, Boolean> canSpawn = arena -> true;
  private Consumer<Creature> onSpawn;
  private Function<Integer, Double> healthFunction = wave -> 21.0;
  private OnDeathFunction onDeath = (event, arena) -> {
  };
  private OnDamageByFunction onDamageBy = (event, arena) -> {
  };
  private OnDamagingEntityFunction onDamaging = (event, arena) -> {
  };
  private OnAbilityTickFunction onAbilityTick = (creature, arena) -> {
  };

  @FunctionalInterface
  public interface OnDeathFunction {
    void onDeath(EntityDeathEvent event, Arena arena);
  }

  @FunctionalInterface
  public interface OnDamageByFunction {
    void onBeingDamaged(EntityDamageByEntityEvent event, Arena arena);
  }

  @FunctionalInterface
  public interface OnDamagingEntityFunction {
    void onDamaging(EntityDamageByEntityEvent event, Arena arena);
  }

  @FunctionalInterface
  public interface OnAbilityTickFunction {
    void onAbilityTick(Creature creature, Arena arena);
  }

  @Data
  @Accessors(chain = true)
  public static class WeaponPiece {
    private int waveMinimum;
    private int waveMaximum;
    private ItemStack itemStack;
  }

}
