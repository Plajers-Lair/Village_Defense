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

package plugily.projects.villagedefense.arena.powerup;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XMaterial;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XParticle;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.handlers.hologram.ArmorStandHologram;
import plugily.projects.villagedefense.handlers.upgrade.NewEntityUpgradeManager;
import plugily.projects.villagedefense.kits.utils.KitSpecifications;

public class IronDeliveryPowerup implements Powerup {

  private final Main plugin;

  public IronDeliveryPowerup(Main plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean canSpawn(Arena arena) {
    return arena.getWave() >= 5;
  }

  @Override
  public void spawn(Arena arena, Location location) {
    ArmorStandHologram hologram = new ArmorStandHologram(location.clone().add(0, -1.35, 0))
      .appendItem(XMaterial.IRON_BLOCK.parseItem())
      .appendLine(ChatColor.translateAlternateColorCodes('&', "&e&lIRON DELIVERY"));

    hologram.setPickupHandler(player -> {
      if (!plugin.getArenaRegistry().getArena(player).equals(arena)) {
        return;
      }
      XSound.ENTITY_PLAYER_LEVELUP.play(player, 1, 0);
      XSound.ENTITY_IRON_GOLEM_DEATH.play(arena.getStartLocation(), 1, 0);
      int count;
      KitSpecifications.GameTimeState state = KitSpecifications.getTimeState(arena);
      if (state == KitSpecifications.GameTimeState.EARLY) {
        count = 1;
      } else if (state == KitSpecifications.GameTimeState.MID) {
        count = 2;
      } else {
        count = 3;
      }
      for (int i = 0; i < count; i++) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
          XSound.ENTITY_IRON_GOLEM_REPAIR.play(arena.getStartLocation(), 1, 1.25f);
          Creature golem = arena.spawnGolemForce(arena.getStartLocation(), player);
          if (golem == null) {
            return;
          }
          golem.setMetadata(NewEntityUpgradeManager.UPGRADES_DISABLED_METADATA, new FixedMetadataValue(plugin, true));
          NewEntityUpgradeManager upgradeManager = plugin.getEntityUpgradeManager();
          upgradeManager.getRegisteredUpgrades()
            .stream()
            .filter(u -> u.getApplicableEntity() == EntityType.IRON_GOLEM)
            .forEach(upgrade -> upgradeManager.applyUpgradeSilent(golem, player, upgrade));
          ArmorStandHologram golemHologram = new ArmorStandHologram(golem.getLocation())
            .appendLine(ChatColor.translateAlternateColorCodes('&', "&e&lIRON DELIVERY"));
          golem.getWorld().playSound(golem.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 0.5f);

          new BukkitRunnable() {
            int tick = 0;
            boolean toggle = false;

            @Override
            public void run() {
              if (tick == 400 || golemHologram.isDeleted() || golemHologram.getArmorStands().isEmpty()) {
                World world = golem.getLocation().getWorld();
                world.spawnParticle(Particle.EXPLOSION_LARGE, golem.getLocation().clone().add(0, 0.5, 0), 1);
                world.playSound(golem.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 0.5f);
                arena.removeIronGolem((IronGolem) golem);
                golemHologram.delete();
                cancel();
                return;
              }
              ArmorStand stand = golemHologram.getArmorStands().get(0);
              stand.teleport(golem.getLocation().clone().add(0, 1.1, 0));
              //postpone countdown until wave starts but still teleport moving holograms
              if (!arena.isFighting()) {
                return;
              }
              int modulo = tick < 100 ? 10 : tick > 350 ? 3 : 5;
              if (tick % modulo == 0) {
                String message;
                if (toggle) {
                  message = ChatColor.translateAlternateColorCodes('&', "&c&lIRON DELIVERY (" + ((400 - tick) / 20) + "s)");
                } else {
                  message = ChatColor.translateAlternateColorCodes('&', "&e&lIRON DELIVERY (" + ((400 - tick) / 20) + "s)");
                }
                toggle = !toggle;
                stand.setCustomName(message);
              }
              tick++;
            }
          }.runTaskTimer(plugin, 1, 1);
          Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!golemHologram.isDeleted()) {
              golemHologram.delete();
            }
          }, /* remove after 40 seconds to prevent staying even if arena is finished */ 20 * 40);
        }, 15 * (i + 1));
      }
      hologram.delete();
    });

    new BukkitRunnable() {
      boolean toggle = true;
      boolean grounded = false;
      boolean flyReverse = false;
      int tick = 0;

      @Override
      public void run() {
        if (tick == 200 || hologram.isDeleted() || hologram.getArmorStands().isEmpty()) {
          hologram.getLocation().getWorld().spawnParticle(XParticle.getParticle("EXPLOSION_HUGE"), hologram.getLocation(), 1);
          hologram.delete();
          cancel();
          return;
        }
        if (hologram.getEntityItem().isOnGround() && !grounded) {
          grounded = true;
          hologram.getEntityItem().setGravity(false);
          hologram.getEntityItem().teleport(hologram.getEntityItem().getLocation().add(0, 0.75, 0));
        }
        if (grounded) {
          hologram.getEntityItem().setGravity(true);
          Location newLocation = hologram.getEntityItem().getLocation().clone().add(0, 0.015 * (flyReverse ? -1 : 1), 0);
          hologram.getEntityItem().teleport(newLocation);
          hologram.getEntityItem().setGravity(false);
          if (tick % 60 == 0) {
            flyReverse = !flyReverse;
          }
        }
        ArmorStand stand = hologram.getArmorStands().get(0);
        int modulo = tick < 100 ? 10 : tick > 150 ? 3 : 5;
        if (tick % modulo == 0) {
          String message;
          if (toggle) {
            message = ChatColor.translateAlternateColorCodes('&', "&c&lIRON DELIVERY (" + ((200 - tick) / 20) + "s)");
          } else {
            message = ChatColor.translateAlternateColorCodes('&', "&e&lIRON DELIVERY (" + ((200 - tick) / 20) + "s)");
          }
          toggle = !toggle;
          stand.setCustomName(message);
        }
        if (tick % 10 == 0) {
          for (int i = 0; i < 2; i++) {
            Item item = hologram.getLocation().getWorld().dropItemNaturally(stand.getLocation().clone().add(0, 0.5, 0), XMaterial.IRON_INGOT.parseItem());
            item.setInvulnerable(true);
            item.setPickupDelay(Integer.MAX_VALUE);
            item.setGravity(true);
            item.setCustomNameVisible(false);
            Bukkit.getScheduler().runTaskLater(plugin, () -> item.remove(), 15);
          }
        }
        stand.teleport(hologram.getEntityItem().getLocation().clone().add(0, -1.25, 0));
        tick++;
      }
    }.runTaskTimer(plugin, 1, 1);
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (!hologram.isDeleted()) {
        hologram.delete();
      }
    }, /* remove after 40 seconds to prevent staying even if arena is finished */ 20 * 40);
  }
}
