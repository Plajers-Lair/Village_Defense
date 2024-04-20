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

package plugily.projects.villagedefense.events;

import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.minigamesbox.classic.utils.version.events.api.PlugilyPlayerInteractEntityEvent;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.handlers.upgrade.NewEntityUpgradeManager;

import java.util.UUID;

/**
 * @author Plajer
 * <p>
 * Created at 18.06.2019
 */
public class EntityUpgradeListener implements Listener {

  private final Main plugin;

  public EntityUpgradeListener(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  @EventHandler
  public void onEntityClick(PlugilyPlayerInteractEntityEvent event) {
    if((event.getRightClicked().getType() != EntityType.IRON_GOLEM && event.getRightClicked().getType() != EntityType.WOLF)
      || VersionUtils.checkOffHand(event.getHand())
      || !event.getPlayer().isSneaking()
      || !event.getRightClicked().hasMetadata("VD_OWNER_UUID")
      || plugin.getArenaRegistry().getArena(event.getPlayer()) == null) {
      return;
    }
    if(plugin.getUserManager().getUser(event.getPlayer()).isSpectator()) {
      return;
    }
    UUID uuid = UUID.fromString(event.getRightClicked().getMetadata("VD_OWNER_UUID").get(0).asString());
    if(!event.getPlayer().getUniqueId().equals(uuid)) {
      new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_CANT_UPGRADE_OTHER").asKey().player(event.getPlayer()).sendPlayer();
      event.getPlayer().playSound(event.getPlayer(), Sound.ENTITY_VILLAGER_NO, 1, 1);
      return;
    }
    if (event.getRightClicked().hasMetadata(NewEntityUpgradeManager.UPGRADES_DISABLED_METADATA)) {
      new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_WAVE_ENTITIES_CANT_UPGRADE_THIS").asKey().player(event.getPlayer()).sendPlayer();
      event.getPlayer().playSound(event.getPlayer(), Sound.ENTITY_VILLAGER_NO, 1, 1);
      return;
    }
    plugin.getEntityUpgradeManager().openUpgradeMenu((LivingEntity) event.getRightClicked(), event.getPlayer());
  }

}
