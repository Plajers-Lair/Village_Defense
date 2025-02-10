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

package plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.type;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import plugily.projects.minigamesbox.classic.utils.version.xseries.ParticleDisplay;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XParticle;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewCreatureUtils;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.VDEnemy;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.VDEnemyBuilder;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.VdEnemyAbilityHandler;
import plugily.projects.villagedefense.arena.villager.VillagerAiAnimations;

import java.util.concurrent.ThreadLocalRandom;

public class BlinkerZombie implements RegistrableZombie {

  private final Main plugin = JavaPlugin.getPlugin(Main.class);

  @Override
  public VDEnemy getEnemy() {
    return new VDEnemyBuilder("VD_BLINKER_ZOMBIE")
      .withName("Blinker Zombie")
      .withWaveMinimum(0)
      .withWaveMaximum(50)
      .withFullArmor(VDEnemyBuilder.ArmorType.LEATHER, 0, 1)
      .withScalingHealth(wave -> 25.0)
      .onSpawn(creature -> {
        creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.2);
      })
      .onDamageByEntity((event, arena) -> {
        Player player;
        if (!(event.getDamager() instanceof Player damager)) {
          if (event.getDamager() instanceof Arrow arrow) {
            player = (Player) arrow.getShooter();
          } else {
            return;
          }
        } else {
          player = damager;
        }
        Entity entity = event.getEntity();
        int value = VdEnemyAbilityHandler.increaseVariable(entity, "VD_ENEMY_ABILITY_RAGE");
        if (value >= 6) {
          if (value % 3 == 0) {
            doPrepareBlink((Creature) entity, player, player.getLocation().distanceSquared(entity.getLocation()) > 8);
          }
        } else {
          if (value % 2 == 0) {
            doPrepareBlink((Creature) entity, player, player.getLocation().distanceSquared(entity.getLocation()) > 8);
          }
        }
      }).build();
  }

  private void doPrepareBlink(Creature creature, Player player, boolean fast) {
    if (creature.isDead() || creature.getHealth() <= 0) {
      return;
    }
    if (creature.hasMetadata("VD_ENEMY_ABILITY_CASTING")) {
      return;
    }
    //save location before blink for a chance to dodge
    Location loc = findNearestAirBehind(player);
    if (loc == null) {
      loc = player.getLocation();
    }
    final Location finalLoc = loc;
    NewCreatureUtils.doSetCustomNameTemporarily(creature, ChatColor.translateAlternateColorCodes('&', "&9&lBLINKING"), (fast ? 7 : 15), () -> {
      if (creature.isDead()) {
        return;
      }
      creature.setAI(true);
      creature.removeMetadata("VD_ENEMY_ABILITY_CASTING", plugin);
      creature.teleport(finalLoc);
      creature.setTarget(player);
      creature.getWorld().playSound(creature.getLocation(), XSound.ENTITY_ENDERMAN_TELEPORT.parseSound(), 1.0F, 1.0F);
    });
    creature.setMetadata("VD_ENEMY_ABILITY_CASTING", new FixedMetadataValue(plugin, true));
    creature.setAI(false);
    creature.getWorld().playSound(creature.getLocation(), XSound.ENTITY_EVOKER_PREPARE_SUMMON.parseSound(), 1.0F, 1.6F);
    new BukkitRunnable() {
      final Location location = creature.getLocation();
      int ticks = 0;

      @Override
      public void run() {
        if (ticks >= (fast ? 7 : 15) || creature.isDead()) {
          cancel();
          return;
        }
        if (ticks % (fast ? 2 : 5) == 0) {
          creature.swingMainHand();
          VillagerAiAnimations.makeEntityLookAt(creature, location
            .clone()
            .add(
              location.getDirection().getX() * 0.5,
              -0.15,
              location.getDirection().getZ() * 0.5
            ));
        }
        XParticle.circle(1.4, 13, ParticleDisplay.simple(location, Particle.ENCHANTMENT_TABLE));
        location.setY(location.getY() + (fast ? 0.3 : 0.15));
        ticks += 1;
      }
    }.runTaskTimer(plugin, 0, 1);
  }

  private Location findNearestAirBehind(Player player) {
    Location loc = player.getLocation();
    Vector direction = loc.getDirection().setY(0).normalize().multiply(-1);

    int offset = ThreadLocalRandom.current().nextInt(0, 1);
    for (int i = 2 + offset; i <= 5 + offset; i++) {
      Location checkLoc = loc.clone().add(direction.clone().multiply(i));
      if (isSafeLocation(checkLoc)) {
        return checkLoc;
      }
    }

    return null;
  }

  private boolean isSafeLocation(Location loc) {
    return loc.getBlock().getType() == Material.AIR &&
      loc.clone().add(0, 1, 0).getBlock().getType() == Material.AIR &&
      loc.clone().add(0, -1, 0).getBlock().getType().isSolid();
  }

}
