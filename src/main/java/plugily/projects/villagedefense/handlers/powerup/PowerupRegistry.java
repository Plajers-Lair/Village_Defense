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

package plugily.projects.villagedefense.handlers.powerup;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import plugily.projects.commonsbox.minecraft.compat.VersionUtils;
import plugily.projects.commonsbox.minecraft.compat.xseries.XMaterial;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.api.event.player.VillagePlayerPowerupPickupEvent;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.ArenaRegistry;
import plugily.projects.villagedefense.arena.ArenaUtils;
import plugily.projects.villagedefense.handlers.ChatManager;
import plugily.projects.villagedefense.handlers.language.Messages;
import plugily.projects.villagedefense.utils.Debugger;

/**
 * @author Plajer
 * <p>
 * Created at 15.01.2019
 */
public class PowerupRegistry {

  private final Random random = new Random();
  private final List<BasePowerup> registeredPowerups = new ArrayList<>();
  private boolean enabled = false;
  private Main plugin;

  public PowerupRegistry(Main plugin) {
    if(!plugin.getConfig().getBoolean("Powerups.Enabled", true)) {
      return;
    }
    if(!plugin.getServer().getPluginManager().isPluginEnabled("HolographicDisplays")) {
      Debugger.debug(Level.WARNING, "[PowerupRegistry] Holographic Displays dependency not found, disabling");
      return;
    }
    enabled = true;
    this.plugin = plugin;
    registerPowerups();
    if(registeredPowerups.isEmpty()) {
      Debugger.debug(Level.WARNING, "[PowerupRegistry] Disabling power up module, all power ups disabled");
      enabled = false;
    }
  }

