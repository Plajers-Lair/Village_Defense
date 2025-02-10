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

package plugily.projects.villagedefense.arena.managers.maprestorer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import plugily.projects.minigamesbox.classic.arena.managers.PluginMapRestorerManager;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.managers.spawner.gold.DoorBreakListener;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 13.01.2021
 */
@SuppressWarnings("deprecation")
public class MapRestorerManager extends PluginMapRestorerManager {

  private final List<Location> doorBlocks = new ArrayList<>();
  private final Arena arena;

  public MapRestorerManager(Arena arena) {
    super(arena);
    this.arena = arena;
  }

  public final void addDoor(Location location) {
    doorBlocks.add(location);
  }

  public final List<Location> getGameDoorLocations() {
    return doorBlocks;
  }

  @Override
  public void fullyRestoreArena() {
    super.fullyRestoreArena();
    arena.setWave(1);
    restoreDoors();
    clearEnemiesFromArena();
    clearGolemsFromArena();
    clearVillagersFromArena();
    clearWolvesFromArena();
    clearDroppedEntities();
  }

  public final void clearEnemiesFromArena() {
    arena.getEnemies().forEach(org.bukkit.entity.Creature::remove);
    arena.getEnemies().clear();
  }

  public final void clearDroppedEntities() {
    for (Entity entity : arena.getPlugin().getBukkitHelper().getNearbyEntities(arena.getStartLocation(), 200)) {
      if (entity.getType() == EntityType.EXPERIENCE_ORB || entity.getType() == EntityType.DROPPED_ITEM) {
        entity.remove();
      }
    }
  }

  public final void clearGolemsFromArena() {
    arena.getIronGolems().forEach(org.bukkit.entity.IronGolem::remove);
    arena.getIronGolems().clear();
  }

  public final void clearVillagersFromArena() {
    arena.getVillagers().forEach(org.bukkit.entity.Villager::remove);
    arena.getVillagers().clear();
  }

  public final void clearWolvesFromArena() {
    arena.getWolves().forEach(org.bukkit.entity.Wolf::remove);
    arena.getWolves().clear();
  }

  public void restoreDoors() {
    int i = 0;
    for (Location location : doorBlocks) {
      Block block = location.getBlock();
      try {
        if (block.getType() != XMaterial.AIR.parseMaterial()) {
          i++;
          continue;
        }
        Block relative = block.getRelative(BlockFace.DOWN).getLocation().getBlock();
        boolean isAirBelow = relative.getType().equals(XMaterial.AIR.parseMaterial());
        boolean relativeTopHalf = false;
        if (!isAirBelow) {
          relative = block.getRelative(BlockFace.UP).getLocation().getBlock();
          relativeTopHalf = true;
        }
        if (relativeTopHalf) {
          restoreDoor(relative, block);
        } else {
          restoreDoor(block, relative);
        }
      } catch (Exception ex) {
        arena.getPlugin().getDebugger().debug(Level.WARNING, "Door has failed to load for arena {0} message {1} type {2} skipping!", arena.getId(), ex.getMessage(), ex.getCause());
      }
    }
    if (i != doorBlocks.size()) {
      arena.getPlugin().getDebugger().debug(Level.WARNING, "Failed to load doors for {0}! Expected {1} got {2}", arena.getId(), doorBlocks.size(), i);
    }
  }

  public void restoreDoor(Block top, Block bottom) {
    top.setType(Material.OAK_DOOR, false);
    bottom.setType(Material.OAK_DOOR, false);

    org.bukkit.block.data.type.Door d2 = (org.bukkit.block.data.type.Door) top.getBlockData();
    org.bukkit.block.data.type.Door d1 = (org.bukkit.block.data.type.Door) bottom.getBlockData();

    d2.setHalf(org.bukkit.block.data.Bisected.Half.TOP);
    d1.setHalf(org.bukkit.block.data.Bisected.Half.BOTTOM);

    d2.setFacing(top.getRelative(BlockFace.WEST).getType() == Material.AIR ? BlockFace.EAST : BlockFace.NORTH);
    d1.setFacing(top.getRelative(BlockFace.WEST).getType() == Material.AIR ? BlockFace.EAST : BlockFace.NORTH);

    top.setBlockData(d2);
    bottom.setBlockData(d1);

    top.removeMetadata(DoorBreakListener.CREATURE_DOOR_BULLDOZER_METADATA, arena.getPlugin());
    bottom.removeMetadata(DoorBreakListener.CREATURE_DOOR_BULLDOZER_METADATA, arena.getPlugin());
  }

}
