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

package plugily.projects.villagedefense.arena.assist;

import org.bukkit.Bukkit;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.metadata.FixedMetadataValue;
import plugily.projects.villagedefense.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class dedicated to handle metadata for players who buff
 * or help their allies in any other way.
 * This class is a middleman between sharing orbs on enemy death
 * both for the killer and all assisting players (including
 * allies that buffed the killer)
 *
 * @author Plajer
 * <p>
 * Created at 29.08.2023
 */
public class AssistHandler {

  public static final String ASSIST_CONTAINER_METADATA = "VD_ASSIST_CONTAINER";
  private Main plugin;

  public AssistHandler(Main plugin) {
    this.plugin = plugin;
  }

  public void doRegisterDamageOnEnemy(LivingEntity source, Creature target, double damage) {
    AssistContainer container = new AssistContainer();
    if(target.hasMetadata(ASSIST_CONTAINER_METADATA)) {
      container = AssistContainer.deserialize(target.getMetadata(ASSIST_CONTAINER_METADATA).get(0).asString());
    }
    container.updateAssist(source, AssistContainer.AssistData.AssistType.DAMAGE, damage);
    target.setMetadata(ASSIST_CONTAINER_METADATA, new FixedMetadataValue(plugin, container.serialize()));
  }

  public void doRegisterDebuffOnEnemy(LivingEntity source, Creature target) {
    AssistContainer container = new AssistContainer();
    if(target.hasMetadata(ASSIST_CONTAINER_METADATA)) {
      container = AssistContainer.deserialize(target.getMetadata(ASSIST_CONTAINER_METADATA).get(0).asString());
    }
    container.updateAssist(source, AssistContainer.AssistData.AssistType.DEBUFF);
    target.setMetadata(ASSIST_CONTAINER_METADATA, new FixedMetadataValue(plugin, container.serialize()));
  }

  public void doRegisterBuffOnAlly(LivingEntity source, LivingEntity target) {
    AssistContainer container = new AssistContainer();
    if(target.hasMetadata(ASSIST_CONTAINER_METADATA)) {
      container = AssistContainer.deserialize(target.getMetadata(ASSIST_CONTAINER_METADATA).get(0).asString());
    }
    container.updateAssist(source, AssistContainer.AssistData.AssistType.BUFF);
    target.setMetadata(ASSIST_CONTAINER_METADATA, new FixedMetadataValue(plugin, container.serialize()));
  }

  /**
   * Distribute orb from killed enemy to all people that assisted the killer.
   * Distribution map is a map of player and their percentage contribution.
   * <p>
   * If killer is a pet, owner will receive 100% of kill participation
   * unless the pet was assisted by any other ally.
   * <p>
   * If killer is a player, player will receive 100% of kill participation
   * unless they were assisted by any other ally.
   * <p>
   * Moreover, deadEnemy is checked for any debuffs it received as well
   * so assist is split between people that applied debuffs as well.
   *
   * @param killer    the entity that killed deadEnemy
   * @param deadEnemy enemy that died in the event
   * @return map of percentage distribution of orbs given by deadEnemy
   */
  public Map<Player, Double> doDistributeAssistRewards(LivingEntity killer, Creature deadEnemy) {
    Player killerOwner = getRealKiller(killer);
    if(killerOwner == null) {
      plugin.getDebugger().debug("No owner of " + killer.getType() + " found, cannot distribute orb assists.");
      return new HashMap<>();
    }
    Map<Player, Double> damageDone = new LinkedHashMap<>();
    List<Player> otherAssists = new ArrayList<>();
    //enemy was debuffed or attacked by someone, check it
    if(deadEnemy.hasMetadata(ASSIST_CONTAINER_METADATA)) {
      AssistContainer debuffers = AssistContainer.deserialize(deadEnemy.getMetadata(ASSIST_CONTAINER_METADATA).get(0).asString());
      List<AssistContainer.AssistData> assistData = debuffers.getData();
      for(AssistContainer.AssistData data : assistData) {
        Player assister = getAssisterByUuid(data.getUuid());
        if(assister == null || assister.equals(killerOwner)) {
          continue;
        }
        //only count indirect (debuffs) or damage to the enemy
        switch(data.getType()) {
          case DAMAGE:
            Entity entity = Bukkit.getEntity(data.getUuid());
            damageDone.put(assister, data.getValue());
            break;
          case DEBUFF:
            otherAssists.add(assister);
            break;
        }
      }
    }
    //check if killer (either pet or player) has any assist buffs
    if(killer.hasMetadata(ASSIST_CONTAINER_METADATA)) {
      AssistContainer assisters = AssistContainer.deserialize(killer.getMetadata(ASSIST_CONTAINER_METADATA).get(0).asString());
      List<AssistContainer.AssistData> assistData = assisters.getData();
      for(AssistContainer.AssistData data : assistData) {
        Player assister = getAssisterByUuid(data.getUuid());
        if(assister == null || data.getType() != AssistContainer.AssistData.AssistType.BUFF) {
          continue;
        }
        //only count buffs (within 10 seconds) to the killer
        if(System.currentTimeMillis() - data.getTimeMillis() <= 10000) {
          otherAssists.add(assister);
        }
      }
    }
    return doDistributeMap(killerOwner, damageDone, otherAssists);
  }

