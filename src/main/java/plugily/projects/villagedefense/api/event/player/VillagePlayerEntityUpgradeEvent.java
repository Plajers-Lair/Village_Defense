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

package plugily.projects.villagedefense.api.event.player;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import plugily.projects.minigamesbox.classic.api.event.PlugilyEvent;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.handlers.upgrade.EntityUpgrade;

/**
 * @author Plajer
 * @since 1.0.0
 * <p>
 * Called when player upgrades an entity.
 */
@Deprecated
public class VillagePlayerEntityUpgradeEvent extends PlugilyEvent {

  private static final HandlerList HANDLERS = new HandlerList();
  private final Entity entity;
  private final Player player;
  private final EntityUpgrade appliedUpgrade;
  private final int tier;

  public VillagePlayerEntityUpgradeEvent(Arena eventArena, Entity entity, Player player, EntityUpgrade appliedUpgrade, int tier) {
    super(eventArena);
    this.entity = entity;
    this.player = player;
    this.appliedUpgrade = appliedUpgrade;
    this.tier = tier;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

  public Entity getEntity() {
    return entity;
  }

  /**
   * @return upgrade that was applied to entity
   */
  public EntityUpgrade getAppliedUpgrade() {
    return appliedUpgrade;
  }

  /**
   * @return upgrade tier
   */
  public int getTier() {
    return tier;
  }

  public Player getPlayer() {
    return player;
  }

}
