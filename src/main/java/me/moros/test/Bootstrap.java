/*
 * Copyright 2022 Moros
 *
 * This file is part of Bending.
 *
 * Bending is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Bending is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bending. If not, see <https://www.gnu.org/licenses/>.
 */

package me.moros.test;

import java.util.ResourceBundle;

import me.moros.bending.locale.Message;
import me.moros.bending.model.ability.description.AbilityDescription;
import me.moros.bending.registry.Registries;
import me.moros.test.ability.EarthDome;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.util.UTF8ResourceBundleControl;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.Nullable;

import static me.moros.bending.model.Element.EARTH;
import static me.moros.bending.model.ability.Activation.SNEAK;

public class Bootstrap extends JavaPlugin {
  @Override
  public void onLoad() {
    // First let's register our AbilityDescription
    AbilityDescription earthDome = AbilityDescription.builder("EarthDome", EarthDome::new)
      .element(EARTH).activation(SNEAK).build();
    Registries.ABILITIES.register(earthDome);

    // Now we attempt to register a translation for our ability
    TranslationRegistry registry = findBendingTranslator(); // There is no proper api for this yet, so we use this unorthodox method
    if (registry != null) {
      ResourceBundle bundle = ResourceBundle.getBundle("earthdome", Message.DEFAULT_LOCALE, UTF8ResourceBundleControl.get());
      registry.registerAll(Message.DEFAULT_LOCALE, bundle, false);
    }
  }

  private @Nullable TranslationRegistry findBendingTranslator() {
    Key key = Key.key("bending", "translations");
    for (var translator : GlobalTranslator.translator().sources()) {
      if (key.equals(translator.name()) && translator instanceof TranslationRegistry registry) {
        return registry;
      }
    }
    return null;
  }
}
