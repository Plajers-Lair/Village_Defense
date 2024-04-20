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

package plugily.projects.villagedefense.creatures;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.metadata.FixedMetadataValue;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.creatures.v1_9_UP.CustomCreature;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Plajer
 * <p>
 * Created at 2017
 */
public class CreatureUtils {

  private static String[] villagerNames = ("Jagger,Kelsey,Kelton,Haylie,Harlow,Howard,Wulffric,Winfred,Ashley,Bailey,Beckett,Alfredo,Alfred,Adair,Edgar,ED,Eadwig,Edgaras,Buckley,Stanley,Nuffley,"
    + "Mary,Jeffry,Rosaly,Elliot,Harry,Sam,Rosaline,Tom,Ivan,Kevin,Adam,Emma,Mira,Jeff,Isac,Nico").split(",");
  private static Main plugin;
  private static BaseCreatureInitializer creatureInitializer;

  private CreatureUtils() {
  }

  public static void init(Main plugin) {
    CreatureUtils.plugin = plugin;
    villagerNames = new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_VILLAGER_NAMES").asKey().build().split(",");
    creatureInitializer = new plugily.projects.villagedefense.creatures.v1_9_UP.CreatureInitializer();
  }

  /**
   * Check if the given entity is a arena's enemy.
   * We define the enemy as it's not the player, the villager, the wolf and the iron golem
   *
   * @param entity the entity
   * @return true if it is
   */
  public static boolean isEnemy(Entity entity) {
    return entity instanceof Creature && !(entity instanceof Player || entity instanceof Villager || entity instanceof Wolf || entity instanceof IronGolem);
  }

  /**
   * Applies attributes (i.e. health bar,
   * health multiplier and follow range) to target enemy.
   *
   * @param enemy enemy to apply attributes for
   * @param arena arena to get health multiplier from
   */
  public static void applyAttributes(Creature enemy, Arena arena) {
    creatureInitializer.applyFollowRange(enemy);
    VersionUtils.setMaxHealth(enemy, VersionUtils.getMaxHealth(enemy) + arena.getArenaOption("ZOMBIE_DIFFICULTY_MULTIPLIER"));
    enemy.setHealth(VersionUtils.getMaxHealth(enemy));
    enemy.setCustomNameVisible(true);
    enemy.setCustomName(CreatureUtils.getHealthNameTag(enemy));
  }

  public static String getHealthNameTag(Creature creature) {
    return getHealthNameTagPreDamage(creature, 0);
  }

  public static void doStunEnemy(Creature creature, int seconds) {
    if (creature.hasMetadata("VD_STUNNED")) {
      return;
    }
    final String customName = creature.getMetadata(CustomCreature.CREATURE_CUSTOM_NAME_METADATA).get(0).asString();
    String message = new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_ZOMBIE_STUNNED_NAME").asKey().build();
    creature.setMetadata("VD_STUNNED", new FixedMetadataValue(getPlugin(), true));
    creature.setMetadata(CustomCreature.CREATURE_CUSTOM_NAME_METADATA, new FixedMetadataValue(getPlugin(), message));
    creature.setAI(false);
    creature.setCustomName(CreatureUtils.getHealthNameTag(creature));
    Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
      creature.setAI(true);
      creature.removeMetadata("VD_STUNNED", getPlugin());
      creature.setMetadata(CustomCreature.CREATURE_CUSTOM_NAME_METADATA, new FixedMetadataValue(getPlugin(), customName));
      creature.setCustomName(CreatureUtils.getHealthNameTag(creature));
    }, 20L * seconds);
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
    if(health < 0) {
      health = 0;
    }
    double maxHealth = creature.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    ChatColor hpColor;
    if(health >= maxHealth * 0.75) {
      hpColor = ChatColor.GREEN;
    } else if(health >= maxHealth * 0.5) {
      hpColor = ChatColor.GOLD;
    } else if(health >= maxHealth * 0.25) {
      hpColor = ChatColor.YELLOW;
    } else {
      hpColor = ChatColor.RED;
    }
    String name = creature.getMetadata(CustomCreature.CREATURE_CUSTOM_NAME_METADATA).get(0).asString();
    return name + " " + hpColor + "" + ChatColor.BOLD + "" + Math.round(health) + ChatColor.GREEN + "" + ChatColor.BOLD + "/" + Math.round(maxHealth) + " ❤";
  }

  public static String[] getVillagerNames() {
    return villagerNames.clone();
  }

  public static String getRandomVillagerName() {
    return getVillagerNames()[villagerNames.length == 1 ? 0 : ThreadLocalRandom.current().nextInt(villagerNames.length)];
  }

  public static Main getPlugin() {
    return plugin;
  }

  public static BaseCreatureInitializer getCreatureInitializer() {
    return creatureInitializer;
  }

}
