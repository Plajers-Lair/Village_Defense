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

package plugily.projects.villagedefense.arena.managers.spawner.gold;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.utils.ProtocolUtils;
import plugily.projects.villagedefense.utils.Utils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Created by Tom on 14/08/2014.
 */
public class DoorBreakListener extends BukkitRunnable {

  public static final String CREATURE_DOOR_BULLDOZER_METADATA = "VD_DOOR_BULLDOZER_BUFF";

  private static final String DESTROY_STATE_METADATA = "VD_DOOR_DESTROY_STATE";
  private final Main plugin;

  public DoorBreakListener(Main plugin) {
    this.plugin = plugin;
    runTaskTimer(plugin, 1, 30);
  }

  @Override
  public void run() {
    for(World world : plugin.getArenaRegistry().getArenaIngameWorlds()) {
      List<LivingEntity> entities = world.getLivingEntities()
        .stream()
        .filter(NewCreatureUtils::isEnemy)
        .collect(Collectors.toList());
      for(LivingEntity entity : entities) {
        for(Block block : plugin.getBukkitHelper().getNearbyBlocks(entity, 1)) {
          Material door = Utils.getCachedDoor(block);
          if(block.getType() != door) {
            continue;
          }
          if(entity.hasMetadata("VD_DOOR_BLOCK_BAN")) {
            Arena arena = findArenaForEntity(entity);
            Player player = Bukkit.getPlayer(UUID.fromString(entity.getMetadata("VD_DOOR_BLOCK_BAN_SOURCE").get(0).asString()));
            if(arena == null || player == null) {
              continue;
            }
            arena.getAssistHandler().doRegisterDebuffOnEnemy(player, (Creature) entity);
          }

          Location location = block.getLocation();
          VersionUtils.sendParticles("SMOKE_LARGE", null, location, 5, 0.1, 0.1, 0.1);
          VersionUtils.playSound(location, "ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR");
          int destroyState = getDoorDestroyState(block);
          int doubleAttackChance = 15;
          if(entity.hasMetadata(CREATURE_DOOR_BULLDOZER_METADATA)) {
            doubleAttackChance = 8;
          }
          if(ThreadLocalRandom.current().nextInt(20) >= doubleAttackChance) {
            destroyState++;
          }
          destroyState++;
          block.setMetadata(CREATURE_DOOR_BULLDOZER_METADATA, new FixedMetadataValue(plugin, destroyState));
          doPerformDoorDamage(block, door, destroyState);
          if(destroyState >= 10) {
            doPerformDoorBreak(block, door);
          }
          final int finalState = destroyState;
          Bukkit.getScheduler().runTaskLater(plugin, () -> {
            //block was already destroyed
            if(block.getType() == XMaterial.AIR.parseMaterial() || !block.hasMetadata(CREATURE_DOOR_BULLDOZER_METADATA)) {
              return;
            }
            //block wasn't touched for a while, remove the destruction
            if(block.getMetadata(CREATURE_DOOR_BULLDOZER_METADATA).get(0).asInt() == finalState) {
              doPerformDamageRemoval(block, door);
            }
          }, 20L * 5);
        }
      }
    }
  }

  private Arena findArenaForEntity(LivingEntity entity) {
    for(Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if(arena.getEnemies().contains(entity)) {
        return arena;
      }
    }
    return null;
  }

  private int getDoorDestroyState(Block block) {
    int destroyState;
    if(!block.hasMetadata(CREATURE_DOOR_BULLDOZER_METADATA)) {
      destroyState = 0;
      block.setMetadata(CREATURE_DOOR_BULLDOZER_METADATA, new FixedMetadataValue(plugin, destroyState));
    } else {
      destroyState = block.getMetadata(CREATURE_DOOR_BULLDOZER_METADATA).get(0).asInt();
    }
    return destroyState;
  }

  private void doPerformDoorDamage(Block block, Material door, int destroyState) {
    ProtocolUtils.sendBlockBreakAnimation(block, destroyState);

    Block relative = block.getRelative(BlockFace.UP);

    if(relative.getType() == door) {
      ProtocolUtils.sendBlockBreakAnimation(relative, destroyState);
    } else if((relative = block.getRelative(BlockFace.DOWN)).getType() == door) {
      ProtocolUtils.sendBlockBreakAnimation(relative, destroyState);
    }
  }

  private void doPerformDamageRemoval(Block block, Material door) {
    block.removeMetadata(CREATURE_DOOR_BULLDOZER_METADATA, plugin);
    ProtocolUtils.removeBlockBreakAnimation(block);

    Block relative = block.getRelative(BlockFace.UP);

    if(relative.getType() == door) {
      ProtocolUtils.removeBlockBreakAnimation(relative);
      relative.removeMetadata(CREATURE_DOOR_BULLDOZER_METADATA, plugin);
    } else if((relative = block.getRelative(BlockFace.DOWN)).getType() == door) {
      ProtocolUtils.removeBlockBreakAnimation(relative);
      relative.removeMetadata(CREATURE_DOOR_BULLDOZER_METADATA, plugin);
    }
  }

  private void doPerformDoorBreak(Block block, Material door) {
    Location location = block.getLocation();
    VersionUtils.sendParticles("SMOKE_LARGE", null, location, 15, 0.1, 0.1, 0.1);
    VersionUtils.sendParticles("EXPLOSION_HUGE", null, location, 1, 0.1, 0.1, 0.1);

    Block relative = block.getRelative(BlockFace.UP);

    //break order matters
    if(relative.getType() == door) {
      block.setType(Material.AIR);
      relative.setType(Material.AIR);
    } else if((relative = block.getRelative(BlockFace.DOWN)).getType() == door) {
      relative.setType(Material.AIR);
      block.setType(Material.AIR);
    }

    VersionUtils.playSound(location, "ENTITY_ZOMBIE_BREAK_WOODEN_DOOR");
  }

}
