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

import fr.skytasul.guardianbeam.Laser;
import lombok.SneakyThrows;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewCreatureUtils;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.VDEnemy;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.VDEnemyBuilder;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.VdEnemyAbilityHandler;
import plugily.projects.villagedefense.arena.villager.VillagerAiAnimations;

public class GravityZombie implements RegistrableZombie {

  private final Main plugin = JavaPlugin.getPlugin(Main.class);

  @Override
  public VDEnemy getEnemy() {
    return new VDEnemyBuilder("VD_GRAVITY_ZOMBIE")
      .withName("Gravity Zombie")
      .withWaveMinimum(0)
      .withWaveMaximum(50)
      .withFullArmor(VDEnemyBuilder.ArmorType.LEATHER, 0, 1)
      .withScalingHealth(wave -> 25.0)
      .onSpawn(creature -> {
        creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.14);
      })
      .onDamageByEntity((event, arena) -> {
        //long distance player marking
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
        if (value >= 2) {
          VdEnemyAbilityHandler.resetVariable(entity, "VD_ENEMY_ABILITY_RAGE");
          doPrepareSkill((Creature) entity, player);
        }
      }).build();
  }

  @SneakyThrows
  private void doPrepareSkill(Creature creature, Player player) {
    if (creature.isDead() || creature.getHealth() <= 0) {
      return;
    }
    if (creature.hasMetadata("VD_ENEMY_ABILITY_CASTING")) {
      return;
    }
    creature.setAI(false);
    creature.setMetadata("VD_ENEMY_ABILITY_CASTING", new FixedMetadataValue(plugin, true));
    Laser laser = new Laser.GuardianLaser(creature.getEyeLocation(), player, 4, 100);
    NewCreatureUtils.doSetCustomNameTemporarily(creature, ChatColor.translateAlternateColorCodes('&', "&b&lCHANNELING"), 20 * 4, () -> {
      if (creature.isDead()) {
        return;
      }
      creature.removeMetadata("VD_ENEMY_ABILITY_CASTING", plugin);
      creature.setAI(true);
    });
    laser.start(plugin);
    new BukkitRunnable() {
      int ticks = 0;

      @Override
      public void run() {
        if (creature.isDead() || ticks >= 20 * 4) {
          if (laser.isStarted()) {
            laser.stop();
          }
          this.cancel();
          return;
        }
        double distance = player.getLocation().distance(creature.getLocation());
        if (distance < 4.5) {
          throwPlayerFromLocation(player, creature.getLocation());
        } else if (distance > 7) {
          throwPlayerToLocation(player, creature.getLocation());
        }

        creature.swingMainHand();
        VillagerAiAnimations.makeEntityLookAt(creature, player.getLocation());
        ticks += 5;
      }
    }.runTaskTimer(plugin, 5, 5);
  }

  private void throwPlayerFromLocation(Player player, Location targetLocation) {
    Location playerLocation = player.getLocation();
    Vector direction = playerLocation.toVector().subtract(targetLocation.toVector()).normalize();
    Vector velocity = direction.multiply(0.5);
    player.setVelocity(velocity);
  }

  private void throwPlayerToLocation(Player player, Location targetLocation) {
    Location playerLocation = player.getLocation();
    Vector direction = targetLocation.toVector().subtract(playerLocation.toVector()).normalize();
    Vector velocity = direction.multiply(0.5);
    player.setVelocity(velocity);
  }

}
