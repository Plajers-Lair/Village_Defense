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

package plugily.projects.villagedefense.utils.avatars;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class AvatarCreator {

  public List<String> getHeadColors(UUID uuid) {
    List<String> colors = new ArrayList<>();
    try {
      BufferedImage img = ImageIO.read(new URL("https://crafatar.com/avatars/" + uuid + "?size=8&overlay"));

      for (int i = 0; i < 8; ++i) {
        for (int j = 0; j < 8; ++j) {
          colors.add(String.format("#%06X", 16777215 & img.getRGB(i, j)));
        }
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    return colors;
  }

  public Component[] getHead(UUID uuid, AvatarSize avatarSize) {
    Component[][] textComponents = new Component[8][8];
    List<String> headColors = this.getHeadColors(uuid);

    for (int i = 0; i < 64; ++i) {
      net.kyori.adventure.text.Component component;
      int var7 = i / 8;
      int var8 = i % 8;
      char unicodeChar = (char) (avatarSize.getUnicodeChar() + var8 + 1);
      if (i % 8 == 7) {
        component = Component.text(unicodeChar + Character.toString(avatarSize.getSpaces()[0]));
      } else {
        component = Component.text(unicodeChar + Character.toString(avatarSize.getSpaces()[1]));
      }

      component = component.color(TextColor.fromHexString(headColors.get(i)));
      component = component.font(Key.key("villagedefense"));
      textComponents[var7][var8] = component;
    }
    return Arrays.stream(textComponents).flatMap(Arrays::stream).toArray(Component[]::new);
  }

}
