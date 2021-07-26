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

package plugily.projects.villagedefense.creatures;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import plugily.projects.commonsbox.minecraft.compat.ServerVersion;
import plugily.projects.commonsbox.minecraft.compat.VersionUtils;
import plugily.projects.commonsbox.string.StringFormatUtils;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.options.ArenaOption;
import plugily.projects.villagedefense.handlers.language.LanguageManager;

/**
 * @author Plajer
 * <p>
 * Created at 17 lis 2017
 */
public class CreatureUtils {

  private static float zombieSpeed = 1.3f;
  private static float babyZombieSpeed = 2.0f;
  private static String[] villagerNames = ("Jagger,Kelsey,Kelton,Haylie,Harlow,Howard,Wulffric,Winfred,Ashley,Bailey,Beckett,Alfredo,Alfred,Adair,Edgar,ED,Eadwig,Edgaras,Buckley,Stanley,Nuffley,"
      + "Mary,Jeffry,Rosaly,Elliot,Harry,Sam,Rosaline,Tom,Ivan,Kevin,Adam").split(",");
  private static Main plugin;
  private static BaseCreatureInitializer creatureInitializer;
  private static final List<CachedObject> cachedObjects = new ArrayList<>();

  private CreatureUtils() {
  }

  public static void init(Main plugin) {
    CreatureUtils.plugin = plugin;
    zombieSpeed = (float) plugin.getConfig().getDouble("Zombie-Speed", 1.3);
    babyZombieSpeed = (float) plugin.getConfig().getDouble("Mini-Zombie-Speed", 2.0);
    villagerNames = LanguageManager.getLanguageMessage("In-Game.Villager-Names").split(",");
    creatureInitializer = initCreatureInitializer();
  }

  public static BaseCreatureInitializer initCreatureInitializer() {
    switch (ServerVersion.Version.getCurrent()) {
      case v1_8_R3:
        return new plugily.projects.villagedefense.creatures.v1_8_R3.CreatureInitializer();
      case v1_9_R1:
        return new plugily.projects.villagedefense.creatures.v1_9_R1.CreatureInitializer();
      case v1_9_R2:
        return new plugily.projects.villagedefense.creatures.v1_9_R2.CreatureInitializer();
      case v1_10_R1:
        return new plugily.projects.villagedefense.creatures.v1_10_R1.CreatureInitializer();
      case v1_11_R1:
        return new plugily.projects.villagedefense.creatures.v1_11_R1.CreatureInitializer();
      case v1_12_R1:
        return new plugily.projects.villagedefense.creatures.v1_12_R1.CreatureInitializer();
      case v1_13_R1:
        return new plugily.projects.villagedefense.creatures.v1_13_R1.CreatureInitializer();
      case v1_13_R2:
        return new plugily.projects.villagedefense.creatures.v1_13_R2.CreatureInitializer();
      case v1_14_R1:
        return new plugily.projects.villagedefense.creatures.v1_14_R1.CreatureInitializer();
      case v1_15_R1:
        return new plugily.projects.villagedefense.creatures.v1_15_R1.CreatureInitializer();
      case v1_16_R1:
        return new plugily.projects.villagedefense.creatures.v1_16_R1.CreatureInitializer();
      case v1_16_R2:
        return new plugily.projects.villagedefense.creatures.v1_16_R2.CreatureInitializer();
      case v1_16_R3:
        return new plugily.projects.villagedefense.creatures.v1_16_R3.CreatureInitializer();
      default:
        return new plugily.projects.villagedefense.creatures.v1_17_R2.CreatureInitializer();
    }
  }

  public static Object getPrivateField(String fieldName, Class<?> clazz, Object object) {
    for(CachedObject cachedObject : cachedObjects) {
      if(cachedObject.getClazz().equals(clazz) && cachedObject.getFieldName().equals(fieldName)) {
        return cachedObject.getObject();
      }
    }
    try {
      Field field = clazz.getDeclaredField(fieldName);

      AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
        field.setAccessible(true);
        return null;
      });

      Object o = field.get(object);
      cachedObjects.add(new CachedObject(fieldName, clazz, o));
      return o;
    } catch(NoSuchFieldException | IllegalAccessException e) {
      plugin.getLogger().log(Level.WARNING, "Failed to retrieve private field of object " + object.getClass() + "!");
      plugin.getLogger().log(Level.WARNING, e.getMessage() + " (fieldName " + fieldName + ", class " + clazz.getName() + ")");
    }
    return null;
  }

  /**
   * Check if the given entity is a arena's zombie.
   * We define the zombie as it's not the player, the villager, the wolf and the iron golem
   *
   * @param entity the entity
   * @return true if it is
   */
  public static boolean isZombie(Entity entity) {
    return entity instanceof Creature && !(entity instanceof Player || entity instanceof Villager || entity instanceof Wolf || entity instanceof IronGolem);
  }

  /**
   * Applies attributes (i.e. health bar (if enabled),
   * health multiplier and follow range) to target zombie.
   *
   * @param zombie zombie to apply attributes for
   * @param arena  arena to get health multiplier from
   */
  public static void applyAttributes(Creature zombie, Arena arena) {
    creatureInitializer.applyFollowRange(zombie);
    VersionUtils.setMaxHealth(zombie, VersionUtils.getMaxHealth(zombie) + arena.getOption(ArenaOption.ZOMBIE_DIFFICULTY_MULTIPLIER));
    zombie.setHealth(VersionUtils.getMaxHealth(zombie));
    if(plugin.getConfig().getBoolean("Simple-Zombie-Health-Bar-Enabled", true)) {
      zombie.setCustomNameVisible(true);
      zombie.setCustomName(StringFormatUtils.getProgressBar((int) zombie.getHealth(), (int) VersionUtils.getMaxHealth(zombie), 50, "|",
          ChatColor.YELLOW + "", ChatColor.GRAY + ""));
    }
  }

  public static float getZombieSpeed() {
    return zombieSpeed;
  }

  public static float getBabyZombieSpeed() {
    return babyZombieSpeed;
  }

  public static String[] getVillagerNames() {
    return villagerNames.clone();
  }

  public static String getRandomVillagerName() {
    return villagerNames[villagerNames.length == 1 ? 0 : ThreadLocalRandom.current().nextInt(villagerNames.length)];
  }

  public static Main getPlugin() {
    return plugin;
  }

  public static BaseCreatureInitializer getCreatureInitializer() {
    return creatureInitializer;
  }
}