  private void registerPowerups() {
    Debugger.debug("[PowerupRegistry] Registering power ups");
    long start = System.currentTimeMillis();

    ChatManager chatManager = plugin.getChatManager();

    if (plugin.getConfig().getBoolean("Powerups.List.Map-Clean", true)) {
      registerPowerup(new Powerup("MAP_CLEAN", chatManager.colorMessage(Messages.POWERUPS_MAP_CLEAN_NAME),
              chatManager.colorMessage(Messages.POWERUPS_MAP_CLEAN_DESCRIPTION), XMaterial.BLAZE_POWDER, pickup -> {
        ArenaUtils.removeSpawnedZombies(pickup.getArena());
        pickup.getArena().getEnemies().clear();

        for (Player p : pickup.getArena().getPlayers()) {
          VersionUtils.sendTitles(p, pickup.getPowerup().getName(), pickup.getPowerup().getDescription(), 5, 30, 5);
        }
      }));
    }

    if (plugin.getConfig().getBoolean("Powerups.List.Double-Damage-For-Players.Enabled", true)) {
      registerPowerup(new Powerup("DOUBLE_DAMAGE", chatManager.colorMessage(Messages.POWERUPS_DOUBLE_DAMAGE_NAME),
              chatManager.colorMessage(Messages.POWERUPS_DOUBLE_DAMAGE_DESCRIPTION), XMaterial.REDSTONE, pickup -> {
        int damageTime = plugin.getConfig().getInt("Powerups.List.Double-Damage-For-Players.Time", 15);

        for (Player p : pickup.getArena().getPlayers()) {
          p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * damageTime, 0, false, false));
        }

        String subTitle = pickup.getPowerup().getDescription();
        subTitle = StringUtils.replace(subTitle, "%time%", Integer.toString(damageTime));

        for (Player p : pickup.getArena().getPlayers()) {
          VersionUtils.sendTitles(p, pickup.getPowerup().getName(), subTitle, 5, 30, 5);
        }
      }));
    }

    if (plugin.getConfig().getBoolean("Powerups.List.Golem-Raid.Enabled", true)) {
      registerPowerup(new Powerup("GOLEM_RAID", chatManager.colorMessage(Messages.POWERUPS_GOLEM_RAID_NAME),
              chatManager.colorMessage(Messages.POWERUPS_GOLEM_RAID_DESCRIPTION), XMaterial.GOLDEN_APPLE, pickup -> {
        for (int i = 0; i < plugin.getConfig().getInt("Powerups.List.Golem-Raid.Golems-Amount", 3); i++) {
          pickup.getArena().spawnGolem(pickup.getArena().getStartLocation(), pickup.getPlayer());
        }

        for (Player p : pickup.getArena().getPlayers()) {
          VersionUtils.sendTitles(p, pickup.getPowerup().getName(), pickup.getPowerup().getDescription(), 5, 30, 5);
        }
      }));
    }

    if (plugin.getConfig().getBoolean("Powerups.List.Healing-For-Players.Enabled", true)) {
      registerPowerup(new Powerup("HEALING", chatManager.colorMessage(Messages.POWERUPS_HEALING_NAME),
              chatManager.colorMessage(Messages.POWERUPS_HEALING_DESCRIPTION), XMaterial.IRON_INGOT, pickup -> {
        int timeHealing = plugin.getConfig().getInt("Powerups.List.Healing-For-Players.Time-Of-Healing", 10);

        String subTitle = pickup.getPowerup().getDescription();
        subTitle = StringUtils.replace(subTitle, "%time%", Integer.toString(timeHealing));

        for (Player p : pickup.getArena().getPlayers()) {
          p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * timeHealing, 0, false, false));
          VersionUtils.sendTitles(p, pickup.getPowerup().getName(), subTitle, 5, 30, 5);
        }
      }));
    }

    if (plugin.getConfig().getBoolean("Powerups.List.One-Shot-One-Kill.Enabled", true)) {
      registerPowerup(new Powerup("ONE_SHOT_ONE_KILL", chatManager.colorMessage(Messages.POWERUPS_ONE_SHOT_ONE_KILL_NAME),
              chatManager.colorMessage(Messages.POWERUPS_ONE_SHOT_ONE_KILL_DESCRIPTION), XMaterial.DIAMOND_SWORD, pickup -> {
        int oneShotKillTime = plugin.getConfig().getInt("Powerups.List.One-Shot-One-Kill.Time", 15);

        String subTitle = pickup.getPowerup().getDescription();
        subTitle = StringUtils.replace(subTitle, "%time%", Integer.toString(oneShotKillTime));

        for (Player p : pickup.getArena().getPlayers()) {
          p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 20 * oneShotKillTime, 1000, false, false));
          VersionUtils.sendTitles(p, pickup.getPowerup().getName(), subTitle, 5, 30, 5);
        }
      }));
    }

    Debugger.debug("[PowerupRegistry] Registered all powerups took {0}ms", System.currentTimeMillis() - start);
  }

  /**
   * @return random powerup from list of registered ones
   */
  public BasePowerup getRandomPowerup() {
    return registeredPowerups.get(registeredPowerups.size() == 1 ? 0 : random.nextInt(registeredPowerups.size()));
  }

  public void spawnPowerup(Location loc, Arena arena) {
    if(!enabled || ThreadLocalRandom.current().nextDouble(0.0, 100.0) > plugin.getConfig().getDouble("Powerups.Drop-Chance", 1.0)) {
      return;
    }

    final BasePowerup powerup = getRandomPowerup();
    final Hologram hologram = HologramsAPI.createHologram(plugin, loc.clone().add(0.0, 1.2, 0.0));
    hologram.appendTextLine(powerup.getName());
    hologram.appendItemLine(powerup.getMaterial().parseItem()).setPickupHandler(player -> {
      if(ArenaRegistry.getArena(player) != arena) {
        return;
      }

      VillagePlayerPowerupPickupEvent event = new VillagePlayerPowerupPickupEvent(arena, player, powerup);
      Bukkit.getPluginManager().callEvent(event);
      if (event.isCancelled()) {
        return;
      }

      powerup.getOnPickup().accept(new PowerupPickupHandler(powerup, arena, player));
      hologram.delete();
    });
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if(!hologram.isDeleted()) {
        hologram.delete();
      }
    }, /* remove after 40 seconds to prevent staying even if arena is finished */ 20 * 40);
  }

  /**
   * Attempts to register a powerup
   *
   * @param powerup powerup to register
   * @throws IllegalArgumentException if power-up with same ID currently exist
   */
  public void registerPowerup(BasePowerup powerup) {
    for(BasePowerup pwup : registeredPowerups) {
      if(pwup.getId().equals(powerup.getId())) {
        throw new IllegalArgumentException("Cannot register new power-up with same ID!");
      }
    }
    registeredPowerups.add(powerup);
  }

  /**
   * Unregisters target powerup from registry
   *
   * @param powerup powerup to remove
   */
  public void unregisterPowerup(Powerup powerup) {
    registeredPowerups.remove(powerup);
  }

}
