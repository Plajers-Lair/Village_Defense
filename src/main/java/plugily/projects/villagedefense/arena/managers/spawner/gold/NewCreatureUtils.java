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
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.VDEnemy;
import plugily.projects.villagedefense.arena.midwave.PinataZombieEvent;
import plugily.projects.villagedefense.arena.midwave.RottenOfferEvent;
import plugily.projects.villagedefense.arena.villager.CompletionCallback;
import plugily.projects.villagedefense.handlers.upgrade.EntityUpgrade;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class NewCreatureUtils {

  private static String[] villagerNames = ("Jagger,Kelsey,Kelton,Haylie,Harlow,Howard,Wulffric,Winfred,Ashley,Bailey,Beckett,Alfredo,Alfred,Adair,Edgar,ED,Eadwig,Edgaras,Buckley,Stanley,Nuffley,"
    + "Mary,Jeffry,Rosaly,Elliot,Harry,Sam,Rosaline,Tom,Ivan,Kevin,Adam,Emma,Mira,Jeff,Isac,Nico").split(",");
  private static Main plugin;

  private NewCreatureUtils() {
  }

  public static void init(Main plugin) {
    NewCreatureUtils.plugin = plugin;
    villagerNames = new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_VILLAGER_NAMES").asKey().build().split(",");
  }

  /**
   * Check if the given entity is a arena's enemy.
   * We define the enemy as it's not the player, the villager, the wolf and the iron golem
   *
   * @param entity the entity
   * @return true if it is
   */
  //todo improve detection
  public static boolean isEnemy(Entity entity) {
    return entity instanceof Creature
      && !(entity instanceof Player || entity instanceof Villager || entity instanceof Wolf
      || entity instanceof IronGolem || entity.hasMetadata(PinataZombieEvent.PINATA_METADATA)
      || entity.hasMetadata(RottenOfferEvent.ROTTEN_SALE_METADATA));
  }

  public static void doFrenzyEnemy(Creature creature, int seconds) {
    if (creature.hasMetadata("VD_FRENZY") || creature.hasMetadata("VD_UNSTUNNABLE") || creature.hasMetadata("VD_ENEMY_ABILITY_CASTING")) {
      return;
    }
    creature.setMetadata("VD_FRENZY", new FixedMetadataValue(plugin, true));
    doSetCustomNameTemporarily(creature, new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_ZOMBIE_FRENZY_NAME").asKey().build(), seconds * 20, () -> {
      creature.removeMetadata("VD_FRENZY", plugin);
      creature.setTarget(null);
    });
    for (Entity entity : creature.getNearbyEntities(10, 10, 10)) {
      if (isEnemy(entity)) {
        Creature nearbyCreature = (Creature) entity;
        if (nearbyCreature.hasMetadata(NewEnemySpawnerManager.CREATURE_ID_METADATA)) {
          VDEnemy enemy = getEnemyFromCreature(nearbyCreature);
          if (enemy != null) {
            nearbyCreature.setTarget(creature.getTarget());
          }
        }
      }
    }
  }

  public static void doStunEnemy(Creature creature, int seconds) {
    if (creature.hasMetadata("VD_STUNNED") || creature.hasMetadata("VD_UNSTUNNABLE") || creature.hasMetadata("VD_ENEMY_ABILITY_CASTING")) {
      return;
    }
    creature.setMetadata("VD_STUNNED", new FixedMetadataValue(plugin, true));
    creature.setAI(false);
    doSetCustomNameTemporarily(creature, new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_ZOMBIE_STUNNED_NAME").asKey().build(), seconds * 20, () -> {
      creature.setAI(true);
      creature.removeMetadata("VD_STUNNED", plugin);
    });
  }

  public static void doSetCustomNameTemporarily(Creature creature, String customName, int ticks, CompletionCallback completionCallback) {
    final String cachedName = creature.getMetadata(NewEnemySpawnerManager.CREATURE_CUSTOM_NAME_METADATA).get(0).asString();
    creature.setMetadata(NewEnemySpawnerManager.CREATURE_CUSTOM_NAME_METADATA, new FixedMetadataValue(plugin, customName));
    creature.setCustomName(getHealthNameTag(creature));
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      creature.setMetadata(NewEnemySpawnerManager.CREATURE_CUSTOM_NAME_METADATA, new FixedMetadataValue(plugin, cachedName));
      creature.setCustomName(getHealthNameTag(creature));
      completionCallback.onComplete();
    }, ticks);
  }

  public static void doMarkPetDead(Creature creature) {
    if (creature.hasMetadata("VD_PET_DEAD")) {
      return;
    }
    creature.setMetadata("VD_PET_DEAD", new FixedMetadataValue(plugin, true));
    creature.setAI(false);
    creature.setCustomName(ChatColor.translateAlternateColorCodes('&', "&8&lDEAD"));
  }

  public static String getHealthNameTag(Creature creature) {
    return getHealthNameTagPreDamage(creature, 0);
  }

  /**
   * In damage events, health is modified after all events are listened to
   * we must apply health bar change pre damage event
   *
   * @param creature target to generate health bar for
   * @param damage   final damage taken by enemy before all events have finished
   * @return health bar adjusted to the events' damage
   */
  public static String getHealthNameTagPreDamage(Creature creature, double damage) {
    double health = creature.getHealth() - damage;
    if (health < 0) {
      health = 0;
    }
    double maxHealth = creature.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
    ChatColor hpColor;
    if (health >= maxHealth * 0.75) {
      hpColor = ChatColor.GREEN;
    } else if (health >= maxHealth * 0.5) {
      hpColor = ChatColor.GOLD;
    } else if (health >= maxHealth * 0.25) {
      hpColor = ChatColor.YELLOW;
    } else {
      hpColor = ChatColor.RED;
    }
    if (creature instanceof Wolf || creature instanceof IronGolem) {
      return renderPetHealthTag(creature, hpColor, health, maxHealth);
    }
    String name = creature.getMetadata(NewEnemySpawnerManager.CREATURE_CUSTOM_NAME_METADATA).get(0).asString();
    long rounded = Math.round(health);
    String roundedStr = String.valueOf(rounded);
    if (rounded == 0 && !creature.isDead()) {
      roundedStr = "0.5";
    }
    return ChatColor.GRAY + name + " " + hpColor + "" + ChatColor.BOLD + "" + roundedStr + ChatColor.GREEN + "" + ChatColor.BOLD + "/" + ChatColor.GREEN + "" + Math.round(maxHealth) + " ❤";
  }

  private static String renderPetHealthTag(Creature creature, ChatColor hpColor, double health, double maxHealth) {
    UUID ownerId = UUID.fromString(creature.getMetadata("VD_OWNER_UUID").get(0).asString());
    Player player = Bukkit.getPlayer(ownerId);
    long rounded = Math.round(health);
    String roundedStr = String.valueOf(rounded);
    if (rounded == 0 && !creature.isDead()) {
      roundedStr = "0.5";
    }
    String healthPlaceholder = hpColor + "" + ChatColor.BOLD + "" + roundedStr + ChatColor.GREEN + "" + ChatColor.BOLD + "/" + ChatColor.GREEN + Math.round(maxHealth) + " ❤";
    int totalLevel = 0;
    for (EntityUpgrade entityUpgrade : plugin.getEntityUpgradeManager().getRegisteredUpgrades()) {
      if (entityUpgrade.getApplicableEntity() == creature.getType() && creature.hasMetadata(entityUpgrade.getMetadataKey())) {
        totalLevel++;
      }
    }
    if (creature instanceof Wolf) {
      String name = creature.getMetadata(NewEnemySpawnerManager.CREATURE_CUSTOM_NAME_METADATA).get(0).asString();
      if (((Wolf) creature).isAdult()) {
        return ChatColor.GRAY + name + " " + healthPlaceholder;
      }
      return ChatColor.GRAY + name + ChatColor.GRAY + " (Baby) " + healthPlaceholder;
    }
    return new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_GOLEM_NAME").asKey().integer(totalLevel).player(player).value(healthPlaceholder).build();
  }

  public static String[] getVillagerNames() {
    return villagerNames.clone();
  }

  public static String getRandomVillagerName() {
    return getVillagerNames()[villagerNames.length == 1 ? 0 : ThreadLocalRandom.current().nextInt(villagerNames.length)];
  }

  public static Villager spawnVillager(Location location) {
    Creature creature = (Creature) VersionUtils.spawnEntity(location, EntityType.VILLAGER);
    creature.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(200D);
    creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.3);
    creature.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(25.0);
    creature.setHealth(25.0);
    creature.setRemoveWhenFarAway(false);
    creature.setInvisible(false);
    return (Villager) creature;
  }

  public static IronGolem spawnIronGolem(Location location) {
    Creature creature = (Creature) VersionUtils.spawnEntity(location, EntityType.IRON_GOLEM);
    creature.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(200D);
    creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.25);
    creature.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
    creature.setHealth(100.0);
    creature.setRemoveWhenFarAway(false);
    creature.setInvisible(false);
    return (IronGolem) creature;
  }

  public static Wolf spawnWolf(Location location) {
    Creature creature = (Creature) VersionUtils.spawnEntity(location, EntityType.WOLF);
    creature.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(200D);
    creature.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.25);
    creature.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(30.0);
    creature.setHealth(30.0);
    creature.setRemoveWhenFarAway(false);
    creature.setInvisible(false);
    return (Wolf) creature;
  }

  public static VDEnemy getEnemyFromCreature(Creature creature) {
    if (!creature.hasMetadata(NewEnemySpawnerManager.CREATURE_ID_METADATA)) {
      return null;
    }
    return JavaPlugin.getPlugin(Main.class)
      .getNewEnemiesRegistry()
      .getEnemyById(
        creature
          .getMetadata(NewEnemySpawnerManager.CREATURE_ID_METADATA)
          .get(0)
          .asString()
      );
  }

  //todo retarget to focused targets
  public static void unTargetPlayerFromZombies(Player player, Arena arena) {
    for (Creature zombie : arena.getEnemies()) {
      LivingEntity target = zombie.getTarget();

      if (!player.equals(target)) {
        continue;
      }
      //set new target as villager so zombies won't stay still waiting for nothing
      zombie.setTarget(arena.getVillagers().get(arena.getPlugin().getRandom().nextInt(arena.getVillagers().size() - 1)));
    }
  }

}
