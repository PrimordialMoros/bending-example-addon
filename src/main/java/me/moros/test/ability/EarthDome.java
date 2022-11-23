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

package me.moros.test.ability;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

import me.moros.bending.config.Configurable;
import me.moros.bending.model.ability.AbilityDescription;
import me.moros.bending.model.ability.AbilityInstance;
import me.moros.bending.model.ability.Activation;
import me.moros.bending.model.ability.MultiUpdatable;
import me.moros.bending.model.ability.common.Pillar;
import me.moros.bending.model.attribute.Attribute;
import me.moros.bending.model.attribute.Modifiable;
import me.moros.bending.model.predicate.Policies;
import me.moros.bending.model.predicate.RemovalPolicy;
import me.moros.bending.model.user.User;
import me.moros.bending.temporal.TempBlock;
import me.moros.bending.util.WorldUtil;
import me.moros.bending.util.material.EarthMaterials;
import me.moros.math.FastMath;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public class EarthDome extends AbilityInstance {
  private User user;
  private Config userConfig;
  private RemovalPolicy removalPolicy;

  private Predicate<Block> predicate;
  private final MultiUpdatable<Pillar> pillars = MultiUpdatable.empty();

  public EarthDome(AbilityDescription desc) {
    super(desc);
  }

  @Override
  public boolean activate(User user, Activation method) {
    this.user = user;
    loadConfig();

    if (!user.isOnGround()) {
      return false;
    }
    predicate = b -> EarthMaterials.isEarthNotLava(user, b);
    if (calculatePillars()) {
      removalPolicy = Policies.builder().add(Policies.NOT_SNEAKING).build();
      user.addCooldown(description(), userConfig.cooldown);
      return true;
    }
    return false;
  }

  @Override
  public void loadConfig() {
    userConfig = user.game().configProcessor().calculate(this, new Config());
  }

  @Override
  public UpdateResult update() {
    if (removalPolicy.test(user, description())) {
      return UpdateResult.REMOVE;
    }
    return pillars.update();
  }

  private boolean calculatePillars() {
    int offset = FastMath.ceil(userConfig.radius + 1);
    int size = offset * 2 + 1;
    boolean[][] checked = new boolean[size][size];
    Block center = user.locBlock().getRelative(BlockFace.DOWN);
    for (int i = 0; i < 2; i++) {
      double radius = userConfig.radius + i;
      int height = userConfig.height - i;
      for (Block block : WorldUtil.createBlockRing(center, radius)) {
        int dx = offset + center.getX() - block.getX();
        int dz = offset + center.getZ() - block.getZ();
        if (checked[dx][dz]) {
          continue;
        }
        Optional<Pillar> pillar = WorldUtil.findTopBlock(block, height, predicate)
          .flatMap(b -> createPillar(b, height));
        if (pillar.isPresent()) {
          checked[dx][dz] = true;
          pillars.add(pillar.get());
        }
      }
    }
    return !pillars.isEmpty();
  }

  private Optional<EarthPillar> createPillar(Block block, int height) {
    if (!predicate.test(block) || !TempBlock.isBendable(block)) {
      return Optional.empty();
    }
    return Pillar.builder(user, block, EarthPillar::new).interval(75).predicate(predicate)
      .build(height + 2, height);
  }

  @Override
  public @MonotonicNonNull User user() {
    return user;
  }

  private static final class EarthPillar extends Pillar {
    private EarthPillar(Builder<EarthPillar> builder) {
      super(builder);
    }

    @Override
    public void playSound(Block block) {
      if (ThreadLocalRandom.current().nextInt(8) == 0) {
        super.playSound(block);
      }
    }
  }

  private static class Config extends Configurable {
    @Modifiable(Attribute.COOLDOWN)
    private final long cooldown = 9000;
    @Modifiable(Attribute.RADIUS)
    private final double radius = 3;
    @Modifiable(Attribute.HEIGHT)
    private final int height = 3;

    @Override
    public Iterable<String> path() {
      return List.of("abilities", "earth", "earthdome");
    }

    @Override
    public boolean external() {
      return true;
    }
  }
}
