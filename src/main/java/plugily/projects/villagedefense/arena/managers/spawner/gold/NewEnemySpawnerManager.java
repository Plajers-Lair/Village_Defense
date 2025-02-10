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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffectType;
import plugily.projects.minigamesbox.classic.utils.configuration.ConfigUtils;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.ArmorPiece;
import plugily.projects.villagedefense.arena.managers.spawner.gold.enemy.VDEnemy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
public class NewEnemySpawnerManager {

  public static final String CREATURE_ID_METADATA = "VD_CREATURE_ID";
  public static final String CREATURE_CUSTOM_NAME_METADATA = "VD_CREATURE_CUSTOM_NAME";
  public static final String CREATURE_PERSISTENT_TARGETING = "VD_CREATURE_PERSIST_TARGET";

  private final Random random = new Random();
  private final Arena arena;
  private final List<Creature> glitchedEnemies = new ArrayList<>();
  private final Map<Creature, Location> enemyCheckerLocations = new HashMap<>();
  private Map<Integer, List<WaveEnemies>> spawnsMap = new HashMap<>();
  private boolean spawning = false;
  private int stage = 1;
  private int stageCounter = 0;
  private int stageCompleted = 0;

  public NewEnemySpawnerManager(Arena arena) {
    this.arena = arena;
    registerSpawnsMap();
  }

  private void registerSpawnsMap() {
    FileConfiguration config = ConfigUtils.getConfig(arena.getPlugin(), "internal/enemies_map");
    for (String waveKey : config.getConfigurationSection("classic_gold").getKeys(false)) {
      List<WaveEnemies> waveEnemies = new LinkedList<>();
      int sumCount = 0;
      for (String stageKey : config.getConfigurationSection("classic_gold." + waveKey).getKeys(false)) {
        Map<VDEnemy, Integer> localSpawns = new HashMap<>();
        for (String zombieData : config.getStringList("classic_gold." + waveKey + "." + stageKey)) {
          String zombieId = zombieData.split(";")[0];
          int count = Integer.parseInt(zombieData.split(";")[1]);
          VDEnemy enemy = arena.getPlugin().getNewEnemiesRegistry().getEnemyById(zombieId);
          if (enemy == null) {
            log.warn("Enemy with id {} not found in registry!", zombieId);
            continue;
          }
          localSpawns.put(enemy, count);
          sumCount += count;
        }
        waveEnemies.add(new WaveEnemies(Integer.parseInt(stageKey), localSpawns));
      }
      spawnsMap.put(Integer.parseInt(waveKey), waveEnemies);
      log.info("Registered {} total enemies for wave {} at mode {}", sumCount, waveKey, "Classic Gold");
    }
  }

  public boolean hasSpawningStarted() {
    return spawning;
  }

  public void doResetSpawnCheck() {
    spawning = false;
    stage = 1;
    stageCounter = 0;
    stageCompleted = 0;
  }

  public void doSpawnEnemies() {
    for (Creature creature : arena.getEnemies()) {
      LivingEntity creatureTarget = creature.getTarget();
      VDEnemy enemy = NewCreatureUtils.getEnemyFromCreature(creature);
      if (creatureTarget == null || creatureTarget.isDead()) {
        setTargetPriority(enemy, creature);
      }
    }
    doPetsTargeting();

    if (!spawning) {
      spawning = true;
    }
    if (stageCompleted != stage) {
      for (WaveEnemies waveEnemies : spawnsMap.getOrDefault(arena.getWave(), new LinkedList<>())) {
        if (waveEnemies.getStage() != stage) {
          continue;
        }
        for (Map.Entry<VDEnemy, Integer> entry : waveEnemies.getEnemies().entrySet()) {
          doSpawnEnemy(entry.getKey(), entry.getValue());
        }
      }
    }
    stageCompleted = stage;
    stageCounter++;
    if (stageCounter >= 5) {
      stageCounter = 0;
      stage++;
    }
  }

  public int getEnemiesForWave(int wave) {
    int count = 0;
    for (WaveEnemies waveEnemies : spawnsMap.getOrDefault(wave, new ArrayList<>())) {
      for (Map.Entry<VDEnemy, Integer> entry : waveEnemies.getEnemies().entrySet()) {
        count += entry.getValue();
      }
    }
    return count;
  }

