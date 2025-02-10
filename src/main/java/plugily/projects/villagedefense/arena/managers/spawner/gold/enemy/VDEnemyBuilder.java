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

package plugily.projects.villagedefense.arena.managers.spawner.gold.enemy;

import org.bukkit.Material;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import plugily.projects.villagedefense.arena.Arena;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public class VDEnemyBuilder {

  private final VDEnemy vdEnemy = new VDEnemy();

  public VDEnemyBuilder(String id) {
    vdEnemy.setId(id);
  }

  public VDEnemyBuilder withName(String name) {
    vdEnemy.setName(name);
    return this;
  }

  public VDEnemyBuilder withTargetPriority(EntityType priority) {
    vdEnemy.setTargetPriority(priority);
    return this;
  }

  public VDEnemyBuilder withPersistentTargeting() {
    vdEnemy.setPersistentTargeting(true);
    return this;
  }

  public VDEnemyBuilder withWaveMinimum(int waveMinimum) {
    vdEnemy.setWaveMinimum(waveMinimum);
    return this;
  }

  public VDEnemyBuilder withWaveMaximum(int waveMaximum) {
    vdEnemy.setWaveMaximum(waveMaximum);
    return this;
  }

  public VDEnemyBuilder withFullArmor(ArmorType armorType, int waveMin) {
    return withFullArmor(armorType, waveMin, 1, 100);
  }

  public VDEnemyBuilder withFullArmor(ArmorType armorType, int waveMin, int priority) {
    return withFullArmor(armorType, waveMin, priority, 100);
  }

  public VDEnemyBuilder withFullArmor(ArmorType armorType, int waveMin, int priority, double chance) {
    switch (armorType) {
      case LEATHER -> {
        withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.LEATHER_HELMET)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.LEATHER_CHESTPLATE)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.LEATHER_LEGGINGS)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.LEATHER_BOOTS)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
      }
      case CHAINMAIL -> {
        withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.CHAINMAIL_HELMET)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.CHAINMAIL_CHESTPLATE)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.CHAINMAIL_LEGGINGS)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.CHAINMAIL_BOOTS)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
      }
      case GOLD -> {
        withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.GOLDEN_HELMET)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.GOLDEN_CHESTPLATE)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.GOLDEN_LEGGINGS)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.GOLDEN_BOOTS)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
      }
      case IRON -> {
        withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.IRON_HELMET)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.IRON_CHESTPLATE)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.IRON_LEGGINGS)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.IRON_BOOTS)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
      }
      case DIAMOND -> {
        withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.DIAMOND_HELMET)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.DIAMOND_CHESTPLATE)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.DIAMOND_LEGGINGS)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.DIAMOND_BOOTS)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
      }
      case NETHERITE -> {
        withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.NETHERITE_HELMET)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.NETHERITE_CHESTPLATE)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.NETHERITE_LEGGINGS)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.NETHERITE_BOOTS)).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
      }
    }
    return this;
  }

  public VDEnemyBuilder withFullArmor(ArmorType armorType, int waveMin, int priority, ArmorPiece.Enchant... enchants) {
    return withFullArmor(armorType, waveMin, priority, 100, enchants);
  }

  public VDEnemyBuilder withFullArmor(ArmorType armorType, int waveMin, int priority, double chance, ArmorPiece.Enchant... enchants) {
    switch (armorType) {
      case LEATHER -> {
        withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.LEATHER_HELMET)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.LEATHER_CHESTPLATE)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.LEATHER_LEGGINGS)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.LEATHER_BOOTS)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
      }
      case CHAINMAIL -> {
        withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.CHAINMAIL_HELMET)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.CHAINMAIL_CHESTPLATE)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.CHAINMAIL_LEGGINGS)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.CHAINMAIL_BOOTS)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
      }
      case GOLD -> {
        withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.GOLDEN_HELMET)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.GOLDEN_CHESTPLATE)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.GOLDEN_LEGGINGS)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.GOLDEN_BOOTS)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
      }
      case IRON -> {
        withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.IRON_HELMET)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.IRON_CHESTPLATE)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.IRON_LEGGINGS)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.IRON_BOOTS)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
      }
      case DIAMOND -> {
        withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.DIAMOND_HELMET)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.DIAMOND_CHESTPLATE)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.DIAMOND_LEGGINGS)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.DIAMOND_BOOTS)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
      }
      case NETHERITE -> {
        withArmorPiece(ArmorPiece.PiecePart.HELMET, new ArmorPiece().setItemStack(new ItemStack(Material.NETHERITE_HELMET)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.CHESTPLATE, new ArmorPiece().setItemStack(new ItemStack(Material.NETHERITE_CHESTPLATE)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.LEGGINGS, new ArmorPiece().setItemStack(new ItemStack(Material.NETHERITE_LEGGINGS)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
        withArmorPiece(ArmorPiece.PiecePart.BOOTS, new ArmorPiece().setItemStack(new ItemStack(Material.NETHERITE_BOOTS)).withEnchants(enchants).setWaveMinimum(waveMin).setPriority(priority).setChance(chance));
      }
    }
    return this;
  }

  public VDEnemyBuilder withArmorPiece(ArmorPiece.PiecePart part, ArmorPiece piece) {
    vdEnemy.getWaveArmorPieces().computeIfAbsent(part, v -> new ArrayList<>()).add(piece);
    return this;
  }

  public VDEnemyBuilder withWeaponPiece(VDEnemy.WeaponPiece piece) {
    vdEnemy.getWeaponParts().add(piece);
    return this;
  }

  /**
   * Head of the entity will be one of playing players instead of armor part
   */
  public VDEnemyBuilder setImpostorHead() {
    vdEnemy.setImpostorHead(true);
    return this;
  }

  public VDEnemyBuilder setBaby() {
    vdEnemy.setBaby(true);
    return this;
  }

  public VDEnemyBuilder canSpawn(Function<Arena, Boolean> canSpawn) {
    vdEnemy.setCanSpawn(canSpawn);
    return this;
  }

  public VDEnemyBuilder withScalingHealth(Function<Integer, Double> scalingHealth) {
    vdEnemy.setHealthFunction(scalingHealth);
    return this;
  }

  public VDEnemyBuilder onSpawn(Consumer<Creature> onSpawn) {
    vdEnemy.setOnSpawn(onSpawn);
    return this;
  }

  public VDEnemyBuilder onDeath(VDEnemy.OnDeathFunction onDeath) {
    vdEnemy.setOnDeath(onDeath);
    return this;
  }

  public VDEnemyBuilder onDamageByEntity(VDEnemy.OnDamageByFunction onDamageByEntity) {
    vdEnemy.setOnDamageBy(onDamageByEntity);
    return this;
  }

  public VDEnemyBuilder onDamagingEntity(VDEnemy.OnDamagingEntityFunction onDamaging) {
    vdEnemy.setOnDamaging(onDamaging);
    return this;
  }

  public VDEnemyBuilder onAbilityTick(VDEnemy.OnAbilityTickFunction onAbilityTick) {
    vdEnemy.setOnAbilityTick(onAbilityTick);
    return this;
  }

  public VDEnemy build() {
    return vdEnemy;
  }

  public enum ArmorType {
    LEATHER, CHAINMAIL, GOLD, IRON, DIAMOND, NETHERITE
  }

}
