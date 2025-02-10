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

package plugily.projects.villagedefense.arena.villager.trait;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import plugily.projects.minigamesbox.classic.utils.version.xseries.XSound;
import plugily.projects.villagedefense.Main;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.arena.managers.spawner.gold.NewCreatureUtils;
import plugily.projects.villagedefense.arena.villager.VillagerAiAnimations;
import plugily.projects.villagedefense.arena.villager.VillagerAiManager;
import plugily.projects.villagedefense.utils.NearbyUtils;

import java.util.concurrent.ThreadLocalRandom;

public class AlchemistTrait implements GenericTrait {

  private final Main plugin = JavaPlugin.getPlugin(Main.class);
  private final VillagerAiManager aiManager;

  public AlchemistTrait(VillagerAiManager aiManager) {
    this.aiManager = aiManager;
  }

  @Override
  public VillagerAiManager.Personality getPersonality() {
    return VillagerAiManager.Personality.ALCHEMIST;
  }

  @Override
  public void onSocialize(Arena arena, Villager villager) {
    int roll = ThreadLocalRandom.current().nextInt(0, 3);
    if (roll == 2) {
      doPotionTricks(villager);
    }
  }

  private void doPotionTricks(Villager villager) {
    ItemStack cachedItem = villager.getEquipment().getItemInMainHand();
    int eatTime = ThreadLocalRandom.current().nextInt(40, 65);
    VillagerAiAnimations.makeEntityEat(villager, new ItemStack(Material.POTION), eatTime, () -> {
      if (ThreadLocalRandom.current().nextBoolean()) {
        villager.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 70, 0));
        villager.getWorld().playSound(villager.getLocation(), XSound.ENTITY_VILLAGER_CELEBRATE.parseSound(), 1.0f, 0.75f);
      } else {
        villager.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 20 * 7, 0));
        villager.getWorld().playSound(villager.getLocation(), XSound.ENTITY_VILLAGER_YES.parseSound(), 1.0f, 1.0f);
      }
      villager.getEquipment().setItemInMainHand(cachedItem);
    });
  }

  @Override
  public void onRetreat(Arena arena, Villager villager) {
    new BukkitRunnable() {
      @Override
      public void run() {
        if (!arena.isFighting() || villager.isDead() || villager.hasMetadata("VD_ALCHEMIST_RETREAT")) {
          this.cancel();
          return;
        }
        ItemStack cachedItem = villager.getEquipment().getItemInMainHand();
        for (Entity entity : villager.getNearbyEntities(5, 5, 5)) {
          if (!NewCreatureUtils.isEnemy(entity)) {
            continue;
          }
          VillagerAiAnimations.makeEntityLookAt(villager, ((LivingEntity) entity).getEyeLocation());
          Bukkit.getScheduler().runTaskLater(plugin, () -> {
            villager.setMetadata("VD_ALCHEMIST_RETREAT", new FixedMetadataValue(plugin, true));
            ItemStack potion = new ItemStack(Material.SPLASH_POTION);
            PotionMeta meta = (PotionMeta) potion.getItemMeta();
            meta.addCustomEffect(new PotionEffect(PotionEffectType.HEAL, 5, 1), true);
            potion.setItemMeta(meta);
            ThrownPotion thrownPotion = villager.getWorld().spawn(villager.getEyeLocation(), ThrownPotion.class);
            thrownPotion.setItem(potion);
            thrownPotion.setPotionMeta(meta);
            Vector direction = entity.getLocation().toVector().subtract(villager.getEyeLocation().toVector()).normalize();
            thrownPotion.setVelocity(direction.multiply(1.5));
            villager.getEquipment().setItemInMainHand(null);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
              int homeIndex = villager.getMetadata(VillagerAiManager.VILLAGER_PERSONALITY_CHOSEN_HOME_ID).get(0).asInt();
              Location home = NearbyUtils.getRandomNearbyLocation(aiManager.getPlaces().get(VillagerAiManager.Place.VILLAGER_HOME_ZONE).get(homeIndex), 2);
              aiManager.doStartPathfinder(villager, home, (v, l) -> {
              });
              Bukkit.getScheduler().runTaskLater(plugin, () -> {
                villager.getEquipment().setItemInMainHand(cachedItem);
                villager.removeMetadata("VD_ALCHEMIST_RETREAT", plugin);
              }, 20 * 10);
            }, 20);
          }, 20);
          this.cancel();
          return;
        }
      }
    }.runTaskTimer(plugin, 0, 30);
  }
}
