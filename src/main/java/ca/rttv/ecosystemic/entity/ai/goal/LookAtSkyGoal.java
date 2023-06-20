package ca.rttv.ecosystemic.entity.ai.goal;

import ca.rttv.ecosystemic.duck.LightDesireDuck;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;

public class LookAtSkyGoal extends Goal {
    private final PathAwareEntity mob;
    private final LightDesireDuck duck;

    public LookAtSkyGoal(PathAwareEntity mob, LightDesireDuck duck) {
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
        return mob.getRandom().nextDouble() + 0.1d <= duck.ecosystemic$lightLove();
    }
}
