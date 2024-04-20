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

package plugily.projects.villagedefense.arena.record;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.minigamesbox.classic.kits.basekits.Kit;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.user.VDUser;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles creation of users records on particular maps
 */
//todo records announce on game end
//todo record per map placeholder (papi)
public class RecordsManager {

  private final Main plugin;

  public RecordsManager(Main plugin) {
    this.plugin = plugin;
  }

  /**
   * Attempts to register survived gameplay wave as a player record if possible.
   * If new record is made an announcement is sent to the player.
   */
  public void registerAndAnnounceRecord(Player player, Arena arena) {
    Kit kit = plugin.getUserManager().getUser(player).getKit();
    VDUser vdUser = plugin.getVdUserManager().getUser(player);
    List<VDUser.GameplayRecord> records = vdUser.getGameplayRecords()
      .getOrDefault(arena.getMapName(), new ArrayList<>())
      .stream()
      .filter(record -> record.getMapId().equals(arena.getMapName()))
      .collect(Collectors.toList());
    VDUser.GameplayRecord newRecord = new VDUser.GameplayRecord(arena.getMapName(), kit.getName(), arena.getWave());
    records.add(newRecord);
    List<VDUser.GameplayRecord> updatedRecords = records
      .stream()
      .sorted(Comparator.comparing(VDUser.GameplayRecord::getWave).reversed())
      .limit(3)
      .collect(Collectors.toList());
    vdUser.getGameplayRecords().put(arena.getMapName(), updatedRecords);
    if (!updatedRecords.contains(newRecord)) {
      return;
    }
    player.playSound(player, Sound.ENTITY_VILLAGER_YES, 1, 1);
    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1.25f);
    new MessageBuilder("VD_GOLD_NEW_RECORD_REACHED").asKey().player(player).value(kit.getName()).sendPlayer();
  }

}
