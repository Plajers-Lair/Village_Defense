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

package plugily.projects.villagedefense.handlers.upgrade;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.creatures.CreatureUtils;
import plugily.projects.villagedefense.kits.utils.KitHelper;
import plugily.projects.villagedefense.kits.utils.KitSpecifications;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

//todo merge into single event
public class EntityUpgradeHandlerEvents implements Listener {

  private final Main plugin;
  private final Map<UUID, Integer> unstoppableStreak = new HashMap<>();

  public EntityUpgradeHandlerEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onSwarmAwareness(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Wolf wolf)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getWolves().contains(wolf)) {
        continue;
      }
      EntityUpgrade swarmAwareness1 = plugin.getEntityUpgradeManager().getUpgrade("WOLF_SWARM_AWARENESS_1");
      EntityUpgrade swarmAwareness2 = plugin.getEntityUpgradeManager().getUpgrade("WOLF_SWARM_AWARENESS_2");
      if (!wolf.hasMetadata(swarmAwareness1.getMetadataKey())
        && !wolf.hasMetadata(swarmAwareness2.getMetadataKey())) {
        return;
      }
      double increase = 0;
      int nearby = 0;
      for (Entity entity : plugin.getBukkitHelper().getNearbyEntities(event.getDamager().getLocation(), 3)) {
        if (entity instanceof Wolf) {
          nearby++;
        }
      }
      if (event.getDamager().hasMetadata(swarmAwareness2.getMetadataKey())) {
        increase = swarmAwareness2.getUpgradeData().get("increase");
      } else {
        increase = swarmAwareness1.getUpgradeData().get("increase");
      }
      double bonusPercent = Math.min(increase * nearby, 30) / 100.0;
      event.setDamage(event.getDamage() + (event.getDamage() * bonusPercent));
      return;
    }
  }

  @EventHandler
  public void onFinalDefense(EntityDeathEvent event) {
    if (!(event.getEntity() instanceof IronGolem ironGolem)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getIronGolems().contains(ironGolem)) {
        continue;
      }
      EntityUpgrade finalDefense = plugin.getEntityUpgradeManager().getUpgrade("GOLEM_FINAL_DEFENSE");
      if (!ironGolem.hasMetadata(finalDefense.getMetadataKey())) {
        return;
      }
      VersionUtils.sendParticles("EXPLOSION_HUGE", arena.getPlayers(), ironGolem.getLocation(), 5);
      for (Entity en : plugin.getBukkitHelper().getNearbyEntities(ironGolem.getLocation(), 6)) {
        if (CreatureUtils.isEnemy(en)) {
          ((Creature) en).damage(KitSpecifications.LETHAL_DAMAGE, ironGolem);
        }
      }
      for (Entity en : plugin.getBukkitHelper().getNearbyEntities(ironGolem.getLocation(), 9)) {
        if (CreatureUtils.isEnemy(en)) {
          ((Creature) en).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1));
          ((Creature) en).damage(0.5, ironGolem);
        }
      }
      return;
    }
  }

  @EventHandler
  public void onUnstoppableStreak(EntityDeathEvent event) {
    if (!(event.getEntity().getKiller() instanceof IronGolem ironGolem)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getIronGolems().contains(ironGolem)) {
        continue;
      }
      EntityUpgrade unstoppableStreak1 = plugin.getEntityUpgradeManager().getUpgrade("GOLEM_UNSTOPPABLE_STREAK_1");
      EntityUpgrade unstoppableStreak2 = plugin.getEntityUpgradeManager().getUpgrade("GOLEM_UNSTOPPABLE_STREAK_2");
      if (!ironGolem.hasMetadata(unstoppableStreak1.getMetadataKey())
        && !ironGolem.hasMetadata(unstoppableStreak2.getMetadataKey())) {
        return;
      }
      double increaseValue;
      if (ironGolem.hasMetadata(unstoppableStreak2.getMetadataKey())) {
        increaseValue = unstoppableStreak2.getUpgradeData().get("increase");
      } else {
        increaseValue = unstoppableStreak1.getUpgradeData().get("increase");
      }
      if (unstoppableStreak.containsKey(ironGolem.getUniqueId())) {
        Bukkit.getScheduler().cancelTask(unstoppableStreak.get(ironGolem.getUniqueId()));
      }
      final double originalDamage = ironGolem.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
      ironGolem.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(originalDamage + increaseValue);
      int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> ironGolem.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(originalDamage), 20 * 5).getTaskId();
      unstoppableStreak.put(ironGolem.getUniqueId(), taskId);
      return;
    }
  }

  @EventHandler
  public void onToughening(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof IronGolem ironGolem)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getIronGolems().contains(ironGolem)) {
        continue;
      }
      EntityUpgrade toughening = plugin.getEntityUpgradeManager().getUpgrade("GOLEM_TOUGHENING");
      if (!ironGolem.hasMetadata(toughening.getMetadataKey())) {
        return;
      }
      if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
        if (ironGolem.hasMetadata("VD_GOLEM_EXPLOSION_TIME")) {
          Instant date = new Date(ironGolem.getMetadata("VD_GOLEM_EXPLOSION_TIME").get(0).asLong()).toInstant();
          if (Duration.between(date, Instant.now()).abs().toSeconds() <= 8) {
            event.setDamage(event.getDamage() - (event.getDamage() * 0.3));
          } else {
            ironGolem.removeMetadata("VD_GOLEM_EXPLOSION_TIME", plugin);
          }
        }
        ironGolem.setMetadata("VD_GOLEM_EXPLOSION_TIME", new FixedMetadataValue(plugin, System.currentTimeMillis()));
      }
      if (ironGolem.getHealth() < ironGolem.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() * 0.4) {
        event.setDamage(event.getDamage() * 0.75);
      }
      return;
    }
  }

  @EventHandler
  public void onWillToSurvive(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof IronGolem ironGolem)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getIronGolems().contains(ironGolem)) {
        continue;
      }
      EntityUpgrade willToSurvive = plugin.getEntityUpgradeManager().getUpgrade("GOLEM_SURVIVOR");
      if (!ironGolem.hasMetadata(willToSurvive.getMetadataKey())) {
        return;
      }
      double healAmount = event.getDamage() * 0.05;
      ironGolem.setHealth(Math.min(ironGolem.getHealth() + healAmount, VersionUtils.getMaxHealth(ironGolem)));
      return;
    }
  }

  @EventHandler
  public void onBannerOfCommand(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof IronGolem ironGolem)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getIronGolems().contains(ironGolem)) {
        continue;
      }
      EntityUpgrade bannerOfCommand = plugin.getEntityUpgradeManager().getUpgrade("GOLEM_BANNER_OF_COMMAND");
      for (Entity entity : ironGolem.getNearbyEntities(6, 6, 6)) {
        if (!(entity instanceof IronGolem golem)) {
          continue;
        }
        if (golem.hasMetadata(bannerOfCommand.getMetadataKey())) {
          event.setDamage(event.getDamage() + (event.getDamage() * 0.1));
        }
      }
      return;
    }
  }

  @EventHandler
  public void onRemembranceLament(EntityDeathEvent event) {
    if (!(event.getEntity() instanceof IronGolem ironGolem)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getIronGolems().contains(ironGolem)) {
        continue;
      }
      EntityUpgrade remembranceLament = plugin.getEntityUpgradeManager().getUpgrade("GOLEM_LAMENT");
      if (!ironGolem.hasMetadata(remembranceLament.getMetadataKey())) {
        return;
      }
      for (Creature creature : arena.getEnemies()) {
        if (KitHelper.executeEnemy(creature, ironGolem)) {
          VersionUtils.sendParticles("LAVA", arena.getPlayers(), creature.getLocation(), 20);
        }
      }
      List<LivingEntity> toHeal = new ArrayList<>();
      toHeal.addAll(arena.getPlayersLeft());
      toHeal.addAll(arena.getVillagers());
      toHeal.addAll(arena.getWolves());
      toHeal.addAll(arena.getIronGolems());
      for (LivingEntity livingEntity : toHeal) {
        livingEntity.setHealth(VersionUtils.getMaxHealth(livingEntity));
      }
      for (Player player : arena.getPlayers()) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aOur beloved Iron Golem has died and protected the Village as a final wish, rest in peace."));
      }
    }
  }

  @EventHandler
  public void onDeepWounds(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Wolf wolf) || !CreatureUtils.isEnemy(event.getEntity())) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getWolves().contains(wolf)) {
        continue;
      }
      EntityUpgrade deepWounds = plugin.getEntityUpgradeManager().getUpgrade("WOLF_DEEP_WOUNDS");
      if (!wolf.hasMetadata(deepWounds.getMetadataKey())) {
        return;
      }
      if (ThreadLocalRandom.current().nextInt(0, 100) <= 25) {
        ((LivingEntity) event.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 3, 0));
      }
    }
  }

  @EventHandler
  public void onWolfPack(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Wolf wolf) || !CreatureUtils.isEnemy(event.getEntity())) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getWolves().contains(wolf)) {
        continue;
      }
      if (event.getEntity().hasMetadata("VD_WOLF_PACK_MARKED")) {
        Instant date = new Date(event.getEntity().getMetadata("VD_WOLF_PACK_MARKED").get(0).asLong()).toInstant();
        if (Duration.between(date, Instant.now()).abs().toSeconds() <= 5) {
          event.setDamage(event.getDamage() + (event.getDamage() * 0.1));
        } else {
          event.getEntity().removeMetadata("VD_WOLF_PACK_MARKED", plugin);
        }
      }
      EntityUpgrade wolfPack = plugin.getEntityUpgradeManager().getUpgrade("WOLF_WOLF_PACK");
      if (wolf.hasMetadata(wolfPack.getMetadataKey())) {
        event.getEntity().removeMetadata("VD_WOLF_PACK_MARKED", plugin);
        event.getEntity().setMetadata("VD_WOLF_PACK_MARKED", new FixedMetadataValue(plugin, System.currentTimeMillis()));
      }
    }
  }

  @EventHandler
  public void onBloodyRevengeDeath(EntityDeathEvent event) {
    if (!(event.getEntity() instanceof Wolf wolf)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getWolves().contains(wolf)) {
        continue;
      }
      EntityUpgrade bloodyRevenge = plugin.getEntityUpgradeManager().getUpgrade("WOLF_BLOODY_REVENGE");
      for (Wolf arenaWolf : arena.getWolves()) {
        if (!arenaWolf.hasMetadata(bloodyRevenge.getMetadataKey())) {
          continue;
        }
        arenaWolf.setMetadata("VD_WOLF_BLOODY_REVENGE", new FixedMetadataValue(plugin, true));
        Bukkit.getScheduler().runTaskLater(plugin, () -> arenaWolf.removeMetadata("VD_WOLF_BLOODY_REVENGE", plugin), 20 * 5);
      }
      return;
    }
  }

  @EventHandler
  public void onBloodyRevengeAttack(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Wolf wolf) || !CreatureUtils.isEnemy(event.getEntity())) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getWolves().contains(wolf)) {
        continue;
      }
      if (!wolf.hasMetadata("VD_WOLF_BLOODY_REVENGE")) {
        return;
      }
      double healAmount = event.getDamage() * 0.05;
      wolf.setHealth(Math.min(wolf.getHealth() + healAmount, VersionUtils.getMaxHealth(wolf)));
      event.setDamage(event.getDamage() + (event.getDamage() * 0.1));
      Vector velocity = event.getEntity().getLocation().getDirection().multiply(-1).normalize().multiply(0.3);
      event.getEntity().setVelocity(velocity);
    }
  }

  @EventHandler
  public void onRobber(EntityDeathEvent event) {
    if (!(event.getEntity().getKiller() instanceof Wolf wolf)) {
      return;
    }
    if (!wolf.hasMetadata("VD_OWNER_UUID")) {
      return;
    }
    UUID playerId = UUID.fromString(wolf.getMetadata("VD_OWNER_UUID").get(0).asString());
    Player owner = Bukkit.getPlayer(playerId);
    User user = plugin.getUserManager().getUser(owner);
    if (user == null) {
      return;
    }
    user.adjustStatistic(plugin.getStatsStorage().getStatisticType("ORBS"), ThreadLocalRandom.current().nextInt(4, 12));
  }

  @EventHandler
  public void onWolfAlpha(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Wolf wolf)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getWolves().contains(wolf)) {
        continue;
      }
      EntityUpgrade wolfAlpha = plugin.getEntityUpgradeManager().getUpgrade("WOLF_ALPHA");
      for (Entity entity : wolf.getNearbyEntities(6, 6, 6)) {
        if (!(entity instanceof IronGolem golem)) {
          continue;
        }
        if (golem.hasMetadata(wolfAlpha.getMetadataKey())) {
          event.setDamage(event.getDamage() + (event.getDamage() * 0.1));
        }
      }
      return;
    }
  }

  @EventHandler
  public void onMoreThanDeath(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Wolf wolf)) {
      return;
    }
    if (wolf.hasMetadata("VD_WOLF_MORE_THAN_DEATH")) {
      event.setDamage(0);
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getWolves().contains(wolf)) {
        continue;
      }
      EntityUpgrade moreThanDeath = plugin.getEntityUpgradeManager().getUpgrade("WOLF_MORE_THAN_DEATH");
      if (!wolf.hasMetadata(moreThanDeath.getMetadataKey())) {
        return;
      }
      if (event.getDamage() >= wolf.getHealth()) {
        event.setDamage(0);
        wolf.setMetadata("VD_WOLF_MORE_THAN_DEATH", new FixedMetadataValue(plugin, true));
        double defaultValue = wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
        wolf.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(defaultValue + (defaultValue * 0.75));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
          wolf.removeMetadata("VD_WOLF_MORE_THAN_DEATH", plugin);
          wolf.damage(KitSpecifications.LETHAL_DAMAGE);
        }, 20 * 5);
      }
    }
  }

  @EventHandler
  public void onIronWillDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof IronGolem ironGolem)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getIronGolems().contains(ironGolem)) {
        continue;
      }
      EntityUpgrade ironWill = plugin.getEntityUpgradeManager().getUpgrade("GOLEM_IRON_WILL");
      if (!ironGolem.hasMetadata(ironWill.getMetadataKey())) {
        return;
      }
      int defaultAmount = plugin.getConfig().getInt("Limit.Spawn.Villagers", 10);
      int missing = defaultAmount - arena.getVillagers().size();
      event.setDamage(event.getDamage() + (event.getDamage() * (0.03 * missing)));
      return;
    }
  }

  @EventHandler
  public void onIronWillReceiveDamage(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof IronGolem ironGolem)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getIronGolems().contains(ironGolem)) {
        continue;
      }
      EntityUpgrade ironWill = plugin.getEntityUpgradeManager().getUpgrade("GOLEM_IRON_WILL");
      if (!ironGolem.hasMetadata(ironWill.getMetadataKey())) {
        return;
      }
      int defaultAmount = plugin.getConfig().getInt("Limit.Spawn.Villagers", 10);
      int missing = defaultAmount - arena.getVillagers().size();
      event.setDamage(event.getDamage() - (event.getDamage() * (0.03 * missing)));
      return;
    }
  }

}
