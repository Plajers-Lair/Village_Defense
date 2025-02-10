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

package plugily.projects.villagedefense.arena;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.minigamesbox.classic.arena.ArenaState;
import plugily.projects.minigamesbox.classic.arena.PluginArenaEvents;
import plugily.projects.minigamesbox.classic.handlers.items.SpecialItem;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.misc.complement.ComplementAccessor;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.events.api.PlugilyEntityPickupItemEvent;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewCreatureUtils;
import plugily.projects.villagedefense.arena.villager.VillagerAiManager;
import plugily.projects.villagedefense.utils.NearbyUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 13.03.2018
 */
public class ArenaEvents extends PluginArenaEvents {

  private final Main plugin;

  public ArenaEvents(Main plugin) {
    super(plugin);
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onBraveTraitRetreat(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Villager villager)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (!arena.getVillagers().contains(villager)) {
        continue;
      }
      VillagerAiManager aiManager = arena.getVillagerAiManager();
      double healthPercent = villager.getHealth() / villager.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
      if (healthPercent <= 0.5) {
        villager.setMetadata("VD_BRAVE_RETREAT", new FixedMetadataValue(plugin, true));
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
          int homeIndex = villager.getMetadata(VillagerAiManager.VILLAGER_PERSONALITY_CHOSEN_HOME_ID).get(0).asInt();
          Location home = NearbyUtils.getRandomNearbyLocation(aiManager.getPlaces().get(VillagerAiManager.Place.VILLAGER_HOME_ZONE).get(homeIndex), 2);
          aiManager.doStartPathfinder(villager, home, (v, l) -> {
          });
          Bukkit.getScheduler().runTaskLater(plugin, () -> {
            villager.removeMetadata("VD_BRAVE_RETREAT", plugin);
          }, 20 * 10);
        }, 20);
      }
    }
  }

  @EventHandler
  public void onDoorLockPhysicsBlock(BlockPhysicsEvent event) {
    if (!(event.getChangedBlockData() instanceof Door door)) {
      return;
    }
    if (!door.isOpen() && event.getSourceBlock().hasMetadata("VD_DOOR_LOCK")) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onCrammingSuffocationDamage(EntityDamageEvent event) {
    if (event.getCause() != EntityDamageEvent.DamageCause.CRAMMING && event.getCause() != EntityDamageEvent.DamageCause.SUFFOCATION) {
      return;
    }
    if (event.getEntity().hasMetadata("VD_CUSTOM_ENTITY")) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onArrowDecay(ProjectileHitEvent event) {
    if (!(event.getEntity() instanceof Arrow arrow)) {
      return;
    }
    if (!(arrow.getShooter() instanceof Player player)) {
      return;
    }
    if (plugin.getArenaRegistry().isInArena(player)) {
      Bukkit.getScheduler().runTaskLater(plugin, () -> event.getEntity().remove(), 20 * 4);
    }
  }

  @EventHandler
  public void onWolfTeleport(EntityTeleportEvent event) {
    if (!(event.getEntity() instanceof Wolf wolf)) {
      return;
    }
    if (!wolf.hasMetadata("VD_OWNER_UUID")) {
      return;
    }
    if (wolf.hasMetadata("VD_TELEPORT_OVERRIDE_TEMP")) {
      return;
    }
    //is a VD entity, cancel no matter what, unless overridden
    event.setCancelled(true);
  }

  @EventHandler
  public void onDeadTargetAdjust(EntityTargetLivingEntityEvent event) {
    if (event.getTarget() == null) {
      return;
    }
    if (event.getTarget().hasMetadata("VD_PET_DEAD")) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onPlayerFocusAdjust(EntityTargetLivingEntityEvent event) {
    if (!(event.getTarget() instanceof Player)) {
      return;
    }
    Player player = (Player) event.getTarget();
    if (!plugin.getArenaRegistry().isInArena(player)) {
      return;
    }
    User user = plugin.getUserManager().getUser(player);
    //cancel targeting of spectators or being targeted by ally pets
    if (user.isSpectator() || event.getEntity() instanceof Wolf || event.getEntity() instanceof IronGolem) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onAllyFocusAdjust(EntityTargetLivingEntityEvent event) {
    if (event.getTarget() instanceof Player) {
      //prevent ally pets targeting players
      if (event.getEntity() instanceof Wolf || event.getEntity() instanceof IronGolem) {
        //cancel no matter which wolf/golem targets player, naively assume it's the same arena entity
        if (plugin.getArenaRegistry().isInArena((Player) event.getTarget())) {
          event.setCancelled(true);
        }
      }
    } else if (event.getTarget() instanceof Wolf || event.getTarget() instanceof IronGolem) {
      //prevent ally pets targeting ally pets
      if (event.getEntity() instanceof Wolf || event.getEntity() instanceof IronGolem) {
        for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
          List<LivingEntity> allies = new ArrayList<>();
          allies.addAll(arena.getWolves());
          allies.addAll(arena.getIronGolems());
          if (allies.contains(event.getEntity()) && allies.contains(event.getTarget())) {
            event.setCancelled(true);
            return;
          }
        }
      }
    } else if (event.getTarget() instanceof Villager villager) {
      //prevent ally pets targeting villagers
      if (event.getEntity() instanceof Wolf || event.getEntity() instanceof IronGolem) {
        for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
          List<LivingEntity> allies = new ArrayList<>();
          allies.addAll(arena.getWolves());
          allies.addAll(arena.getIronGolems());
          if (allies.contains(event.getEntity()) && arena.getVillagers().contains(villager)) {
            event.setCancelled(true);
            return;
          }
        }
      }
    }
  }

  //override WorldGuard build deny flag where villagers cannot be damaged
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onVillagerDamage(EntityDamageByEntityEvent e) {
    if (e.getEntityType() != EntityType.VILLAGER || !(e.getDamager() instanceof Creature)) {
      return;
    }

    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (arena.getVillagers().contains(e.getEntity()) && arena.getEnemies().contains(e.getDamager())) {
        e.setCancelled(false);
        e.getEntity().setCustomName(NewCreatureUtils.getHealthNameTagPreDamage((Creature) e.getEntity(), e.getFinalDamage()));
        XSound.ENTITY_VILLAGER_HURT.play(e.getEntity().getLocation(), 10.0f, 1.0f);

        doApplyDamageGlowingWarning(e.getDamager(), arena);
        break;
      }
    }
  }

  private void doApplyDamageGlowingWarning(Entity target, Arena arena) {
    String GLOWING_TARGET_METADATA = "VD_RECENT_VILLAGER_DAMAGE_GLOW";
    target.setMetadata(GLOWING_TARGET_METADATA, new FixedMetadataValue(plugin, true));
    new BukkitRunnable() {
      boolean toggle = false;
      int tick = 0;

      @Override
      public void run() {
        if (target.isDead() || tick >= 60) {
          try {
            target.removeMetadata(GLOWING_TARGET_METADATA, plugin);
            for (Player player : arena.getPlayers()) {
              plugin.getGlowingEntities().unsetGlowing(target, player);
            }
          } catch (Exception ignored) {
          }
          cancel();
          return;
        }
        try {
          for (Player player : arena.getPlayers()) {
            plugin.getGlowingEntities().setGlowing(target, player, toggle ? ChatColor.YELLOW : ChatColor.RED);
          }
        } catch (Exception ignored) {
        }
        toggle = !toggle;
        tick += 10;
      }
    }.runTaskTimer(plugin, 0, 10);
  }

  //todo apply kills for golem by VD_OWNER_UUID detection
  @EventHandler
  public void onDieEntity(EntityDamageByEntityEvent e) {
    if (!(e.getDamager() instanceof Wolf && e.getEntity() instanceof Creature)) {
      return;
    }

    if (e.getDamage() >= ((Creature) e.getEntity()).getHealth()) {

      //trick to get non player killer of zombie
      for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
        if (arena.getEnemies().contains(e.getEntity())) {
          org.bukkit.entity.AnimalTamer owner = ((Wolf) e.getDamager()).getOwner();

          if (owner instanceof Player) { //prevent offline player cast error
            Player player = (Player) owner;

            if (plugin.getArenaRegistry().getArena(player) != null) {
              plugin.getUserManager().addStat(player, plugin.getStatsStorage().getStatisticType("KILLS"));
              plugin.getUserManager().addExperience(player, (int) (2 * Math.log(arena.getWave())));
            }
          }

          break;
        }
      }
    }
  }

  @EventHandler
  public void onItemDrop(ItemSpawnEvent e) {
    org.bukkit.entity.Item item = e.getEntity();

    if (item.getItemStack().getType() != Material.ROTTEN_FLESH) {
      return;
    }

    Location itemLoc = item.getLocation();

    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      Location start = arena.getStartLocation();

      if (itemLoc.getWorld() != start.getWorld() || itemLoc.distance(start) > 150) {
        continue;
      }

      arena.addDroppedFlesh(item);
    }
  }

  @EventHandler
  public void onVillagerDeath(EntityDeathEvent event) {
    LivingEntity entity = event.getEntity();
    if (!(entity instanceof Creature)) {
      return;
    }
    for (Arena arena : plugin.getArenaRegistry().getPluginArenas()) {
      if (event.getEntityType() == EntityType.VILLAGER) {
        if (!arena.getVillagers().contains(entity)) {
          continue;
        }
        arena.getStartLocation().getWorld().strikeLightningEffect(entity.getLocation());
        arena.removeVillager((Villager) entity);
        plugin.getRewardsHandler().performReward(null, arena, plugin.getRewardsHandler().getRewardType("VILLAGER_DEATH"));
        plugin.getHolidayManager().applyHolidayDeathEffects(entity);
        new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_VILLAGER_DIED").asKey().arena(arena).sendArena();
      }
    }
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerDie(PlayerDeathEvent e) {
    Arena arena = plugin.getArenaRegistry().getArena(e.getEntity());
    if (arena == null) {
      return;
    }

    final Player player = e.getEntity();

    if (player.isDead()) {
      player.setHealth(VersionUtils.getMaxHealth(player));
    }
    plugin.getRewardsHandler().performReward(player, arena, plugin.getRewardsHandler().getRewardType("PLAYER_DEATH"));
    ComplementAccessor.getComplement().setDeathMessage(e, "");
    e.getDrops().clear();
    e.setDroppedExp(0);
    plugin.getHolidayManager().applyHolidayDeathEffects(player);
    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.spigot().respawn(), 1);
    plugin.getServer().getScheduler().runTask(plugin, () -> {
      if (arena.getArenaState() == ArenaState.STARTING) {
        VersionUtils.teleport(player, arena.getStartLocation());
        return;
      }

      if (arena.getArenaState() == ArenaState.ENDING || arena.getArenaState() == ArenaState.RESTARTING) {
        player.getInventory().clear();
        player.setFlying(false);
        player.setAllowFlight(false);
        plugin.getUserManager().getUser(player).setStatistic("ORBS", 0);
        VersionUtils.teleport(player, arena.getEndLocation());
        return;
      }

      User user = plugin.getUserManager().getUser(player);

      plugin.getUserManager().addStat(user, plugin.getStatsStorage().getStatisticType("DEATHS"));
      VersionUtils.teleport(player, arena.getStartLocation());
      user.setSpectator(true);
      player.setGameMode(GameMode.SURVIVAL);

      //VD Gold - lose only 40% of orbs on death
      user.setStatistic("ORBS", (int) (user.getStatistic("ORBS") * 0.6));

      ArenaUtils.hidePlayer(player, arena);
      player.setAllowFlight(true);
      player.setFlying(true);
      player.getInventory().clear();
      VersionUtils.sendTitle(player, new MessageBuilder("IN_GAME_DEATH_SCREEN").asKey().build(), 0, 5 * 20, 0);
      sendSpectatorActionBar(user, arena);
      new MessageBuilder(MessageBuilder.ActionType.DEATH).arena(arena).player(player).sendArena();

      plugin.getSpecialItemManager().addSpecialItemsOfStage(player, SpecialItem.DisplayStage.SPECTATOR);

      NewCreatureUtils.unTargetPlayerFromZombies(player, arena);
    });
  }

  private void sendSpectatorActionBar(User user, Arena arena) {
    new BukkitRunnable() {
      @Override
      public void run() {
        if (arena.getArenaState() == ArenaState.ENDING || !user.isSpectator()) {
          cancel();
          return;
        }
        Player player = user.getPlayer();
        if (player == null) {
          cancel();
        } else {
          VersionUtils.sendActionBar(player, new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_RESPAWN_ON_NEXT").asKey().player(player).arena(arena).build());
        }
      }
    }.runTaskTimer(plugin, 30, 30);
  }


  @EventHandler(priority = EventPriority.HIGHEST)
  public void onRespawn(PlayerRespawnEvent e) {
    Arena arena = plugin.getArenaRegistry().getArena(e.getPlayer());
    if (arena == null) {
      return;
    }
    Player player = e.getPlayer();
    player.setAllowFlight(true);
    player.setFlying(true);
    User user = plugin.getUserManager().getUser(player);
    if (!user.isSpectator()) {
      user.setSpectator(true);
      player.setGameMode(GameMode.SURVIVAL);
      player.removePotionEffect(PotionEffectType.NIGHT_VISION);
      player.removePotionEffect(PotionEffectType.SPEED);
    }
    e.setRespawnLocation(arena.getStartLocation());
  }

  @EventHandler
  public void onPickup(PlugilyEntityPickupItemEvent e) {
    if (e.getEntity().getType() != EntityType.PLAYER || e.getItem().getItemStack().getType() != Material.ROTTEN_FLESH) {
      return;
    }
    Player player = (Player) e.getEntity();
    Arena arena = plugin.getArenaRegistry().getArena(player);
    if (arena == null) {
      return;
    }
    if (plugin.getUserManager().getUser(player).isSpectator()) {
      e.setCancelled(true);
    }
    arena.removeDroppedFlesh(e.getItem());
  }


}
