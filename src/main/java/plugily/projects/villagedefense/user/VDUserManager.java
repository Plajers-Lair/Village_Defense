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

package plugily.projects.villagedefense.user;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import plugily.projects.minigamesbox.classic.utils.configuration.ConfigUtils;
import plugily.projects.villagedefense.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class VDUserManager implements Listener {

  private final Main plugin;
  private final List<VDUser> users = new ArrayList<>();
  private final FileConfiguration usersFile;

  public VDUserManager(Main plugin) {
    this.plugin = plugin;
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    this.usersFile = ConfigUtils.getConfig(plugin, "vd_user_data");
    for (Player player : Bukkit.getOnlinePlayers()) {
      users.add(load(player));
    }
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    users.add(load(event.getPlayer()));
    event.getPlayer().setResourcePack(
      "https://static.plajer.xyz/villagedefense/lookandfeelv1_1.zip",
      "f221d030f380026ffef1202c7a2f538c",
      true,
      Component.text("We kindly ask to download required Village Defense resource pack for better game experience.")
    );
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    VDUser target = users.stream()
      .filter(user -> user.getUuid().equals(event.getPlayer().getUniqueId()))
      .findFirst()
      .orElse(null);
    persist(target);
    users.remove(target);
  }

  public VDUser getUser(Player player) {
    return users.stream()
      .filter(user -> user.getUuid().equals(player.getUniqueId()))
      .findFirst()
      .orElse(null);
  }

  public List<VDUser> getRegisteredUsers() {
    return users;
  }

  public VDUser load(OfflinePlayer player) {
    UUID uuid = player.getUniqueId();
    if (!usersFile.isSet(uuid.toString())) {
      return new VDUser(uuid, new HashMap<>());
    }
    Map<String, List<VDUser.GameplayRecord>> records = new HashMap<>();
    ConfigurationSection section = usersFile.getConfigurationSection(uuid + ".records");
    for (String key : section.getKeys(false)) {
      List<VDUser.GameplayRecord> localRecords = new ArrayList<>();
      for (String value : usersFile.getStringList(uuid + ".records." + key)) {
        String[] data = value.split(";");
        localRecords.add(new VDUser.GameplayRecord(key, data[0], Integer.parseInt(data[1])));
      }
      records.put(key, localRecords);
    }
    return new VDUser(uuid, records);
  }

  public void persist(VDUser user) {
    for (Map.Entry<String, List<VDUser.GameplayRecord>> entry : user.getGameplayRecords().entrySet()) {
      List<String> flatData = entry.getValue().stream()
        .map(record -> record.getKitName() + ";" + record.getWave())
        .collect(Collectors.toList());
      usersFile.set(user.getUuid() + ".records." + entry.getKey(), flatData);
    }
    ConfigUtils.saveConfig(plugin, usersFile, "vd_user_data");
  }

}
