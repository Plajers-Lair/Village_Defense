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

package plugily.projects.villagedefense;

import fr.skytasul.glowingentities.GlowingEntities;
import lombok.Getter;
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
import plugily.projects.villagedefense.arena.powerup.PowerupEvents;
import plugily.projects.villagedefense.boot.AdditionalValueInitializer;
import plugily.projects.villagedefense.boot.MessageInitializer;
import plugily.projects.villagedefense.boot.PlaceholderInitializer;
import plugily.projects.villagedefense.commands.arguments.ArgumentsRegistry;
import plugily.projects.villagedefense.creatures.CreatureUtils;
import plugily.projects.villagedefense.creatures.DoorBreakListener;
import plugily.projects.villagedefense.events.EntityUpgradeListener;
import plugily.projects.villagedefense.events.PluginEvents;
import plugily.projects.villagedefense.handlers.hologram.NewHologramManager;
import plugily.projects.villagedefense.handlers.setup.SetupCategoryManager;
import plugily.projects.villagedefense.handlers.upgrade.EntityUpgradeHandlerEvents;
import plugily.projects.villagedefense.handlers.upgrade.NewEntityUpgradeManager;
import plugily.projects.villagedefense.kits.BuilderKit;
import plugily.projects.villagedefense.kits.CleanerKit;
import plugily.projects.villagedefense.kits.CrusaderKit;
import plugily.projects.villagedefense.kits.KnightKit;
import plugily.projects.villagedefense.kits.MedicKit;
import plugily.projects.villagedefense.kits.PetsFriend;
import plugily.projects.villagedefense.kits.ShotBowKit;
import plugily.projects.villagedefense.kits.TerminatorKit;
import plugily.projects.villagedefense.kits.TornadoKit;
import plugily.projects.villagedefense.kits.WizardKit;
import plugily.projects.villagedefense.kits.utils.KitHelper;
import plugily.projects.villagedefense.user.VDUserManager;
import plugily.projects.villagedefense.utils.ProtocolUtils;

import java.io.File;
import java.util.logging.Level;

/**
 * Created by Tom on 12/08/2014.
 * Updated by Tigerpanzer_02 on 03.12.2021
 */
public class Main extends PluginMain {

  private @Getter FileConfiguration entityUpgradesConfig;
  private @Getter EnemySpawnerRegistry enemySpawnerRegistry;
  private @Getter NewEntityUpgradeManager entityUpgradeManager;
  private @Getter NewHologramManager newHologramManager;
  private @Getter VDUserManager vdUserManager;
  private @Getter GlowingEntities glowingEntities;

  private ArgumentsRegistry argumentsRegistry;
  private ArenaRegistry arenaRegistry;
  private ArenaManager arenaManager;

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
    /*if(!validateStartup()) {
      return;
    }*/
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
    if (ServerVersion.Version.isCurrentLower(ServerVersion.Version.v1_12_R1)) {
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
    ProtocolUtils.init(this);
    new ArenaEvents(this);
    new PowerupEvents(this);
    vdUserManager = new VDUserManager(this);
    arenaManager = new ArenaManager(this);
    arenaRegistry = new ArenaRegistry(this);
    arenaRegistry.registerArenas();
    getSignManager().loadSigns();
    getSignManager().updateSigns();
    argumentsRegistry = new ArgumentsRegistry(this);
    enemySpawnerRegistry = new EnemySpawnerRegistry(this);
    entityUpgradesConfig = ConfigUtils.getConfig(this, "entity_upgrades");
    entityUpgradeManager = new NewEntityUpgradeManager(this);
    newHologramManager = new NewHologramManager(this);
    new DoorBreakListener(this);
    CreatureUtils.init(this);
    new PluginEvents(this);
    new EntityUpgradeListener(this);
    new EntityUpgradeHandlerEvents(this);
    this.glowingEntities = new GlowingEntities(this);
    addPluginMetrics();
  }

  public void addKits() {
    long start = System.currentTimeMillis();
    getDebugger().debug("Adding kits...");
    addFileName("kits");
    Class<?>[] classKitNames = new Class[]{KnightKit.class, BuilderKit.class, TornadoKit.class, ShotBowKit.class, MedicKit.class,
      CleanerKit.class, PetsFriend.class, TerminatorKit.class, CrusaderKit.class, WizardKit.class};
    for (Class<?> kitClass : classKitNames) {
      try {
        kitClass.getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        getLogger().log(Level.SEVERE, "Fatal error while registering existing game kit! Report this error to the developer!");
        getLogger().log(Level.SEVERE, "Cause: " + e.getMessage() + " (kitClass " + kitClass.getName() + ")");
        e.printStackTrace();
      }
    }
    getDebugger().debug("Kit adding finished took {0}ms", System.currentTimeMillis() - start);
  }


  private void addPluginMetrics() {
    getMetrics().addCustomChart(new Metrics.SimplePie("hooked_addons", () -> {
      if (getServer().getPluginManager().getPlugin("VillageDefense-Enhancements") != null) {
        return "Enhancements";
      }
      return "None";
    }));
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

  @Override
  public PluginSetupCategoryManager getSetupCategoryManager(SetupInventory setupInventory) {
    return new SetupCategoryManager(setupInventory);
  }

}
