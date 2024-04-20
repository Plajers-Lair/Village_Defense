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

package plugily.projects.villagedefense.arena;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Wolf;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import plugily.projects.minigamesbox.classic.arena.ArenaState;
import plugily.projects.minigamesbox.classic.arena.PluginArena;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.misc.MiscUtils;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.assist.AssistHandler;
import plugily.projects.villagedefense.arena.managers.CreatureTargetManager;
import plugily.projects.villagedefense.arena.managers.EnemySpawnManager;
import plugily.projects.villagedefense.arena.managers.ScoreboardManager;
import plugily.projects.villagedefense.arena.managers.ShopManager;
import plugily.projects.villagedefense.arena.managers.maprestorer.MapRestorerManager;
import plugily.projects.villagedefense.arena.midwave.MidWaveEvent;
import plugily.projects.villagedefense.arena.states.EndingState;
import plugily.projects.villagedefense.arena.states.InGameState;
import plugily.projects.villagedefense.arena.states.RestartingState;
import plugily.projects.villagedefense.arena.states.StartingState;
import plugily.projects.villagedefense.creatures.CreatureUtils;
import plugily.projects.villagedefense.creatures.v1_9_UP.CustomCreature;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 17.12.2021
 */
public class Arena extends PluginArena {

  private static Main plugin;
  private final List<Creature> enemies = new ArrayList<>();
  private final List<Wolf> wolves = new ArrayList<>();
  private final List<Villager> villagers = new ArrayList<>();
  private final List<IronGolem> ironGolems = new ArrayList<>();
  private final List<LivingEntity> specialEntities = new ArrayList<>();
  private final List<Item> droppedFleshes = new ArrayList<>();
  private final List<Entity> spawnedEntities = new ArrayList<>();
  private MapRestorerManager mapRestorerManager;
  private MidWaveEvent midWaveEvent;

  private final Map<SpawnPoint, List<Location>> spawnPoints = new EnumMap<>(SpawnPoint.class);

  private ShopManager shopManager;
  private EnemySpawnManager enemySpawnManager;
  private CreatureTargetManager creatureTargetManager;
  private AssistHandler assistHandler;

  private boolean fighting = false;

  public Arena(String id) {
    super(id);
    setPluginValues();
    shopManager = new ShopManager(this);
    enemySpawnManager = new EnemySpawnManager(this);
    creatureTargetManager = new CreatureTargetManager(this);
    mapRestorerManager = new MapRestorerManager(this);
    assistHandler = new AssistHandler(plugin);
    setMapRestorerManager(mapRestorerManager);
    setScoreboardManager(new ScoreboardManager(this));

    addGameStateHandler(ArenaState.ENDING, new EndingState());
    addGameStateHandler(ArenaState.IN_GAME, new InGameState());
    addGameStateHandler(ArenaState.RESTARTING, new RestartingState());
    addGameStateHandler(ArenaState.STARTING, new StartingState());
  }

  public void reloadShopManager() {
    shopManager = new ShopManager(this);
  }

  public static void init(Main plugin) {
    Arena.plugin = plugin;
  }

  @Override
  public Main getPlugin() {
    return plugin;
  }

  private void setPluginValues() {
    for(SpawnPoint point : SpawnPoint.values()) {
      spawnPoints.put(point, new ArrayList<>());
    }
  }

  public ShopManager getShopManager() {
    return shopManager;
  }

  public EnemySpawnManager getEnemySpawnManager() {
    return enemySpawnManager;
  }

  public CreatureTargetManager getCreatureTargetManager() {
    return creatureTargetManager;
  }

  public AssistHandler getAssistHandler() {
    return assistHandler;
  }

  public void clearVillagers() {
    for(Entity entity : plugin.getBukkitHelper().getNearbyEntities(getStartLocation(), 50)) {
      if(!(entity instanceof Villager)) {
        continue;
      }
      removeVillager((Villager) entity);
    }
  }

  public void spawnVillagers() {
    List<Location> villagerSpawns = getVillagerSpawns();
    if(villagerSpawns.isEmpty()) {
      getPlugin().getDebugger().debug(Level.WARNING, "No villager spawns set for {0} game won't start", getId());
      return;
    }

    int amount = getPlugin().getConfig().getInt("Limit.Spawn.Villagers", 10);
    int spawnSize = villagerSpawns.size();
    for(int i = 0; i < amount; i++) {
      spawnVillager(villagerSpawns.get(i % spawnSize));
    }

    if(villagers.isEmpty()) {
      getPlugin().getDebugger().debug(Level.WARNING, "Spawning villagers for {0} failed! Are villager spawns set in safe and valid locations?", getId());
    }
  }

  public boolean isFighting() {
    return fighting;
  }

  public void setFighting(boolean fighting) {
    this.fighting = fighting;
  }


