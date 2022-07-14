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
        this.world = mob.world;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return this.world.isRaining() && this.world.isSkyVisible(this.mob.getBlockPos()) && this.targetShelterPos();
    }

    protected boolean targetShelterPos() {
        Optional<Vec3d> vec3d = this.locateShelterPos();
        if (vec3d.isEmpty()) {
            return false;
        } else {
            this.targetX = vec3d.get().x;
            this.targetY = vec3d.get().y;
            this.targetZ = vec3d.get().z;
            return true;
        }
    }

    @Override
    public boolean shouldContinue() {
        return !this.mob.getNavigation().isIdle();
    }

    @Override
    public void start() {
        this.mob.getNavigation().startMovingTo(this.targetX, this.targetY, this.targetZ, 1.0f);
    }

    protected Optional<Vec3d> locateShelterPos() {
        RandomGenerator random = this.mob.getRandom();
        BlockPos blockPos = this.mob.getBlockPos();

        for(int i = 0; i < 20; ++i) {
            BlockPos blockPos2 = blockPos.add(random.nextInt(32) - 16, random.nextInt(6) - 3, random.nextInt(32) - 16);
            if (!this.world.isSkyVisible(blockPos2) && this.mob.getPathfindingFavor(blockPos2) < 0.0F) {
                return Optional.of(Vec3d.ofBottomCenter(blockPos2));
            }
        }

        return Optional.empty();
    }
}