  //aims to get owner of the wolf/golem or player if applicable
  private Player getRealKiller(LivingEntity killer) {
    Player realKiller = null;
    if(killer instanceof Player) {
      realKiller = (Player) killer;
    } else if(killer instanceof Wolf || killer instanceof IronGolem && killer.hasMetadata("VD_OWNER_UUID")) {
      realKiller = Bukkit.getPlayer(UUID.fromString(killer.getMetadata("VD_OWNER_UUID").get(0).asString()));
    }
    return realKiller;
  }

  //aims to get the owner of the assisting wolf/golem or player if applicable
  private Player getAssisterByUuid(UUID uuid) {
    Entity entity = Bukkit.getEntity(uuid);
    if(entity == null) {
      return null;
    }
    if(entity instanceof Player) {
      return (Player) entity;
    }
    if(!entity.hasMetadata("VD_OWNER_UUID")) {
      return null;
    }
    return Bukkit.getPlayer(UUID.fromString(entity.getMetadata("VD_OWNER_UUID").get(0).asString()));
  }

  //50% / 50% split between killer and assisters
  //and 50% split based on damage contribution (weighted)
  //and 50% split based on other assists (split evenly)
  private Map<Player, Double> doDistributeMap(Player killer, Map<Player, Double> damage, List<Player> otherAssists) {
    double total = 100.0;
    Map<Player, Double> shares = new HashMap<>();
    double killerShare = 50.0;
    if(damage.isEmpty() && otherAssists.isEmpty()) {
      killerShare = 100.0;
    }
    shares.put(killer, killerShare);
    double totalDamageShare = 0.0;
    for(Map.Entry<Player, Double> entry : damage.entrySet()) {
      double damageDealt = 1.0;
      if(entry.getValue() > 0) {
        damageDealt = entry.getValue();
      }
      totalDamageShare += damageDealt;
    }
    double assistSplit = 0.5;
    //if either is empty then assist should be shared 50/50 not 50/(25/25)
    if(damage.isEmpty() || otherAssists.isEmpty()) {
      assistSplit = 1.0;
    }
    for(Map.Entry<Player, Double> entry : damage.entrySet()) {
      double damageShare = (entry.getValue() / totalDamageShare) * assistSplit * (total - killerShare);
      shares.put(entry.getKey(), damageShare);
    }

    double otherAssistsShare = assistSplit * (total - killerShare);
    for(Player player : otherAssists) {
      shares.put(player, otherAssistsShare / otherAssists.size());
    }
    return shares;
  }

}
