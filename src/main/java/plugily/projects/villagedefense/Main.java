/*
 * Village Defense - Protect villagers from hordes of zombies
 * Copyright (c) 2023  Plugily Projects - maintained by Tigerpanzer_02 and contributors
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

package plugily.projects.villagedefense;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.TestOnly;
import plugily.projects.minigamesbox.classic.PluginMain;
import plugily.projects.minigamesbox.classic.handlers.setup.SetupInventory;
import plugily.projects.minigamesbox.classic.handlers.setup.categories.PluginSetupCategoryManager;
import plugily.projects.minigamesbox.classic.utils.configuration.ConfigUtils;
import plugily.projects.minigamesbox.classic.utils.misc.MiscUtils;
import plugily.projects.minigamesbox.classic.utils.services.metrics.Metrics;
import plugily.projects.minigamesbox.classic.utils.version.ServerVersion;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.ArenaEvents;
import plugily.projects.villagedefense.arena.ArenaManager;
import plugily.projects.villagedefense.arena.ArenaRegistry;
import plugily.projects.villagedefense.arena.ArenaUtils;
import plugily.projects.villagedefense.arena.managers.enemy.spawner.EnemySpawnerRegistry;
import plugily.projects.villagedefense.boot.AdditionalValueInitializer;
import plugily.projects.villagedefense.boot.MessageInitializer;
import plugily.projects.villagedefense.boot.PlaceholderInitializer;
import plugily.projects.villagedefense.commands.arguments.ArgumentsRegistry;
import plugily.projects.villagedefense.creatures.CreatureUtils;
import plugily.projects.villagedefense.creatures.DoorBreakListener;
import plugily.projects.villagedefense.events.PluginEvents;
import plugily.projects.villagedefense.handlers.powerup.PowerupHandler;
import plugily.projects.villagedefense.handlers.setup.SetupCategoryManager;
import plugily.projects.villagedefense.handlers.upgrade.EntityUpgradeMenu;
import plugily.projects.villagedefense.handlers.upgrade.upgrades.Upgrade;
import plugily.projects.villagedefense.handlers.upgrade.upgrades.UpgradeBuilder;
import plugily.projects.villagedefense.kits.KitHelper;
import plugily.projects.villagedefense.kits.free.KnightKit;
import plugily.projects.villagedefense.kits.free.LightTankKit;
import plugily.projects.villagedefense.kits.level.ArcherKit;
import plugily.projects.villagedefense.kits.level.HardcoreKit;
import plugily.projects.villagedefense.kits.level.HealerKit;
import plugily.projects.villagedefense.kits.level.LooterKit;
import plugily.projects.villagedefense.kits.level.MediumTankKit;
import plugily.projects.villagedefense.kits.level.PuncherKit;
import plugily.projects.villagedefense.kits.level.RunnerKit;
import plugily.projects.villagedefense.kits.level.ZombieFinderKit;
import plugily.projects.villagedefense.kits.overhauled.BuilderKit;
import plugily.projects.villagedefense.kits.overhauled.CleanerKit;
import plugily.projects.villagedefense.kits.overhauled.MedicKit;
import plugily.projects.villagedefense.kits.overhauled.PetsFriend;
import plugily.projects.villagedefense.kits.overhauled.TerminatorKit;
import plugily.projects.villagedefense.kits.overhauled.TornadoKit;
import plugily.projects.villagedefense.kits.overhauled.WizardKit;
import plugily.projects.villagedefense.kits.premium.HeavyTankKit;
import plugily.projects.villagedefense.kits.premium.NakedKit;
import plugily.projects.villagedefense.kits.premium.PremiumHardcoreKit;
import plugily.projects.villagedefense.kits.premium.ShotBowKit;
import plugily.projects.villagedefense.kits.premium.TeleporterKit;

import java.io.File;
import java.util.logging.Level;

/**
 * Created by Tom on 12/08/2014.
 * Updated by Tigerpanzer_02 on 03.12.2021
 */
public class Main extends PluginMain {

  private FileConfiguration entityUpgradesConfig;
  private EnemySpawnerRegistry enemySpawnerRegistry;
  private ArenaRegistry arenaRegistry;
  private ArenaManager arenaManager;
  private ArgumentsRegistry argumentsRegistry;
  private EntityUpgradeMenu entityUpgradeMenu;

  @TestOnly
  public Main() {
    super();
  }

  @TestOnly
  protected Main(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
    super(loader, description, dataFolder, file);
  }

