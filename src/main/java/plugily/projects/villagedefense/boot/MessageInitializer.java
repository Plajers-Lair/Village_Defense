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

package plugily.projects.villagedefense.boot;

import plugily.projects.minigamesbox.classic.handlers.language.Message;
import plugily.projects.minigamesbox.classic.handlers.language.MessageManager;
import plugily.projects.minigamesbox.classic.utils.services.locale.Locale;
import plugily.projects.minigamesbox.classic.utils.services.locale.LocaleRegistry;
import plugily.projects.villagedefense.Main;

import java.util.Arrays;

/**
 * @author Tigerpanzer_02
 * <p>
 * Created at 15.10.2022
 */
public class MessageInitializer {
  private final Main plugin;

  public MessageInitializer(Main plugin) {
    this.plugin = plugin;
    registerLocales();
  }

  public void registerMessages() {
    getMessageManager().registerMessage("COMMANDS_ADMIN_ADDED_ORBS", new Message("Commands.Admin.Added-Orbs", ""));
    getMessageManager().registerMessage("COMMANDS_ADMIN_RECEIVED_ORBS", new Message("Commands.Admin.Received-Orbs", ""));


    getMessageManager().registerMessage("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_DIED_PLAYERS", new Message("In-Game.Messages.Game-End.Placeholders.Died.Players", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_DIED_VILLAGERS", new Message("In-Game.Messages.Game-End.Placeholders.Died.Villagers", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_GAME_END_PLACEHOLDERS_SURVIVED", new Message("In-Game.Messages.Game-End.Placeholders.Survived", ""));

    getMessageManager().registerMessage("IN_GAME_MESSAGES_ADMIN_REMOVED_VILLAGERS", new Message("In-Game.Messages.Admin.Removed.Villagers", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_ADMIN_REMOVED_GOLEMS", new Message("In-Game.Messages.Admin.Removed.Golems", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_ADMIN_REMOVED_ZOMBIES", new Message("In-Game.Messages.Admin.Removed.Zombies", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_ADMIN_REMOVED_WOLVES", new Message("In-Game.Messages.Admin.Removed.Wolves", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_ADMIN_CHANGED_WAVE", new Message("In-Game.Messages.Admin.Changed.Wave", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_ADMIN_NOTHING_T0_CLEAN", new Message("In-Game.Messages.Admin.Nothing-To-Clean", ""));


    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_ROTTEN_FLESH_LEVEL_UP", new Message("In-Game.Messages.Village.Rotten-Flesh-Level-Up", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_VILLAGER_DIED", new Message("In-Game.Messages.Village.Villager.Died", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_VILLAGER_NAMES", new Message("In-Game.Messages.Village.Villager.Names", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_VILLAGER_SPECIAL_OFFER", new Message("In-Game.Messages.Village.Villager.Special-Offer", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_VILLAGER_OFFERING_SALE", new Message("In-Game.Messages.Village.Villager.Offering-Sale", ""));

    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_VILLAGER_ROTTEN_OFFER", new Message("In-Game.Messages.Village.Villager.Rotten-Offer", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_VILLAGER_ROTTEN_SALE", new Message("In-Game.Messages.Village.Villager.Rotten-Sale", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_VILLAGER_PINATA_EVENT", new Message("In-Game.Messages.Village.Villager.Pinata-Event", ""));

    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_STUCK_ZOMBIES", new Message("In-Game.Messages.Village.Wave.Stuck-Zombies", ""));
    /*unused*/
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_SPECTATOR_WARNING", new Message("In-Game.Messages.Village.Wave.Spectator-Warning", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_RESPAWN_ON_NEXT", new Message("In-Game.Messages.Village.Wave.Respawn-On-Next", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_RESPAWNED", new Message("In-Game.Messages.Village.Wave.Respawned", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_NEXT_IN", new Message("In-Game.Messages.Village.Wave.Next-In", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_STARTED", new Message("In-Game.Messages.Village.Wave.Started", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_TITLE_START", new Message("In-Game.Messages.Village.Wave.Title.Start", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_TITLE_END", new Message("In-Game.Messages.Village.Wave.Title.End", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_TITLE_STARTING_3", new Message("In-Game.Messages.Village.Wave.Title.Start-3", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_TITLE_STARTING_2", new Message("In-Game.Messages.Village.Wave.Title.Start-2", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_TITLE_STARTING_1", new Message("In-Game.Messages.Village.Wave.Title.Start-1", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_CANT_RIDE_OTHER", new Message("In-Game.Messages.Village.Entities.Cant-Ride-Other", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_CANT_UPGRADE_THIS", new Message("In-Game.Messages.Village.Entities.Cant-Upgrade-This", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_CANT_UPGRADE_OTHER", new Message("In-Game.Messages.Village.Entities.Cant-Upgrade-Other", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_WOLF_SPAWN", new Message("In-Game.Messages.Village.Entities.Wolf.Spawn", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_WOLF_NAME", new Message("In-Game.Messages.Village.Entities.Wolf.Name", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_WOLF_DEATH", new Message("In-Game.Messages.Village.Entities.Wolf.Death", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_GOLEM_DEATH", new Message("In-Game.Messages.Village.Entities.Golem.Death", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_GOLEM_SPAWN", new Message("In-Game.Messages.Village.Entities.Golem.Spawn", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_GOLEM_NAME", new Message("In-Game.Messages.Village.Entities.Golem.Name", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_ZOMBIE_STUNNED_NAME", new Message("In-Game.Messages.Village.Entities.Zombie.Stunned-Name", ""));


    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_SHOP_GUI", new Message("In-Game.Messages.Village.Shop.GUI", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_SHOP_GOLEM_ITEM", new Message("In-Game.Messages.Village.Shop.Golem-Item-Name", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_SHOP_WOLF_ITEM", new Message("In-Game.Messages.Village.Shop.Wolf-Item-Name", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_SHOP_MOB_LIMIT_REACHED", new Message("In-Game.Messages.Village.Shop.Mob-Limit-Reached", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_SHOP_NOT_ENOUGH_CURRENCY", new Message("In-Game.Messages.Village.Shop.Not-Enough-Currency", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_SHOP_CURRENCY", new Message("In-Game.Messages.Village.Shop.Currency", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_SHOP_WAVE_LOCK", new Message("In-Game.Messages.Village.Shop.Wave-Lock", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_SHOP_ITEM_LOCKED", new Message("In-Game.Messages.Village.Shop.Item-Locked-Name", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_SHOP_WAVE_STILL_LOCKED", new Message("In-Game.Messages.Village.Shop.Wave-Still-Locked", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_SHOP_NOT_DEFINED", new Message("In-Game.Messages.Village.Shop.Not-Defined", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_SHOP_NEW_OFFERS_AVAILABLE", new Message("In-Game.Messages.Village.Shop.New-Shop-Offers", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_SHOP_SPECIAL_OFFER", new Message("In-Game.Messages.Village.Shop.Special-Offer", ""));
    getMessageManager().registerMessage("IN_GAME_MESSAGES_VILLAGE_SHOP_AUTO_ARMOR_EQUIPPED", new Message("In-Game.Messages.Village.Shop.Auto-Armor-Equipped", ""));


    getMessageManager().registerMessage("LEADERBOARD_STATISTICS_ORBS", new Message("Leaderboard.Statistics.Orbs", ""));
    getMessageManager().registerMessage("LEADERBOARD_STATISTICS_HIGHEST_WAVE", new Message("Leaderboard.Statistics.Highest-Wave", ""));
    getMessageManager().registerMessage("LEADERBOARD_STATISTICS_KILLS", new Message("Leaderboard.Statistics.Kills", ""));
    getMessageManager().registerMessage("LEADERBOARD_STATISTICS_DEATHS", new Message("Leaderboard.Statistics.Deaths", ""));


    getMessageManager().registerMessage("UPGRADE_MENU_TITLE", new Message("Upgrade-Menu.Title", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_UPGRADED_ENTITY", new Message("Upgrade-Menu.Upgraded-Entity", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_CANNOT_AFFORD", new Message("Upgrade-Menu.Cannot-Afford", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_MAX_TIER", new Message("Upgrade-Menu.Max-Tier", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_STATS_ITEM_NAME", new Message("Upgrade-Menu.Stats-Item.Name", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_STATS_ITEM_DESCRIPTION", new Message("Upgrade-Menu.Stats-Item.Description", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_UPGRADES_HEALTH_NAME", new Message("Upgrade-Menu.Upgrades.Health.Name", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_UPGRADES_HEALTH_DESCRIPTION", new Message("Upgrade-Menu.Upgrades.Health.Description", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_UPGRADES_DAMAGE_NAME", new Message("Upgrade-Menu.Upgrades.Damage.Name", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_UPGRADES_DAMAGE_DESCRIPTION", new Message("Upgrade-Menu.Upgrades.Damage.Description", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_UPGRADES_SPEED_NAME", new Message("Upgrade-Menu.Upgrades.Speed.Name", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_UPGRADES_SPEED_DESCRIPTION", new Message("Upgrade-Menu.Upgrades.Speed.Description", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_UPGRADES_SWARM_NAME", new Message("Upgrade-Menu.Upgrades.Swarm-Awareness.Name", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_UPGRADES_SWARM_DESCRIPTION", new Message("Upgrade-Menu.Upgrades.Swarm-Awareness.Description", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_UPGRADES_DEFENSE_NAME", new Message("Upgrade-Menu.Upgrades.Final-Defense.Name", ""));
    getMessageManager().registerMessage("UPGRADE_MENU_UPGRADES_DEFENSE_DESCRIPTION", new Message("Upgrade-Menu.Upgrades.Final-Defense.Description", ""));

    //KNIGHT

    getMessageManager().registerMessage("KIT_CONTENT_KNIGHT_NAME", new Message("Kit.Content.Knight.Name", ""));
    getMessageManager().registerMessage("KIT_CONTENT_KNIGHT_DESCRIPTION", new Message("Kit.Content.Knight.Description", ""));

    //GENERAL FOR KITS
    getMessageManager().registerMessage("KIT_LOCKED_TILL", new Message("Kit.Locked-Till", ""));
    getMessageManager().registerMessage("KIT_ABILITY_UNLOCKED", new Message("Kit.Ability-Unlocked", ""));
    getMessageManager().registerMessage("KIT_ABILITY_POWER_INCREASED", new Message("Kit.Ability-Power-Increased", ""));
    getMessageManager().registerMessage("KIT_PASSIVE_POWER_INCREASED", new Message("Kit.Passive-Power-Increased", ""));
  }

  private void registerLocales() {
    Arrays.asList(new Locale("Chinese (Traditional)", "简体中文", "zh_HK", "POEditor contributors", Arrays.asList("中文(傳統)", "中國傳統", "chinese_traditional", "zh")),
            new Locale("Chinese (Simplified)", "简体中文", "zh_CN", "POEditor contributors", Arrays.asList("简体中文", "中文", "chinese", "chinese_simplified", "cn")),
            new Locale("Czech", "Český", "cs_CZ", "POEditor contributors", Arrays.asList("czech", "cesky", "český", "cs")),
            new Locale("Dutch", "Nederlands", "nl_NL", "POEditor contributors", Arrays.asList("dutch", "nederlands", "nl")),
            new Locale("English", "English", "en_GB", "Tigerpanzer_02", Arrays.asList("default", "english", "en")),
            new Locale("French", "Français", "fr_FR", "POEditor contributors", Arrays.asList("french", "francais", "français", "fr")),
            new Locale("German", "Deutsch", "de_DE", "Tigerkatze and POEditor contributors", Arrays.asList("deutsch", "german", "de")),
            new Locale("Italian", "Italiano", "it_IT", "POEditor contributors", Arrays.asList("italian", "italiano", "it")),
            new Locale("Polish", "Polski", "pl_PL", "Plajer", Arrays.asList("polish", "polski", "pl")),
            new Locale("Portuguese", "Português", "pt_PT", "POEditor contributors", Arrays.asList("portuguese", "pt-pt", "pt_pt")),
            new Locale("Portuguese (BR)", "Português Brasileiro", "pt_BR", "POEditor contributors", Arrays.asList("brazilian", "brasil", "brasileiro", "pt-br", "pt_br")),
            new Locale("Russian", "Pусский", "ru_RU", "POEditor contributors", Arrays.asList("russian", "pусский", "pyccknn", "russkiy", "ru")),
            new Locale("Spanish", "Español", "es_ES", "POEditor contributors", Arrays.asList("spanish", "espanol", "español", "es")),
            new Locale("Turkish", "Türk", "tr_TR", "POEditor contributors", Arrays.asList("turkish", "turk", "türk", "tr")),
            new Locale("Vietnamese", "Việt", "vn_VN", "POEditor contributors", Arrays.asList("vietnamese", "viet", "việt", "vn")))
        .forEach(LocaleRegistry::registerLocale);
  }

  private MessageManager getMessageManager() {
    return plugin.getMessageManager();
  }

}