  /**
   * Get list of already spawned enemies.
   * This will only return alive enemies not total enemies in current wave.
   *
   * @return list of spawned enemies in arena
   */
  @NotNull
  public List<Creature> getEnemies() {
    return enemies;
  }

  public void removeEnemy(Creature enemy) {
    enemies.remove(enemy);
  }

  @NotNull
  public List<Location> getVillagerSpawns() {
    return spawnPoints.getOrDefault(SpawnPoint.VILLAGER, new ArrayList<>());
  }

  public void addVillagerSpawn(Location location) {
    plugin.getDebugger().debug("Arena {0} Adding villager spawn on location {1}", getId(), location.toString());
    List<Location> villagerSpawns = getVillagerSpawns();
    villagerSpawns.add(location);
    spawnPoints.put(SpawnPoint.VILLAGER, villagerSpawns);
    plugin.getDebugger().debug("Arena {0} VillagerSpawns {1}", getId(), getVillagerSpawns());
  }

  public void addZombieSpawn(Location location) {
    plugin.getDebugger().debug("Arena {0} Adding zombie spawn on location {1}", getId(), location.toString());
    List<Location> zombies = getZombieSpawns();
    zombies.add(location);
    spawnPoints.put(SpawnPoint.ZOMBIE, zombies);
    plugin.getDebugger().debug("Arena {0} ZombieSpawns {1}", getId(), getZombieSpawns());
  }

  @NotNull
  public List<Item> getDroppedFleshes() {
    return droppedFleshes;
  }

  public void addDroppedFlesh(Item item) {
    droppedFleshes.add(item);
  }

  public void removeDroppedFlesh(Item item) {
    droppedFleshes.remove(item);
  }

  public int getZombiesLeft() {
    return getArenaOption("ZOMBIES_TO_SPAWN") + enemies.size();
  }

  public int getWave() {
    return getArenaOption("WAVE");
  }

  /**
   * Should be used with endWave.
   *
   * @param wave new game wave
   * @see ArenaManager#endWave(Arena)
   */
  public void setWave(int wave) {
    setArenaOption("WAVE", wave);
  }

  public void spawnVillager(Location location) {
    Villager villager = CreatureUtils.getCreatureInitializer().spawnVillager(location);
    villager.setCustomNameVisible(true);
    String name = CreatureUtils.getRandomVillagerName();
    villager.setMetadata(CustomCreature.CREATURE_CUSTOM_NAME_METADATA, new FixedMetadataValue(plugin, name));
    villager.setCustomName(CreatureUtils.getHealthNameTag(villager));
    villager.setVillagerLevel(5);
    villager.setVillagerType(Villager.Type.values()[ThreadLocalRandom.current().nextInt(Villager.Type.values().length)]);
    villager.setProfession(Villager.Profession.values()[ThreadLocalRandom.current().nextInt(Villager.Profession.values().length)]);
    addVillager(villager);
  }

  @Nullable
  public Creature spawnWolf(Location location, Player player) {
    if(!canSpawnMobForPlayer(player, EntityType.WOLF)) {
      return null;
    }
    return spawnWolfForce(location, player);
  }

