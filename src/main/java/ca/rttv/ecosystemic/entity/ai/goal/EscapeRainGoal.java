package ca.rttv.ecosystemic.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;

import java.util.EnumSet;
import java.util.Optional;

public class EscapeRainGoal extends Goal {
    protected final PathAwareEntity mob;
    private double targetX;
    private double targetY;
    private double targetZ;
    private final World world;

    public EscapeRainGoal(PathAwareEntity mob) {
        this.mob = mob;
        world = mob.world;
        setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return world.isRaining() && world.isSkyVisible(mob.getBlockPos()) && targetShelterPos();
    }

    protected boolean targetShelterPos() {
        Optional<Vec3d> vec3d = locateShelterPos();
        if (vec3d.isEmpty()) {
            return false;
        } else {
            targetX = vec3d.get().x;
            targetY = vec3d.get().y;
            targetZ = vec3d.get().z;
            return true;
        }
    }

    @Override
    public boolean shouldContinue() {
        return !mob.getNavigation().isIdle();
    }

    @Override
    public void start() {
        mob.getNavigation().startMovingTo(targetX, targetY, targetZ, 1.0f);
    }

    protected Optional<Vec3d> locateShelterPos() {
        RandomGenerator random = mob.getRandom();

        for(int i = 0; i < 20; ++i) {
            BlockPos pos = mob.getBlockPos().add(random.nextInt(32) - 16, random.nextInt(6) - 3, random.nextInt(32) - 16);
            if (!world.isSkyVisible(pos) && mob.getPathfindingFavor(pos) < 0.0F) {
                return Optional.of(Vec3d.ofBottomCenter(pos));
            }
        }

        return Optional.empty();
    }
}
