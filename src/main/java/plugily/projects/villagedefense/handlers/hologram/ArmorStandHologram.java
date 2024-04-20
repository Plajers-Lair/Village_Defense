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

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import plugily.projects.minigamesbox.classic.utils.version.ServerVersion;
import plugily.projects.minigamesbox.classic.utils.version.VersionUtils;
import plugily.projects.villagedefense.Main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author Plajer
 * <p>
 * Created at 06.10.2023
 */
public class ArmorStandHologram {

  private static final Main plugin = JavaPlugin.getPlugin(Main.class);
  private final List<ArmorStand> armorStands = new ArrayList<>();
  private Item entityItem;
  private ItemStack item;
  private List<String> lines = new ArrayList<>();
  private Location location;
  private PickupHandler pickupHandler = null;
  private TouchHandler touchHandler = null;

  public ArmorStandHologram() {
  }

  public ArmorStandHologram(Location location) {
    this.location = location;
    plugin.getNewHologramManager().getHolograms().add(this);
  }

  public ArmorStandHologram(Location location, @NotNull String... lines) {
    this.location = location;
    this.lines = Arrays.asList(lines);
    plugin.getNewHologramManager().getHolograms().add(this);
    append();
  }

  public ArmorStandHologram(Location location, @NotNull List<String> lines) {
    this.location = location;
    this.lines = lines;
    plugin.getNewHologramManager().getHolograms().add(this);
    append();
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public ItemStack getItem() {
    return item;
  }

  public Item getEntityItem() {
    return entityItem;
  }

  @NotNull
  public List<String> getLines() {
    return lines;
  }

  @NotNull
  public List<ArmorStand> getArmorStands() {
    return armorStands;
  }

  public ArmorStandHologram overwriteLines(@NotNull String... lines) {
    this.lines = Arrays.asList(lines);
    append();
    return this;
  }

  public ArmorStandHologram overwriteLines(@NotNull List<String> lines) {
    this.lines = lines;
    append();
    return this;
  }

  public ArmorStandHologram overwriteLine(@NotNull String line) {
    this.lines = Collections.singletonList(line);
    append();
    return this;
  }

  public ArmorStandHologram appendLines(@NotNull String... lines) {
    this.lines.addAll(Arrays.asList(lines));
    append();
    return this;
  }

  public ArmorStandHologram appendLines(@NotNull List<String> lines) {
    this.lines.addAll(lines);
    append();
    return this;
  }

  public ArmorStandHologram appendLine(@NotNull String line) {
    this.lines.add(line);
    append();
    return this;
  }

  public ArmorStandHologram appendItem(@NotNull ItemStack item) {
    this.item = item;
    append();
    return this;
  }

  public void delete() {
    for (ArmorStand armor : armorStands) {
      armor.setCustomNameVisible(false);
      armor.remove();
      plugin.getNewHologramManager().getArmorStands().remove(armor);
    }
    if (entityItem != null) {
      entityItem.remove();
    }
    plugin.getNewHologramManager().getHolograms().remove(this);
    armorStands.clear();
  }

  public boolean isDeleted() {
    return entityItem == null && armorStands.isEmpty();
  }

  private void append() {
    delete();

    World world = location.getWorld();
    if (world == null) {
      return;
    }

    double distanceAbove = -0.27;
    double y = location.getY();

    for (String line : lines) {
      y += distanceAbove;
      ArmorStand armorStand = getEntityArmorStand(y);
      armorStand.setCustomName(line);
      plugin.getDebugger().debug("Creating armorstand with name {0}", line);
      armorStands.add(armorStand);
      plugin.getNewHologramManager().getArmorStands().add(armorStand);
    }

    if (item != null && item.getType() != org.bukkit.Material.AIR) {
      entityItem = world.dropItem(location, item);
      //set random uuid in meta to prevent item merging for multiple holograms being close
      ItemMeta meta = entityItem.getItemStack().getItemMeta();
      meta.setLore(Arrays.asList(UUID.randomUUID().toString()));
      entityItem.getItemStack().setItemMeta(meta);
      entityItem.setMetadata("PLUGILY_HOLOGRAM", new FixedMetadataValue(plugin, true));
      /*if(VersionUtils.isPaper()) {
        entityItem.setCanMobPickup(false);
      }*/
      entityItem.setCustomNameVisible(false);

      if (ServerVersion.Version.isCurrentEqualOrHigher(ServerVersion.Version.v1_10_R1)) {
        entityItem.setGravity(true);
      }

      if (ServerVersion.Version.isCurrentHigher(ServerVersion.Version.v1_8_R3)) {
        entityItem.setInvulnerable(true);
      }
      VersionUtils.teleport(entityItem, location);
    }
  }

  /**
   * @param y the y axis of the hologram
   * @return {@link ArmorStand}
   */
  private ArmorStand getEntityArmorStand(double y) {
    Location loc = location.clone();
    loc.setY(y);

    World world = loc.getWorld();
    if (ServerVersion.Version.isCurrentHigher(ServerVersion.Version.v1_8_R1)) {
      world.getNearbyEntities(location, 0.2, 0.2, 0.2).forEach(entity -> {
        if (entity instanceof ArmorStand && !armorStands.contains(entity) && !plugin.getNewHologramManager().getArmorStands().contains(entity)) {
          entity.remove();
          entity.setCustomNameVisible(false);
          plugin.getNewHologramManager().getArmorStands().remove(entity);
        }
      });
    }
    ArmorStand stand = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
    stand.setVisible(false);
    stand.setGravity(false);
    stand.setCustomNameVisible(true);
    stand.setMetadata("PLUGILY_HOLOGRAM", new FixedMetadataValue(plugin, true));
    return stand;
  }

  public PickupHandler getPickupHandler() {
    return pickupHandler;
  }

  /**
   * Set a handler which triggers on player pickup item event
   *
   * @param handler which should be executed on pickup
   */
  public ArmorStandHologram setPickupHandler(PickupHandler handler) {
    plugin.getNewHologramManager().getHolograms().remove(this);
    this.pickupHandler = handler;
    plugin.getNewHologramManager().getHolograms().add(this);
    return this;
  }

  public boolean hasPickupHandler() {
    return pickupHandler != null;
  }

  public TouchHandler getTouchHandler() {
    return touchHandler;
  }

  public ArmorStandHologram setTouchHandler(TouchHandler handler) {
    plugin.getNewHologramManager().getHolograms().remove(this);
    this.touchHandler = handler;
    plugin.getNewHologramManager().getHolograms().add(this);
    return this;
  }

  public boolean hasTouchHandler() {
    return touchHandler != null;
  }

}