  @Nullable
  public Creature spawnWolfForce(Location location, Player player) {
    Wolf wolf = CreatureUtils.getCreatureInitializer().spawnWolf(location);
    wolf.setMetadata("VD_OWNER_UUID", new FixedMetadataValue(getPlugin(), player.getUniqueId().toString()));
    wolf.setMetadata("VD_ALIVE_SINCE_WAVE", new FixedMetadataValue(getPlugin(), getWave()));
    wolf.setOwner(player);
    wolf.setCustomNameVisible(true);
    wolf.setCollarColor(DyeColor.values()[ThreadLocalRandom.current().nextInt(DyeColor.values().length)]);
    wolf.setCustomName(new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_WOLF_NAME").asKey().integer(0).player(player).build());
    new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_WOLF_SPAWN").asKey().player(player).sendPlayer();
    addWolf(wolf);
    return wolf;
  }

  @Nullable
  public Creature spawnGolem(Location location, Player player) {
    if(!canSpawnMobForPlayer(player, EntityType.IRON_GOLEM)) {
      return null;
    }
    return spawnGolemForce(location, player);
  }

  @Nullable
  public Creature spawnGolemForce(Location location, Player player) {
    IronGolem ironGolem = CreatureUtils.getCreatureInitializer().spawnGolem(location);
    ironGolem.setMetadata("VD_OWNER_UUID", new FixedMetadataValue(getPlugin(), player.getUniqueId().toString()));
    ironGolem.setMetadata("VD_ALIVE_SINCE_WAVE", new FixedMetadataValue(getPlugin(), getWave()));
    ironGolem.setCustomNameVisible(true);
    ironGolem.setCustomName(new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_GOLEM_NAME").asKey().integer(0).player(player).build());
    ironGolem.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(150.0);
    new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_GOLEM_SPAWN").asKey().player(player).sendPlayer();
    addIronGolem(ironGolem);
    MiscUtils.getEntityAttribute(ironGolem, Attribute.GENERIC_MOVEMENT_SPEED).ifPresent(ai -> ai.setBaseValue(0.25));
    return ironGolem;
  }

  protected void addWolf(Wolf wolf) {
    wolves.add(wolf);
    spawnedEntities.add(wolf);
  }

  public boolean canSpawnMobForPlayer(Player player, EntityType type) {
    if(type != EntityType.IRON_GOLEM && type != EntityType.WOLF) {
      return false;
    }
    int globalEntityLimit = 0;
    switch(type) {
      case WOLF:
        globalEntityLimit = plugin.getConfig().getInt("Limit.Spawn.Wolves", 20);
        break;
      case IRON_GOLEM:
        globalEntityLimit = plugin.getConfig().getInt("Limit.Spawn.Golems", 15);
        break;
      default:
        break;
    }
    plugin.getDebugger().debug("SpawnMobCheck for {0} and mob {1}, globalLimit {2}", player.getName(), type, globalEntityLimit);
    List<Entity> entities = new ArrayList<>(spawnedEntities);
    if(plugin.getConfigPreferences().getOption("LIMIT_ENTITY_BUY_AFTER_DEATH")) {
      List<Entity> entityList = entities.stream().filter(entity -> entity.getType() == type).collect(Collectors.toList());
      entityList = entityList.stream().filter(en -> !en.isDead()).collect(Collectors.toList());

      long spawnedAmount = entityList.size();
      if(spawnedAmount >= globalEntityLimit) {
        new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_SHOP_MOB_LIMIT_REACHED").asKey().player(player).integer(globalEntityLimit).sendPlayer();
        return false;
      }
    }
    return true;
  }

  /**
   * Get alive wolves.
   *
   * @return alive wolves in game
   */
  @NotNull
  public List<Wolf> getWolves() {
    return wolves;
  }

  /**
   * Get alive iron golems.
   *
   * @return alive iron golems in game
   */
  @NotNull
  public List<IronGolem> getIronGolems() {
    return ironGolems;
  }

  /**
   * Get alive villagers.
   *
   * @return alive villagers in game
   */
  @NotNull
  public List<Villager> getVillagers() {
    return villagers;
  }

  /**
   * Get special alive entities.
   *
   * @return alive special entities
   */
  @NotNull
  public List<LivingEntity> getSpecialEntities() {
    return specialEntities;
  }

  public boolean checkLevelUpRottenFlesh() {
    String rottenFleshLevelOption = "ROTTEN_FLESH_LEVEL";
    int rottenFleshLevel = getArenaOption(rottenFleshLevelOption);
    int rottenFleshAmount = getArenaOption("ROTTEN_FLESH_AMOUNT");

    if(rottenFleshLevel == 0 && rottenFleshAmount > 50) {
      setArenaOption(rottenFleshLevelOption, 1);
      return true;
    }

    if(rottenFleshLevel * 10 * getPlayers().size() + 10 < rottenFleshAmount) {
      changeArenaOptionBy(rottenFleshLevelOption, 1);
      return true;
    }

    return false;
  }

  protected void addVillager(Villager villager) {
    villagers.add(villager);
  }

  public void removeVillager(Villager villager) {
    villager.remove();
    villager.setHealth(0);
    villagers.remove(villager);
  }

  @Override
  public MapRestorerManager getMapRestorerManager() {
    return mapRestorerManager;
  }

  public MidWaveEvent getMidWaveEvent() {
    return midWaveEvent;
  }

  public void setMidWaveEvent(MidWaveEvent midWaveEvent) {
    this.midWaveEvent = midWaveEvent;
  }

  @NotNull
  public List<Location> getZombieSpawns() {
    return spawnPoints.getOrDefault(SpawnPoint.ZOMBIE, new ArrayList<>());
  }

  public final Location getRandomZombieSpawnLocation(Random random) {
    List<Location> spawns = getZombieSpawns();
    return spawns.get(spawns.size() == 1 ? 0 : random.nextInt(spawns.size()));
  }

  protected void addIronGolem(IronGolem ironGolem) {
    ironGolems.add(ironGolem);
    spawnedEntities.add(ironGolem);
  }

  public void removeIronGolem(IronGolem ironGolem) {
    ironGolem.remove();
    ironGolems.remove(ironGolem);
    spawnedEntities.remove(ironGolem);
  }

  public void removeWolf(Wolf wolf) {
    wolf.remove();
    wolves.remove(wolf);
    spawnedEntities.remove(wolf);
  }

  public void addSpecialEntity(LivingEntity livingEntity) {
    specialEntities.add(livingEntity);
  }

  public void removeSpecialEntity(LivingEntity entity) {
    specialEntities.remove(entity);
  }

  public enum SpawnPoint {
    ZOMBIE, VILLAGER
  }

}