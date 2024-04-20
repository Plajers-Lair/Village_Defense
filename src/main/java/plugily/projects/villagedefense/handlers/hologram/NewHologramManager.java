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

package plugily.projects.villagedefense.handlers.hologram;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import plugily.projects.minigamesbox.classic.PluginMain;
import plugily.projects.minigamesbox.classic.utils.version.events.api.PlugilyEntityPickupItemEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 06.10.2023
 */
public class NewHologramManager implements Listener {

  private final PluginMain plugin;
  private final List<ArmorStand> armorStands = new ArrayList<>();
  private final List<ArmorStandHologram> holograms = new ArrayList<>();
  public NewHologramManager(PluginMain plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
  }

  public List<ArmorStand> getArmorStands() {
    return armorStands;
  }

  public List<ArmorStandHologram> getHolograms() {
    return holograms;
  }

  @EventHandler
  public void onHologramDamage(EntityDamageByEntityEvent event) {
    if (!event.getEntity().hasMetadata("PLUGILY_HOLOGRAM")) {
      return;
    }
    if (!(event.getDamager() instanceof Player)) {
      return;
    }
    if (onTouch((Player) event.getDamager(), event.getEntity())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onHologramTouch(PlayerInteractAtEntityEvent event) {
    if (!event.getRightClicked().hasMetadata("PLUGILY_HOLOGRAM")) {
      return;
    }
    if (onTouch(event.getPlayer(), event.getRightClicked())) {
      event.setCancelled(true);
    }
  }

  private boolean onTouch(Player player, Entity entity) {
    for (ArmorStandHologram hologram : holograms) {
      if (!hologram.hasTouchHandler()) {
        continue;
      }
      Item entityItem = hologram.getEntityItem();
      boolean touch = false;
      if (entity.equals(entityItem)) {
        touch = true;
      }
      if (!touch) {
        for (ArmorStand stand : hologram.getArmorStands()) {
          if (entity.equals(stand)) {
            touch = true;
            break;
          }
        }
      }
      if (touch) {
        if (plugin.getUserManager().getUser(player).isSpectator()) {
          return false;
        }
        hologram.getTouchHandler().onTouch(player);
        return true;
      }
    }
    return false;
  }

  @EventHandler
  public void onItemPickup(PlugilyEntityPickupItemEvent event) {
    if (!event.getItem().hasMetadata("PLUGILY_HOLOGRAM")) {
      return;
    }
    if (!(event.getEntity() instanceof Player)) {
      return;
    }
    Player player = (Player) event.getEntity();
    if (plugin.getUserManager().getUser(player).getArena() == null) {
      return;
    }
    for (ArmorStandHologram hologram : holograms) {
      if (!hologram.hasPickupHandler()) {
        continue;
      }
      Item entityItem = hologram.getEntityItem();
      Item item = event.getItem();
      if (item.equals(entityItem)) {
        if (plugin.getUserManager().getUser(player).isSpectator()) {
          return;
        }
        event.setCancelled(true);
        hologram.getPickupHandler().onPickup(player);
        return;
      }
    }
    //pickup not handled for known hologram, cancel it
    event.setCancelled(true);
  }

}
