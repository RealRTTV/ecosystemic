package ca.rttv.ecosystemic.entity.ai.goal;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;

public class LookAtSkyGoal extends Goal {
    private final PathAwareEntity mob;
    private final AnimalEntityDuck duck;

    public LookAtSkyGoal(PathAwareEntity mob, AnimalEntityDuck duck) {
        this.mob = mob;
        this.duck = duck;
        setControls(EnumSet.of(Control.LOOK));
    }

    @Override
    public void tick() {
        mob.setPitch(mob.getPitch() + MathHelper.clamp(MathHelper.wrapDegrees(85.0f - mob.getPitch()), -5.0f, 5.0f));
    }

    @Override
    public boolean canStart() {
        return !mob.world.isSkyVisible(mob.getBlockPos()) && !mob.world.isRaining() && duck.ecosystemic$ticksWithSkylight() < 3000;
    }
}
