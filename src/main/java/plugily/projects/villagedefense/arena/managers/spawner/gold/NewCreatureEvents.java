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
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.Nullable;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.VDEnemy;
import plugily.projects.villagedefense.kits.utils.KitHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NewCreatureEvents implements Listener {

  private final Main plugin;

  public NewCreatureEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onRetargetBlock(EntityTargetLivingEntityEvent event) {
    if (event.getEntity().hasMetadata(NewEnemySpawnerManager.CREATURE_PERSISTENT_TARGETING) && event.getReason() != EntityTargetEvent.TargetReason.CUSTOM) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onCreatureDamagingEntity(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Creature yourself)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.isFighting()) {
        continue;
      }
      VDEnemy enemy = NewCreatureUtils.getEnemyFromCreature(yourself);
      if (enemy == null) {
        return;
      }
      enemy.getOnDamaging().onDamaging(event, arena);
    }
  }

  @EventHandler
  public void onEntityDamagingCreature(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Creature yourself)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.isFighting()) {
        continue;
      }
      VDEnemy enemy = NewCreatureUtils.getEnemyFromCreature(yourself);
      if (enemy == null) {
        return;
      }
      enemy.getOnDamageBy().onBeingDamaged(event, arena);
    }
  }

  @EventHandler
  public void onEntityDeath(EntityDeathEvent event) {
    if (!(event.getEntity() instanceof Creature yourself)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.isFighting()) {
        continue;
      }
      VDEnemy enemy = NewCreatureUtils.getEnemyFromCreature(yourself);
      if (enemy == null) {
        return;
      }
      enemy.getOnDeath().onDeath(event, arena);
    }
  }

  @EventHandler
  public void onTntDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof TNTPrimed primed)) {
      return;
    }
    if (!primed.hasMetadata("VD_PRIMED_TNT")) {
      return;
    }
    if (NewCreatureUtils.isEnemy(event.getEntity())) {
      event.setCancelled(true);
      return;
    }
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }
    User user = plugin.getUserManager().getUser(player);
    if (user == null || user.getArena() == null) {
      return;
    }
    if (primed.hasMetadata("VD_TNT_DAMAGE_PERCENT")) {
      event.setDamage(0);
      player.damage(0);
      double percent = primed.getMetadata("VD_TNT_DAMAGE_PERCENT").get(0).asDouble();
      KitHelper.maxHealthPercentDamage(player, percent);
    }
  }

  //todo improve the code structure
  @EventHandler
  @Deprecated
  public void onCreatureDeathEvent(EntityDeathEvent event) {
    LivingEntity entity = event.getEntity();
    if (!(entity instanceof Creature creature)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getEnemies().contains(creature)) {
        continue;
      }
      VDEnemy enemy = NewCreatureUtils.getEnemyFromCreature(creature);
      if (enemy == null) {
        continue;
      }
      arena.removeEnemy(creature);
      arena.changeArenaOptionBy("TOTAL_KILLED_ZOMBIES", 1);

      Player killer = creature.getKiller();
      if (killer == null) {
        killer = performKillerDetection(event);
      }
      applyKillMetadata(event);
      Arena killerArena = plugin.getArenaRegistry().getArena(killer);

      if (killerArena != null) {
        plugin.getUserManager().addStat(killer, plugin.getStatsStorage().getStatisticType("KILLS"));
        plugin.getUserManager().addExperience(killer, (int) (2 * Math.log(arena.getWave())));
        plugin.getRewardsHandler().performReward(killer, plugin.getRewardsHandler().getRewardType("ZOMBIE_KILL"));
      }

      event.setDroppedExp(0);
      filterDrops(event, killer);
      if (killer != null) {
        User user = plugin.getUserManager().getUser(killer);
        if (user == null || !user.getArena().equals(arena)) {
          continue;
        }
        Map<Player, Double> contribution = arena.getAssistHandler().doDistributeAssistRewards(killer, creature);
        for (Map.Entry<Player, Double> entry : contribution.entrySet()) {
          double share = entry.getValue() / 100.0;
          double multiplier = 1 + Math.log10(arena.getPlayers().size());
          int amount = (int) Math.ceil(10 * share * multiplier);
          User targetUser = plugin.getUserManager().getUser(entry.getKey());
          targetUser.adjustStatistic(plugin.getStatsStorage().getStatisticType("ORBS"), amount);
        }
      }
    }
  }

  @Nullable
  private Player performKillerDetection(EntityDeathEvent event) {
    EntityDamageEvent cause = event.getEntity().getLastDamageCause();
    if (!(cause instanceof EntityDamageByEntityEvent)) {
      return null;
    }
    Entity entity = ((EntityDamageByEntityEvent) cause).getDamager();
    if (entity instanceof Player) {
      return (Player) entity;
    } else if (entity instanceof Wolf || entity instanceof IronGolem) {
      if (!entity.hasMetadata("VD_OWNER_UUID")) {
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

  private void filterDrops(EntityDeathEvent event, Player player) {
    List<ItemStack> filtered = new ArrayList<>();
    for (ItemStack itemStack : event.getDrops()) {
      if (itemStack == null || !XMaterial.ROTTEN_FLESH.isSimilar(itemStack)) {
        continue;
      }
      itemStack.setAmount(1);
      filtered.add(itemStack);
    }
    event.getDrops().clear();
    if (filtered.isEmpty() || player == null) {
      return;
    }
    User user = plugin.getUserManager().getUser(player);
    if (user.isSpectator()) {
      return;
    }
    player.getInventory().addItem(filtered.toArray(new ItemStack[]{}));
  }

}
