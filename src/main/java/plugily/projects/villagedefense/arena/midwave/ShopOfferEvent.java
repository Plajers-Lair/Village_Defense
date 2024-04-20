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

package plugily.projects.villagedefense.arena.midwave;

import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.utils.hologram.ArmorStandHologram;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.managers.ShopManager;
import plugily.projects.villagedefense.creatures.v1_9_UP.CustomCreature;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class ShopOfferEvent implements MidWaveEvent {

  private final Main plugin;

  public ShopOfferEvent(Main plugin) {
    this.plugin = plugin;
  }

  @Override
  public boolean canTrigger(Arena arena) {
    return arena.getWave() % 5 == 0;
  }

  @Override
  public void initiate(Arena arena) {
    List<Villager> list = arena.getVillagers()
      .stream()
      .filter(v -> !v.isDead())
      .collect(Collectors.toList());
    Villager target = list.get(ThreadLocalRandom.current().nextInt(list.size()));
    target.setGlowing(true);

    String offerMessage = new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_VILLAGER_SPECIAL_OFFER").asKey().build();
    ArmorStandHologram hologram = new ArmorStandHologram(target.getLocation().add(0, 0.25, 0), offerMessage);
    new BukkitRunnable() {
      @Override
      public void run() {
        if (!target.hasMetadata(ShopManager.SHOP_OFFER_METADATA)) {
          hologram.delete();
          cancel();
          return;
        }
        ArmorStand stand = hologram.getArmorStands().get(0);
        stand.teleport(target.getLocation().add(0, 0.25, 0));
      }
    }.runTaskTimer(plugin, 0, 1);
    target.setMetadata(ShopManager.SHOP_OFFER_METADATA, new FixedMetadataValue(plugin, true));
    new MessageBuilder("IN_GAME_MESSAGES_VILLAGE_VILLAGER_OFFERING_SALE")
      .asKey()
      .value(target.getMetadata(CustomCreature.CREATURE_CUSTOM_NAME_METADATA).get(0).asString())
      .arena(arena)
      .sendArena();
    for (Player player : arena.getPlayers()) {
      player.playSound(player, Sound.ENTITY_VILLAGER_TRADE, 1, 1.15f);
    }
  }

  @Override
  public void cleanup(Arena arena) {
    for (Villager villager : arena.getVillagers()) {
      if (villager.hasMetadata(ShopManager.SHOP_OFFER_METADATA)) {
        villager.setGlowing(false);
        villager.removeMetadata(ShopManager.SHOP_OFFER_METADATA, plugin);
      }
    }
  }
}
