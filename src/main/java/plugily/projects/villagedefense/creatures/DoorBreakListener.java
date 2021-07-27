/*
 * Village Defense - Protect villagers from hordes of zombies
 * Copyright (C) 2021  Plugily Projects - maintained by 2Wild4You, Tigerpanzer_02 and contributors
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

package plugily.projects.villagedefense.creatures;

import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.commonsbox.minecraft.compat.VersionUtils;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.ArenaRegistry;
import plugily.projects.villagedefense.utils.Utils;

/**
 * Created by Tom on 14/08/2014.
 */
public class DoorBreakListener extends BukkitRunnable {

  public DoorBreakListener(Main plugin) {
    runTaskTimer(plugin, 1, 20);
  }

  @Override
  public void run() {
    for(World world : ArenaRegistry.getArenaIngameWorlds()) {
      for(LivingEntity entity : world.getLivingEntities()) {
        if(entity.getType() != EntityType.ZOMBIE) {
          continue;
        }

        for(Block block : Utils.getNearbyBlocks(entity, 1)) {
          Material door = Utils.getCachedDoor(block);

          if(block.getType() != door) {
            continue;
          }

          org.bukkit.Location blockLoc = block.getLocation();

          VersionUtils.sendParticles("SMOKE_LARGE", null, blockLoc, 5, 0.1,0.1,0.1);
          Utils.playSound(blockLoc, "ENTITY_ZOMBIE_ATTACK_DOOR_WOOD", "ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR");

          if(ThreadLocalRandom.current().nextInt(20) == 5) {
            VersionUtils.sendParticles("SMOKE_LARGE", null, blockLoc, 15, 0.1,0.1,0.1);
            VersionUtils.sendParticles("EXPLOSION_HUGE", null, blockLoc, 1, 0.1,0.1,0.1);

            Block b = block.getRelative(BlockFace.UP);

            if(b.getType() == door) {
              b.setType(Material.AIR);
            } else if((b = block.getRelative(BlockFace.DOWN)).getType() == door) {
              b.setType(Material.AIR);
            }

            block.setType(Material.AIR);
            Utils.playSound(blockLoc, "ENTITY_ZOMBIE_BREAK_DOOR_WOOD", "ENTITY_ZOMBIE_BREAK_WOODEN_DOOR");
          }
        }
      }
    }
  }

}