  @Override
  public void onEnable() {
    long start = System.currentTimeMillis();
    MessageInitializer messageInitializer = new MessageInitializer(this);
    super.onEnable();
    if(!validateStartup()) {
      return;
    }
    getDebugger().debug("[System] [Plugin] Initialization start");
    new PlaceholderInitializer(this);
    messageInitializer.registerMessages();
    new AdditionalValueInitializer(this);
    initializePluginClasses();
    addKits();
    getDebugger().debug("Full {0} plugin enabled", getName());
    getDebugger().debug("[System] [Plugin] Initialization finished took {0}ms", System.currentTimeMillis() - start);
  }

  private boolean validateStartup() {
    if(ServerVersion.Version.isCurrentLower(ServerVersion.Version.v1_12_R1)) {
      MiscUtils.sendLineBreaker(this.getName());
      getMessageUtils().thisVersionIsNotSupported();
      MiscUtils.sendVersionInformation(this, this.getName(), this.getDescription());
      getDebugger().sendConsoleMsg(getPluginMessagePrefix() + "&cYour server version is not supported by " + this.getDescription().getName() + "!");
      getDebugger().sendConsoleMsg(getPluginMessagePrefix() + "&cSadly, we must shut off. Maybe you consider changing your server version?");
      MiscUtils.sendLineBreaker(this.getName());
      getServer().getPluginManager().disablePlugin(this);
      return false;
    }
    return true;
  }

  public void initializePluginClasses() {
    addFileName("powerups");
    addFileName("creatures");
    Arena.init(this);
    ArenaUtils.init(this);
    KitHelper.init(this);
    new ArenaEvents(this);
    arenaManager = new ArenaManager(this);
    arenaRegistry = new ArenaRegistry(this);
    arenaRegistry.registerArenas();
    getSignManager().loadSigns();
    getSignManager().updateSigns();
    argumentsRegistry = new ArgumentsRegistry(this);
    enemySpawnerRegistry = new EnemySpawnerRegistry(this);
    entityUpgradesConfig = ConfigUtils.getConfig(this, "entity_upgrades");
    Upgrade.init(this);
    UpgradeBuilder.init(this);
    entityUpgradeMenu = new EntityUpgradeMenu(this);
    new DoorBreakListener(this);
    CreatureUtils.init(this);
    new PowerupHandler(this);
    new PluginEvents(this);
    addPluginMetrics();
  }

  public void addKits() {
    long start = System.currentTimeMillis();
    getDebugger().debug("Adding kits...");
    addFileName("kits");
    Class<?>[] classKitNames = new Class[]{KnightKit.class, LightTankKit.class, ZombieFinderKit.class, ArcherKit.class, PuncherKit.class, HealerKit.class,
      LooterKit.class, RunnerKit.class, MediumTankKit.class, TerminatorKit.class, HardcoreKit.class, CleanerKit.class, TeleporterKit.class, HeavyTankKit.class,
      ShotBowKit.class, PremiumHardcoreKit.class, TornadoKit.class, BuilderKit.class, PetsFriend.class, MedicKit.class, NakedKit.class, WizardKit.class};
    for(Class<?> kitClass : classKitNames) {
      try {
        kitClass.getDeclaredConstructor().newInstance();
      } catch(Exception e) {
        getLogger().log(Level.SEVERE, "Fatal error while registering existing game kit! Report this error to the developer!");
        getLogger().log(Level.SEVERE, "Cause: " + e.getMessage() + " (kitClass " + kitClass.getName() + ")");
        e.printStackTrace();
      }
    }
    getDebugger().debug("Kit adding finished took {0}ms", System.currentTimeMillis() - start);
  }


  private void addPluginMetrics() {
    getMetrics().addCustomChart(new Metrics.SimplePie("hooked_addons", () -> {
      if(getServer().getPluginManager().getPlugin("VillageDefense-Enhancements") != null) {
        return "Enhancements";
      }
      return "None";
    }));
  }

  public FileConfiguration getEntityUpgradesConfig() {
    return entityUpgradesConfig;
  }

  public EnemySpawnerRegistry getEnemySpawnerRegistry() {
    return enemySpawnerRegistry;
  }

  @Override
  public ArenaRegistry getArenaRegistry() {
    return arenaRegistry;
  }

  @Override
  public ArgumentsRegistry getArgumentsRegistry() {
    return argumentsRegistry;
  }

  @Override
  public ArenaManager getArenaManager() {
    return arenaManager;
  }

  public EntityUpgradeMenu getEntityUpgradeMenu() {
    return entityUpgradeMenu;
  }

  @Override
  public PluginSetupCategoryManager getSetupCategoryManager(SetupInventory setupInventory) {
    return new SetupCategoryManager(setupInventory);
  }

}