  /**
   * Spawns enemy outside zombie spawner manager, for example to be used for Pop Zombie that spawns bonus zombie on kill
   */
  public Creature doSpawnEnemyOutsideManagement(VDEnemy enemy, Location location) {
    Creature creature = doLocalSpawn(enemy, location);
    arena.getPlugin().getHolidayManager().applyHolidayCreatureEffects(creature);
    arena.getEnemies().add(creature);
    return creature;
  }

  private void doSpawnEnemy(VDEnemy enemy, int count) {
    if (arena.getWave() < enemy.getWaveMinimum() || arena.getWave() > enemy.getWaveMaximum()) {
      log.warn("Enemy spawned outside suggested wave limits! Wave {} but limited to {}-{}", arena.getWave(), enemy.getWaveMinimum(), enemy.getWaveMaximum());
    }
    for (int i = 0; i < count; i++) {
      Creature creature = doLocalSpawn(enemy, arena.getRandomZombieSpawnLocation(random));
      arena.getPlugin().getHolidayManager().applyHolidayCreatureEffects(creature);
      arena.changeArenaOptionBy("ZOMBIES_TO_SPAWN", -1);
      arena.setArenaOption("ZOMBIES_TO_SPAWN", arena.getArenaOption("ZOMBIES_TO_SPAWN") - 1);
      arena.getEnemies().add(creature);
    }
  }

  //todo can spawn func
  //todo damage scaling & with players
  private Creature doLocalSpawn(VDEnemy enemy, Location location) {
    Creature creature = (Creature) VersionUtils.spawnEntity(location, enemy.getType());
    creature.setMetadata(CREATURE_CUSTOM_NAME_METADATA, new FixedMetadataValue(arena.getPlugin(), ChatColor.translateAlternateColorCodes('&', enemy.getName())));
    creature.setMetadata(CREATURE_ID_METADATA, new FixedMetadataValue(arena.getPlugin(), enemy.getId()));
    creature.setMetadata("VD_CUSTOM_ENTITY", new FixedMetadataValue(arena.getPlugin(), true));
    if (creature instanceof Ageable ageable) {
      if (enemy.isBaby()) {
        ageable.setBaby();
      } else {
        ageable.setAdult();
      }
      ageable.setAgeLock(true);
    }
    int wave = arena.getWave();
    creature.setCanPickupItems(false);
    creature.getEquipment().clear();
    for (Map.Entry<ArmorPiece.PiecePart, List<ArmorPiece>> entry : enemy.getWaveArmorPieces().entrySet()) {
      List<ArmorPiece> piecesOrdered = entry.getValue()
        .stream()
        .sorted(Comparator.comparing(ArmorPiece::getPriority).reversed())
        .toList();
      for (ArmorPiece piece : piecesOrdered) {
        if (wave < piece.getWaveMinimum()) {
          continue;
        }
        if (random.nextDouble(0, 100) > piece.getChance()) {
          continue;
        }
        switch (entry.getKey()) {
          case HELMET -> creature.getEquipment().setHelmet(piece.getItemStack());
          case CHESTPLATE -> creature.getEquipment().setChestplate(piece.getItemStack());
          case LEGGINGS -> creature.getEquipment().setLeggings(piece.getItemStack());
          case BOOTS -> creature.getEquipment().setBoots(piece.getItemStack());
        }
        break;
      }
    }
    if (enemy.isImpostorHead()) {
      List<Player> players = new ArrayList<>(arena.getPlayers());
      Player randomPlayer = players.get(random.nextInt(players.size()));
      ItemStack head = new ItemStack(Material.PLAYER_HEAD);
      SkullMeta meta = (SkullMeta) head.getItemMeta();
      meta.setPlayerProfile(randomPlayer.getPlayerProfile());
      head.setItemMeta(meta);
      creature.getEquipment().setHelmet(head);
    }
    for (VDEnemy.WeaponPiece piece : enemy.getWeaponParts()) {
      if (wave >= piece.getWaveMinimum() && wave <= piece.getWaveMaximum()) {
        VersionUtils.setItemInHand(creature, piece.getItemStack());
      }
    }
    enemy.getOnSpawn().accept(creature);
    double baseHealth = enemy.getHealthFunction().apply(wave);
    if (arena.getPlayers().size() > 1) {
      baseHealth += baseHealth * (0.1 * (arena.getPlayers().size() - 1));
    }
    creature.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(baseHealth);
    creature.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(200D);
    creature.setHealth(baseHealth);
    creature.setRemoveWhenFarAway(false);
    creature.setCustomNameVisible(true);
    creature.setCustomName(NewCreatureUtils.getHealthNameTag(creature));
    setTargetPriority(enemy, creature);
    if (enemy.isPersistentTargeting()) {
      creature.setMetadata(CREATURE_PERSISTENT_TARGETING, new FixedMetadataValue(arena.getPlugin(), true));
    }
    //todo door bulldozer trait
    /*if(doorBulldozing) {
      creature.setMetadata(DoorBreakListener.CREATURE_DOOR_BULLDOZER_METADATA, new FixedMetadataValue(arena.getPlugin(), true));
    }*/
    return creature;
  }

