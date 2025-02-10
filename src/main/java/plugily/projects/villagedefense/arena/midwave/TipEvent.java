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

package plugily.projects.villagedefense.arena.midwave;

import plugily.projects.minigamesbox.classic.handlers.language.MessageBuilder;
import plugily.projects.villagedefense.arena.Arena;
import plugily.projects.villagedefense.utils.LimitedQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

public class TipEvent implements MidWaveEvent {

  private final List<Tip> registeredTips = new ArrayList<>();
  private final Queue<Tip> tipCache = new LimitedQueue<>(6);

  public TipEvent() {
    registeredTips.add(new Tip(new MessageBuilder("Remember to upgrade your armor and weapons in &e&lVillager Shop").build(), 1));
    registeredTips.add(new Tip(new MessageBuilder("You, your pets and villagers %value% passively every wave").value("&a&lHEAL").build(), 1));
    registeredTips.add(new Tip(new MessageBuilder("You can upgrade your %value% with Shift + Right Click").value("&b&lPETS").build(), 5));
    registeredTips.add(new Tip(new MessageBuilder("Enemies do occasionally drop %value% such as Money Pouch, Iron Delivery and more").value("&b&lPOWERUPS").build(), 5));
    registeredTips.add(new Tip(new MessageBuilder("Baby zombies are pretty annoying").build(), 5));
    registeredTips.add(new Tip(new MessageBuilder("You can use %value% to ride villagers and move them").value("&a&lSADDLE").build(), 8));
    registeredTips.add(new Tip(new MessageBuilder("%value% pop when you damage them, avoid their explosion").value("&e&lPlayer Busters").build(), 10));
    registeredTips.add(new Tip(new MessageBuilder("Enemies start scaling health starting from &e&lWave 15").build(), 12));
    registeredTips.add(new Tip(new MessageBuilder("Do not let enemies crowd yourself or you'll die pretty fast").build(), 15));
    registeredTips.add(new Tip(new MessageBuilder("%value% spawn TNT upon hitting you, avoid their TNT explosion").value("&c&lKamikaze Zombies").build(), 25));
    registeredTips.add(new Tip(new MessageBuilder("%value% units cannot be stunned nor slowed by any abilities").value("&7&lUnstunnable (✶)").build(), 36));
    registeredTips.add(new Tip(new MessageBuilder("%value% units will not die on one-shot but receive 15% max health damage instead").value("&4&lUnpoppable (☄)").build(), 36));
    registeredTips.add(new Tip(new MessageBuilder("%value% ignore your presence completely and target only villagers").value("&a&lVillager Slayers").build(), 45));
  }


  @Override
  public boolean canTrigger(Arena arena) {
    return arena.getWave() % 4 == 0;
  }

  @Override
  public void initiate(Arena arena) {
    while (true) {
      Tip tip = registeredTips.get(ThreadLocalRandom.current().nextInt(registeredTips.size()));
      if (arena.getWave() < tip.minWave()) {
        continue;
      }
      if (!tipCache.contains(tip)) {
        new MessageBuilder(tip.message()).prefix().arena(arena).sendArena();
        break;
      }
    }
  }

  @Override
  public void cleanup(Arena arena) {
  }

  private record Tip(String message, int minWave) {
  }

}
