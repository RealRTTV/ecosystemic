package ca.rttv.ecosystemic.entity.ai.goal;

import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Random;

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
        Vec3d vec3d = this.locateShelterPos();
        if (vec3d == null) {
            return false;
        } else {
            this.targetX = vec3d.x;
            this.targetY = vec3d.y;
            this.targetZ = vec3d.z;
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

    @Nullable
    protected Vec3d locateShelterPos() {
        RandomGenerator random = this.mob.getRandom();
        BlockPos blockPos = this.mob.getBlockPos();

        for(int i = 0; i < 10; ++i) {
            BlockPos blockPos2 = blockPos.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
            if (!this.world.isSkyVisible(blockPos2) && this.mob.getPathfindingFavor(blockPos2) < 0.0F) {
                return Vec3d.ofBottomCenter(blockPos2);
            }
        }

        return null;
    }
}