  private void setTargetPriority(VDEnemy enemy, Creature creature) {
    List<LivingEntity> targets = new ArrayList<>();
    if (enemy.getTargetPriority() == null) {
      targets.addAll(arena.getVillagers());
    } else {
      switch (enemy.getTargetPriority()) {
        case VILLAGER -> targets.addAll(arena.getVillagers());
        case IRON_GOLEM -> targets.addAll(
          arena.getIronGolems()
            .stream()
            .filter(golem -> !golem.hasMetadata("VD_PET_DEAD"))
            .toList()
        );
        case WOLF -> targets.addAll(
          arena.getWolves()
            .stream()
            .filter(wolf -> !wolf.hasMetadata("VD_PET_DEAD"))
            .toList()
        );
        case PLAYER -> targets.addAll(arena.getPlayersLeft());
      }
    }
    LivingEntity nearestEntity = targets.get(0);

    Location location = creature.getLocation();
    for (LivingEntity entity : targets) {
      double distance = location.distance(entity.getLocation());
      if (distance < location.distance(nearestEntity.getLocation())) {
        nearestEntity = entity;
      }
    }
    creature.setTarget(nearestEntity);
  }

  private void doPetsTargeting() {
    List<Creature> pets = new ArrayList<>();
    pets.addAll(arena.getWolves());
    pets.addAll(arena.getIronGolems());
    if (arena.getEnemies().isEmpty()) {
      return;
    }
    List<Creature> targets = arena.getEnemies()
      .stream()
      .filter(enemy -> !enemy.hasPotionEffect(PotionEffectType.INVISIBILITY))
      .toList();
    for (Creature pet : pets) {
      LivingEntity currentTarget = pet.getTarget();
      if (!NewCreatureUtils.isEnemy(currentTarget)) {
        pet.setTarget(targets.get(targets.size() > 1 ? random.nextInt(targets.size() - 1) : 0));
      }
    }
  }

  /**
   * Increments ZOMBIE_GLITCH_CHECKER value and attempts to check
   * whether any enemies are glitched on spawn point when
   * ZOMBIE_GLITCH_CHECKER value is higher or equal than 60
   * <p>
   * Glitch checker also clean ups dead enemies and villagers from the arena
   */
  public void doGlitchCheck() {
    arena.changeArenaOptionBy("ZOMBIE_GLITCH_CHECKER", 1);
    if (arena.getArenaOption("ZOMBIE_GLITCH_CHECKER") >= 60) {
      Iterator<Villager> villagerIterator = arena.getVillagers().iterator();
      while (villagerIterator.hasNext()) {
        Villager villager = villagerIterator.next();
        if (villager.isDead()) {
          villagerIterator.remove();
          arena.removeVillager(villager);
        }
      }
      arena.setArenaOption("ZOMBIE_GLITCH_CHECKER", 0);

      Iterator<Creature> creatureIterator = arena.getEnemies().iterator();
      while (creatureIterator.hasNext()) {
        Creature creature = creatureIterator.next();
        if (creature.isDead()) {
          creatureIterator.remove();
          arena.removeEnemy(creature);
          continue;
        }
        if (glitchedEnemies.contains(creature) && creature.getLocation().distance(enemyCheckerLocations.get(creature)) <= 1) {
          creatureIterator.remove();
          arena.removeEnemy(creature);
          enemyCheckerLocations.remove(creature);
          creature.remove();
        }

        Location checkerLoc = enemyCheckerLocations.get(creature);
        if (checkerLoc == null) {
          enemyCheckerLocations.put(creature, creature.getLocation());
        } else if (creature.getLocation().distance(checkerLoc) <= 1) {
          VersionUtils.teleport(creature, arena.getRandomZombieSpawnLocation(arena.getPlugin().getRandom()));
          enemyCheckerLocations.put(creature, creature.getLocation());
          glitchedEnemies.add(creature);
        }
      }
    }
  }

  @Data
  @AllArgsConstructor
  public static class WaveEnemies {

    private int stage;
    private Map<VDEnemy, Integer> enemies;

  }

}
