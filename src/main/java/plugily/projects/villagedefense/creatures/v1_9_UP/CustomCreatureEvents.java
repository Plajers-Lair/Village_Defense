
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

package plugily.projects.villagedefense.creatures.v1_9_UP;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.Nullable;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.managers.spawner.EnemySpawner;
import plugily.projects.villagedefense.creatures.CreatureUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 15.01.2022
 */
public class CustomCreatureEvents implements Listener {

  private final Main plugin;

  public CustomCreatureEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }


  //todo improve the code structure
  //todo test pets giving rewards to owner (wolf/golem)
  @EventHandler
  public void onCreatureDeathEvent(EntityDeathEvent event) {
    LivingEntity entity = event.getEntity();
    if(!(entity instanceof Creature)) {
      return;
    }
    for(Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if(entity instanceof IronGolem || entity instanceof Wolf || entity instanceof Villager) {
        if(arena.getIronGolems().contains(entity) || arena.getWolves().contains(entity) || arena.getVillagers().contains(entity)) {
          Optional<CustomRideableCreature> customRideableCreatureOptional = plugin.getEnemySpawnerRegistry().getRideableCreatureByName(CustomRideableCreature.RideableType.valueOf(entity.getType().name().toUpperCase()));
          if(!customRideableCreatureOptional.isPresent()) {
            continue;
          }
          ItemStack itemStack = customRideableCreatureOptional.get().getDropItem();
          event.getDrops().add(itemStack);
        }
      } else if(arena.getEnemies().contains(entity)) {
        CustomCreature customCreature = arena.getCreatureTargetManager().getCustomCreatureFromCreature((Creature) entity);
        if(customCreature == null) {
          continue;
        }
        arena.removeEnemy((Creature) entity);
        arena.changeArenaOptionBy("TOTAL_KILLED_ZOMBIES", 1);

        Player killer = entity.getKiller();
        if(killer == null) {
          killer = performKillerDetection(event);
        }
        applyKillMetadata(event);
        Arena killerArena = plugin.getArenaRegistry().getArena(killer);

        if(killerArena != null) {
          plugin.getUserManager().addStat(killer, plugin.getStatsStorage().getStatisticType("KILLS"));
          plugin.getUserManager().addExperience(killer, 2 * arena.getArenaOption("ZOMBIE_DIFFICULTY_MULTIPLIER"));
          plugin.getRewardsHandler().performReward(killer, plugin.getRewardsHandler().getRewardType("ZOMBIE_KILL"));
        }

        event.setDroppedExp(0);
        filterDrops(event, customCreature, killer);
        if(killer != null) {
          User user = plugin.getUserManager().getUser(killer);
          if(user == null || !user.getArena().equals(arena)) {
            continue;
          }
          Map<Player, Double> contribution = arena.getAssistHandler().doDistributeAssistRewards(killer, (Creature) entity);
          for(Map.Entry<Player, Double> entry : contribution.entrySet()) {
            double share = entry.getValue() / 100.0;
            double multiplier = Math.log(arena.getArenaOption("ZOMBIE_DIFFICULTY_MULTIPLIER"));
            if(multiplier < 1.0) {
              multiplier = 1.0;
            }
            int amount = (int) Math.ceil(customCreature.getExpDrop() * share * 1.6 * multiplier);
            int orbsBoost = plugin.getPermissionsManager().getPermissionCategoryValue("ORBS_BOOSTER", entry.getKey());
            amount += (amount * (orbsBoost / 100));
            User targetUser = plugin.getUserManager().getUser(entry.getKey());
            targetUser.adjustStatistic(plugin.getStatsStorage().getStatisticType("ORBS"), amount);
          }
        }
      }
    }
  }

  @Nullable
  private Player performKillerDetection(EntityDeathEvent event) {
    EntityDamageEvent cause = event.getEntity().getLastDamageCause();
    if(!(cause instanceof EntityDamageByEntityEvent)) {
      return null;
    }
    Entity entity = ((EntityDamageByEntityEvent) cause).getDamager();
    if(entity instanceof Player) {
      return (Player) entity;
    } else if(entity instanceof Wolf || entity instanceof IronGolem) {
      if(!entity.hasMetadata("VD_OWNER_UUID")) {
        return null;
      }
      UUID uuid = UUID.fromString(entity.getMetadata("VD_OWNER_UUID").get(0).asString());
      return Bukkit.getServer().getPlayer(uuid);
    }
    return null;
  }

  private void applyKillMetadata(EntityDeathEvent event) {
    EntityDamageEvent cause = event.getEntity().getLastDamageCause();
    if (!(cause instanceof EntityDamageByEntityEvent)) {
      return;
    }
    Entity entity = ((EntityDamageByEntityEvent) cause).getDamager();
    if (entity instanceof Wolf || entity instanceof IronGolem) {
      if (!entity.hasMetadata("VD_OWNER_UUID")) {
        return;
      }
      if (!entity.hasMetadata("VD_ENTITY_KILLS")) {
        entity.setMetadata("VD_ENTITY_KILLS", new FixedMetadataValue(plugin, 1));
      } else {
        int kills = entity.getMetadata("VD_ENTITY_KILLS").get(0).asInt();
        entity.removeMetadata("VD_ENTITY_KILLS", plugin);
        entity.setMetadata("VD_ENTITY_KILLS", new FixedMetadataValue(plugin, kills + 1));
      }
    }
  }

  private void filterDrops(EntityDeathEvent event, CustomCreature creature, Player player) {
    List<ItemStack> filtered = new ArrayList<>();
    for (ItemStack itemStack : event.getDrops()) {
      if (itemStack == null || !XMaterial.ROTTEN_FLESH.isSimilar(itemStack)) {
        continue;
      }
      itemStack.setAmount(1);
      filtered.add(itemStack);
    }
    event.getDrops().clear();
    if(filtered.isEmpty() || player == null) {
      return;
    }
    User user = plugin.getUserManager().getUser(player);
    if(user.isSpectator()) {
      return;
    }
    player.getInventory().addItem(filtered.toArray(new ItemStack[]{}));
  }

  @EventHandler
  public void onExplosiveHit(EntityDamageByEntityEvent event) {
    if(!(event.getDamager() instanceof Creature)) {
      return;
    }
    for(Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if(!arena.isFighting()) {
        continue;
      }
      CustomCreature customCreature = getCustomCreatureFromCreature(arena, (Creature) event.getDamager());
      if(customCreature == null) {
        return;
      }
      if(!customCreature.isExplodeTarget()) {
        return;
      }
      CustomCreature.PriorityTarget priorityTarget = customCreature.getPriorityTarget();
      List<EntityType> entityTypes = new ArrayList<>();
      if(priorityTarget == CustomCreature.PriorityTarget.ANY) {
        for(CustomCreature.PriorityTarget priorityTargets : CustomCreature.PriorityTarget.values()) {
          if(priorityTargets == CustomCreature.PriorityTarget.ANY) {
            continue;
          }
          entityTypes.add(EntityType.valueOf(priorityTargets.toString()));
        }
      } else {
        entityTypes.add(EntityType.valueOf(priorityTarget.toString()));
      }
      if(entityTypes.contains(event.getEntity().getType())) {
        TNTPrimed primed = (TNTPrimed) event.getDamager().getLocation().getWorld().spawnEntity(event.getDamager().getLocation(), EntityType.PRIMED_TNT);
        primed.setSource(event.getEntity());
        primed.setMetadata("VD_PRIMED_TNT", new FixedMetadataValue(arena.getPlugin(), true));
        event.getDamager().remove();
        Bukkit.getServer().getPluginManager().callEvent(new EntityDeathEvent((LivingEntity) event.getDamager(), new ArrayList<>(Collections.singletonList(new ItemStack(Material.ROTTEN_FLESH))), 6));
      }
    }
  }

  @EventHandler
  public void onTntDamage(EntityDamageByEntityEvent event) {
    if(!(event.getDamager() instanceof TNTPrimed)) {
      return;
    }
    TNTPrimed primed = (TNTPrimed) event.getDamager();
    if(!primed.hasMetadata("VD_PRIMED_TNT")) {
      return;
    }
    if(event.getEntity() instanceof Player) {
      User user = plugin.getUserManager().getUser((Player) event.getEntity());
      if(user == null || user.getArena() == null) {
        return;
      }
      //reduce tnt damage by half for players
      event.setDamage(event.getDamage() * 0.5);
    } else if(CreatureUtils.isEnemy(event.getEntity())) {
      //disable friendly fire
      event.setCancelled(true);
    }
  }

  public CustomCreature getCustomCreatureFromCreature(Arena arena, Creature creature) {
    List<MetadataValue> metadataValueList = creature.getMetadata(CustomCreature.CREATURE_ID_METADATA);
    if(metadataValueList.isEmpty()) {
      plugin.getDebugger().debug("Arena {0} Couldn't find creature meta data", arena.getId());
      return null;
    }
    for(MetadataValue metadataValue : metadataValueList) {
      if(plugin.getEnemySpawnerRegistry().getSpawnerByName(metadataValue.asString()).isPresent()) {
        EnemySpawner enemySpawner = plugin.getEnemySpawnerRegistry().getSpawnerByName(metadataValue.asString()).get();
        if(enemySpawner instanceof CustomCreature) {
          return (CustomCreature) enemySpawner;
        }
      }
    }
    plugin.getDebugger().debug("Arena {0} Couldn't find creature spawner", arena.getId());
    return null;
  }

}
