package ca.rttv.ecosystemic.entity.ai.goal;

import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.mob.PathAwareEntity;

public class AvoidRainGoal extends Goal {
    private final PathAwareEntity mob;

    public AvoidRainGoal(PathAwareEntity mob) {
        this.mob = mob;
    }

    @Override
    public boolean canStart() {
        return this.mob.world.isRaining() && NavigationConditions.hasMobNavigation(this.mob);
    }

    @Override
    public void start() {
        ((MobNavigation)this.mob.getNavigation()).setAvoidSunlight(true); // that;s convenient
    }

    @Override
    public void stop() {
        if (NavigationConditions.hasMobNavigation(this.mob)) {
            ((MobNavigation)this.mob.getNavigation()).setAvoidSunlight(false);
        }

    }
}
