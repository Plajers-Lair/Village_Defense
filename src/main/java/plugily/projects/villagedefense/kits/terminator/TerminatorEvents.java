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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.user.User;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewCreatureUtils;
import plugily.projects.villagedefense.handlers.hologram.ArmorStandHologram;
import plugily.projects.villagedefense.kits.utils.KitSpecifications;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

public class TerminatorEvents implements Listener {

  private final Main plugin;

  public TerminatorEvents(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  //todo random waves roll
  @EventHandler
  public void onAugmentDrop(EntityDeathEvent event) {
    if (!NewCreatureUtils.isEnemy(event.getEntity())) {
      return;
    }
    Player player = event.getEntity().getKiller();
    User user = plugin.getUserManager().getUser(player);
    if (player == null || user.getArena() == null || ((Arena) user.getArena()).getZombiesLeft() > 0) {
      return;
    }
    Optional<Player> terminatorPlayer = user.getArena()
      .getPlayers()
      .stream()
      .filter(p -> plugin.getUserManager().getUser(p).getKit() instanceof TerminatorKit)
      .findFirst();
    if (terminatorPlayer.isEmpty()) {
      return;
    }
    int count = 0;
    if (terminatorPlayer.get().hasMetadata(TerminatorAugment.AUGMENTS_COUNT_METADATA_KEY)) {
      count = terminatorPlayer.get().getMetadata(TerminatorAugment.AUGMENTS_COUNT_METADATA_KEY).get(0).asInt();
    }
    switch (KitSpecifications.getTimeState((Arena) user.getArena())) {
      case LATE -> {
        if (count < 3) {
          doDropAugmentShard(user, event.getEntity().getLocation());
        }
      }
      case MID -> {
        if (count < 2) {
          doDropAugmentShard(user, event.getEntity().getLocation());
        }
      }
      case EARLY -> {
        if (count < 1) {
          doDropAugmentShard(user, event.getEntity().getLocation());
        }
      }
    }
  }

  private void doDropAugmentShard(User user, Location location) {
    Location targetLocation = location.clone().add(0, -0.75, 0);
    ArmorStandHologram hologram = new ArmorStandHologram(targetLocation)
      .appendItem(XMaterial.PRISMARINE_SHARD.parseItem())
      .appendLine(ChatColor.translateAlternateColorCodes('&', "&6&lCORE COMPONENT &e&lAUGMENT"))
      .appendLine(ChatColor.translateAlternateColorCodes('&', "&7Can be picked by &6&lTerminator&7 only"));
    hologram.setPickupHandler(target -> {
      if (!user.getArena().equals(plugin.getArenaRegistry().getArena(target))) {
        return;
      }
      XSound.ENTITY_PLAYER_LEVELUP.play(target, 1, 0);
      XSound.ENTITY_VILLAGER_YES.play(target);
      hologram.delete();
      new TerminatorAugmentsGui(plugin).openGui(target);
    });
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (!hologram.isDeleted()) {
        hologram.delete();
      }
    }, 20 * 25);
  }

  @EventHandler
  public void onVitalityTrigger(EntityDeathEvent event) {
    if (!NewCreatureUtils.isEnemy(event.getEntity())) {
      return;
    }
    Player player = event.getEntity().getKiller();
    User user = plugin.getUserManager().getUser(player);
    if (player == null || user.getArena() == null || !(user.getKit() instanceof TerminatorKit)) {
      return;
    }
    String key = TerminatorAugment.METADATA_KEY + "VITALITY";
    if (!player.hasMetadata(key)) {
      return;
    }
    int count = player.getMetadata(key).get(0).asInt() + 1;
    player.setMetadata(key, new FixedMetadataValue(plugin, count));
    if (count >= TerminatorKit.Settings.AUGMENT_VITALITY_TRIGGER_COUNT.getForArenaState((Arena) user.getArena())) {
      player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 30, 0));
      player.setMetadata(key, new FixedMetadataValue(plugin, 0));
    }
  }

  @EventHandler
  public void onAdaptiveShieldsTrigger(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }
    if (!(event.getDamager() instanceof TNTPrimed primed)) {
      return;
    }
    if (!primed.hasMetadata("VD_PRIMED_TNT")) {
      return;
    }
    if (!player.hasMetadata(TerminatorAugment.METADATA_KEY + "ADAPTIVE_SHIELDS")) {
      return;
    }
    User user = plugin.getUserManager().getUser(player);
    if (user.getArena() == null || !(user.getKit() instanceof TerminatorKit)) {
      return;
    }
    event.setDamage(event.getDamage() - (event.getDamage() * TerminatorKit.Settings.AUGMENT_ADAPTIVE_SHIELDS_REDUCE_PERCENT.getForArenaState((Arena) user.getArena())));
  }

  @EventHandler
  public void onSteadyScalingDamage(EntityDamageEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }
    if (!player.hasMetadata(TerminatorAugment.METADATA_KEY + "STEADY_SCALING")) {
      return;
    }
    User user = plugin.getUserManager().getUser(player);
    int count = player.getMetadata(TerminatorAugment.METADATA_KEY + "STEADY_SCALING").get(0).asInt();
    if (user.getArena() == null || !(user.getKit() instanceof TerminatorKit) || count == 0) {
      return;
    }
    event.setDamage(event.getDamage() + (event.getDamage() * (count / 100.0)));
  }

  @EventHandler
  public void onSteadyScalingIncrease(EntityDeathEvent event) {
    if (!NewCreatureUtils.isEnemy(event.getEntity())) {
      return;
    }
    Player player = event.getEntity().getKiller();
    User user = plugin.getUserManager().getUser(player);
    if (player == null || user.getArena() == null || !(user.getKit() instanceof TerminatorKit)) {
      return;
    }
    String key = TerminatorAugment.METADATA_KEY + "STEADY_SCALING_COUNT";
    if (!player.hasMetadata(key)) {
      return;
    }
    int count = player.getMetadata(key).get(0).asInt() + 1;
    player.setMetadata(key, new FixedMetadataValue(plugin, count));
    if (count >= 50) {
      int steadyScaling = 0;
      String scalingKey = TerminatorAugment.METADATA_KEY + "STEADY_SCALING";
      if (player.hasMetadata(scalingKey)) {
        steadyScaling = Math.min(player.getMetadata(scalingKey).get(0).asInt() + 1, 30);
      }
      if (steadyScaling <= 30) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&e&lSTEADY SCALING &7has increased to &e" + steadyScaling + " stacks"));
      }
      player.setMetadata(key, new FixedMetadataValue(plugin, 0));
      player.setMetadata(scalingKey, new FixedMetadataValue(plugin, steadyScaling));
    }
  }

  //todo synergy - expanded aid

  @EventHandler
  public void onDecapitationTrigger(EntityDeathEvent event) {
    if (!NewCreatureUtils.isEnemy(event.getEntity())) {
      return;
    }
    Player player = event.getEntity().getKiller();
    User user = plugin.getUserManager().getUser(player);
    if (player == null || user.getArena() == null || !(user.getKit() instanceof TerminatorKit)) {
      return;
    }
    String key = TerminatorAugment.METADATA_KEY + "DECAPITATION";
    if (!player.hasMetadata(key)) {
      return;
    }
    double chance = TerminatorKit.Settings.AUGMENT_DECAPITATION_TRIGGER_CHANCE.getForArenaState((Arena) user.getArena());
    if (chance > ThreadLocalRandom.current().nextInt(0, 100)) {
      return;
    }
    Location location = event.getEntity().getLocation().add(0, 0.5, 0);
    ArmorStandHologram hologram = new ArmorStandHologram(location)
      .appendItem(XMaterial.ZOMBIE_HEAD.parseItem())
      .appendLine(ChatColor.translateAlternateColorCodes('&', "&e&l-10% COOLDOWN"));
    hologram.setPickupHandler(target -> {
      if (!user.getArena().equals(plugin.getArenaRegistry().getArena(target))) {
        return;
      }
      XSound.ENTITY_PLAYER_LEVELUP.play(target, 1, 0);
      XSound.ENTITY_VILLAGER_YES.play(target);
      hologram.delete();
      double neverdeathCd = user.getCooldown("terminator_neverdeath");
      user.setCooldown("terminator_neverdeath", Math.max(neverdeathCd - (neverdeathCd * 0.1), 0));
      VersionUtils.setMaterialCooldown(user.getPlayer(), XMaterial.CHARCOAL.parseMaterial(), (int) user.getCooldown("terminator_neverdeath"));
      double overchargeCd = user.getCooldown("terminator_overcharge");
      user.setCooldown("terminator_overcharge", Math.max(overchargeCd - (overchargeCd * 0.1), 0));
      VersionUtils.setMaterialCooldown(user.getPlayer(), XMaterial.TRIPWIRE_HOOK.parseMaterial(), (int) user.getCooldown("terminator_overcharge"));
    });
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (!hologram.isDeleted()) {
        hologram.delete();
      }
    }, 20 * 10);
  }

  @EventHandler
  public void onSafetyProtocolsTrigger(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player)) {
      return;
    }
    User user = plugin.getUserManager().getUser(player);
    if (user.getArena() == null || !(user.getKit() instanceof TerminatorKit)) {
      return;
    }
    if (!player.hasMetadata(TerminatorAugment.METADATA_KEY + "SAFETY_PROTOCOLS")) {
      return;
    }
    if (player.getHealth() > 6) {
      return;
    }
    event.setDamage(event.getDamage() + (event.getDamage() * TerminatorKit.Settings.AUGMENT_SAFETY_PROTOCOLS_DAMAGE_MULTIPLIER.getForArenaState((Arena) user.getArena())));
  }

  @EventHandler
  public void onReinforcedLearningTrigger(EntityDeathEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }
    User user = plugin.getUserManager().getUser(player);
    if (user.getArena() == null || !(user.getKit() instanceof TerminatorKit)) {
      return;
    }
    String key = TerminatorAugment.METADATA_KEY + "REINFORCED_LEARNING";
    if (!player.hasMetadata(key)) {
      player.setMetadata(key, new FixedMetadataValue(plugin, 1));
      return;
    }
    int count = Math.min(player.getMetadata(key).get(0).asInt() + 1, 15);
    player.setMetadata(key, new FixedMetadataValue(plugin, count));
  }

  @EventHandler
  public void onReinforcedLearningDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player)) {
      return;
    }
    User user = plugin.getUserManager().getUser(player);
    if (user.getArena() == null || !(user.getKit() instanceof TerminatorKit)) {
      return;
    }
    String key = TerminatorAugment.METADATA_KEY + "REINFORCED_LEARNING";
    if (!player.hasMetadata(key)) {
      return;
    }
    int count = player.getMetadata(key).get(0).asInt();
    event.setDamage(event.getDamage() + (event.getDamage() * (count / 100.0)));
  }

  @EventHandler
  public void onBleedingVitalsDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player)) {
      return;
    }
    User user = plugin.getUserManager().getUser(player);
    if (user.getArena() == null || !(user.getKit() instanceof TerminatorKit)) {
      return;
    }
    if (!player.hasMetadata(TerminatorAugment.METADATA_KEY + "BLEEDING_VITALS")) {
      return;
    }
    if (!isCritical(player)) {
      return;
    }
    event.setDamage(event.getDamage() + (event.getDamage() * TerminatorKit.Settings.AUGMENT_BLEEDING_VITALS_MULTIPLIER.getForArenaState((Arena) user.getArena())));
  }

  private boolean isCritical(Player player) {
    return player.getFallDistance() > 0.0F &&
      !player.isOnGround() &&
      !player.isInsideVehicle() &&
      !player.hasPotionEffect(PotionEffectType.BLINDNESS) &&
      player.getLocation().getBlock().getType() != Material.LADDER &&
      player.getLocation().getBlock().getType() != Material.VINE;
  }

  //todo poplust copycat

  @EventHandler
  public void onWillOfSteelDamage(EntityDamageByEntityEvent event) {
    if (!(event.getDamager() instanceof Player player)) {
      return;
    }
    User user = plugin.getUserManager().getUser(player);
    if (user.getArena() == null || !(user.getKit() instanceof TerminatorKit)) {
      return;
    }
    if (!player.hasMetadata(TerminatorAugment.METADATA_KEY + "WILL_OF_STEEL")) {
      return;
    }
    if (!NewCreatureUtils.isEnemy(event.getEntity()) || !event.getEntity().hasMetadata("VD_STUNNED")) {
      return;
    }
    event.setDamage(event.getDamage() + (event.getDamage() * TerminatorKit.Settings.AUGMENT_WILL_OF_STEEL_MULTIPLIER.getForArenaState((Arena) user.getArena())));
  }

  @EventHandler
  public void onNeuronalRepurposeTrigger(EntityDeathEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }
    User user = plugin.getUserManager().getUser(player);
    if (user.getArena() == null || !(user.getKit() instanceof TerminatorKit)) {
      return;
    }
    if (!player.hasMetadata(TerminatorAugment.METADATA_KEY + "NEURONAL_REPURPOSE")) {
      return;
    }
    if (!NewCreatureUtils.isEnemy(event.getEntity()) || ThreadLocalRandom.current().nextInt() > 5) {
      return;
    }
    NewCreatureUtils.doFrenzyEnemy((Creature) event.getEntity(), 5);
  }

  @EventHandler
  public void onPsionicBlowTrigger(EntityDamageByEntityEvent event) {
    if (!(event.getEntity() instanceof Player player)) {
      return;
    }
    User user = plugin.getUserManager().getUser(player);
    if (user == null || user.getArena() == null || user.isSpectator() || !(user.getKit() instanceof TerminatorKit)) {
      return;
    }
    if (user.getCooldown("terminator_psionic_blow") > 0) {
      return;
    }
    if (player.getHealth() - event.getDamage() <= 0) {
      new MessageBuilder("&aPsionic blow has saved you!").send(player);
      user.setCooldown("terminator_psionic_blow", 20);
      event.setDamage(0);
      player.setHealth(1.5);
      player.playEffect(EntityEffect.valueOf("TOTEM_RESURRECT"));
      for (Entity entity : player.getNearbyEntities(6, 6, 6)) {
        if (!NewCreatureUtils.isEnemy(entity)) {
          continue;
        }
        if (!entity.hasMetadata("VD_UNSTUNNABLE")) {
          Vector vector = entity.getLocation().toVector().subtract(player.getLocation().toVector()).normalize();
          vector.add(new Vector(0, 0.1, 0));
          entity.setVelocity(vector.multiply(1.6));
        }
      }
    }
  }

  /*
  todo
  EXPANDED_AID
  POPLUST_COPYCAT
  ULTRAVISION
   */

}
