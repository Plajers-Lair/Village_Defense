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

package plugily.projects.villagedefense.creatures.v1_9_UP;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.util.Vector;
import plugily.projects.villagedefense.Main;

import java.util.Optional;

/**
 * @author Plajer
 * <p>
 * Created at 04.08.2023
 */
public class RideableCreatureEvents {

  private Main plugin;

  public RideableCreatureEvents(Main plugin) {
    if(plugin.getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
      return;
    }
    this.plugin = plugin;
    ProtocolManager manager = ProtocolLibrary.getProtocolManager();
    manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.STEER_VEHICLE) {
      @Override
      public void onPacketReceiving(PacketEvent event) {
        handlePreSteer(event);
      }
    });
  }

  private void handlePreSteer(PacketEvent event) {
    Entity vehicle = event.getPlayer().getVehicle();
    if(vehicle == null) {
      return;
    }
    CustomRideableCreature.RideableType type = null;
    for(CustomRideableCreature.RideableType rideableType : CustomRideableCreature.RideableType.values()) {
      if(rideableType.name().equals(vehicle.getType().name().toUpperCase())) {
        type = rideableType;
        break;
      }
    }
    if(type == null) {
      return;
    }
    Optional<CustomRideableCreature> customRideableCreatureOptional = plugin.getEnemySpawnerRegistry().getRideableCreatureByName(type);
    if(!customRideableCreatureOptional.isPresent()) {
      return;
    }
    handleSteer(event, vehicle);
  }

  private void handleSteer(PacketEvent event, Entity vehicle) {
    Player player = event.getPlayer();
    PacketContainer packet = event.getPacket();
    //https://wiki.vg/Protocol#Player_Input
    float sideways = packet.getFloat().read(0);
    float forward = packet.getFloat().read(1);
    boolean jump = packet.getBooleans().read(0);
    boolean unmount = packet.getBooleans().read(1);
    if(unmount) {
      return;
    }
    Location location = player.getLocation();
    double radians = Math.toRadians(location.getYaw());
    double x = -forward * Math.sin(radians) + sideways * Math.cos(radians);
    double z = forward * Math.cos(radians) + sideways * Math.sin(radians);
    double multiplier = 0.4;
    if (vehicle instanceof Villager) {
      multiplier = 0.25;
    }
    Vector velocity = new Vector(x, 0.0, z).normalize().multiply(multiplier);
    velocity.setY(vehicle.getVelocity().getY());
    if(!Double.isFinite(velocity.getX())) {
      velocity.setX(0);
    }
    if(!Double.isFinite(velocity.getZ())) {
      velocity.setZ(0);
    }
    if(jump && vehicle.isOnGround()) {
      velocity.setY(0.45);
    }
    try {
      velocity.checkFinite();
      vehicle.setVelocity(velocity);
    } catch(Exception ignored) {
    }
  }

}
