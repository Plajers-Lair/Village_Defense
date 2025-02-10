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

package plugily.projects.villagedefense.handlers.upgrade;

import org.bukkit.entity.EntityType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityUpgrade {

  private String id;
  private String metadataKey;
  private String name;
  private List<String> description;
  private int slot;
  private int cost;
  private EntityType applicableEntity;
  private boolean hidden = false;
  private int surviveWaves = -1;
  private String dependsOn;
  private Map<String, Double> upgradeData = new HashMap<>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMetadataKey() {
    return metadataKey;
  }

  public void setMetadataKey(String metadataKey) {
    this.metadataKey = metadataKey;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getDescription() {
    return description;
  }

  public void setDescription(List<String> description) {
    this.description = description;
  }

  public int getSlot() {
    return slot;
  }

  public void setSlot(int slot) {
    this.slot = slot;
  }

  public int getCost() {
    return cost;
  }

  public void setCost(int cost) {
    this.cost = cost;
  }

  public EntityType getApplicableEntity() {
    return applicableEntity;
  }

  public void setApplicableEntity(EntityType applicableEntity) {
    this.applicableEntity = applicableEntity;
  }

  public boolean isHidden() {
    return hidden;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

  public int getSurviveWaves() {
    return surviveWaves;
  }

  public void setSurviveWaves(int surviveWaves) {
    this.surviveWaves = surviveWaves;
  }

  public String getDependsOn() {
    return dependsOn;
  }

  public void setDependsOn(String dependsOn) {
    this.dependsOn = dependsOn;
  }

  public String getDependencyMetadataKey() {
    return "VD_" + dependsOn;
  }

  public Map<String, Double> getUpgradeData() {
    return upgradeData;
  }

  public static class Builder {

    private EntityUpgrade upgrade = new EntityUpgrade();

    public Builder withId(String id) {
      upgrade.setId(id);
      upgrade.setMetadataKey("VD_" + id);
      return this;
    }

    public Builder withName(String name) {
      upgrade.setName(name);
      return this;
    }

    public Builder withDescription(List<String> description) {
      upgrade.setDescription(description);
      return this;
    }

    public Builder atSlot(int slot) {
      upgrade.setSlot(slot);
      return this;
    }

    public Builder withCost(int cost) {
      upgrade.setCost(cost);
      return this;
    }

    public Builder withApplicableEntity(EntityType applicableEntity) {
      upgrade.setApplicableEntity(applicableEntity);
      return this;
    }

    public Builder isHidden(int waveUnlock) {
      upgrade.setSurviveWaves(waveUnlock);
      upgrade.setHidden(true);
      return this;
    }

    public Builder andDependsOn(String id) {
      upgrade.setDependsOn(id);
      return this;
    }

    public Builder putUpgradeData(String key, double value) {
      upgrade.upgradeData.put(key, value);
      return this;
    }

    public EntityUpgrade build() {
      return upgrade;
    }

  }
}