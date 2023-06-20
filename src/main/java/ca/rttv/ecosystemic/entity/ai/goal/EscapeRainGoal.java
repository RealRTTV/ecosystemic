package ca.rttv.ecosystemic.entity.ai.goal;

import ca.rttv.ecosystemic.duck.DryDesireDuck;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Optional;

public class EscapeRainGoal extends Goal {
    protected final PathAwareEntity mob;
    private final DryDesireDuck duck;
    private double targetX;
    private double targetY;
    private double targetZ;
    private final World world;

    public EscapeRainGoal(PathAwareEntity mob, DryDesireDuck duck) {
        this.mob = mob;
        this.duck = duck;
        world = mob.getWorld();
        setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.JUMP));
    }

    @Override
    public boolean canStart() {
        return world.isRaining() && world.getBiome(mob.getBlockPos()).value().getPrecipitationAt(mob.getBlockPos()) != Biome.Precipitation.NONE && skyVisible(mob.getBlockPos()) && targetShelterPos();
    }

    private boolean skyVisible(BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        while (y <= world.getTopY()) {
            BlockPos current = new BlockPos(x, y, z);
            BlockState state = world.getBlockState(current);
            if (!state.isSideSolidFullSquare(world, current, Direction.UP) || !state.isSideSolidFullSquare(world, current, Direction.DOWN)) {
                return false;
            }
            y += 1;
        }
        return true;
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
        return !mob.getNavigation().isIdle() && world.isRaining() && world.getBiome(mob.getBlockPos()).value().getPrecipitationAt(mob.getBlockPos()) != Biome.Precipitation.NONE;
    }

    @Override
    public void start() {
        mob.getNavigation().startMovingTo(targetX, targetY, targetZ, 1.0f);
    }

    protected Optional<Vec3d> locateShelterPos() {
        // limited to the first 25, to keep things not painfully slow
        return duck.ecosystemic$pen().stream().filter(this::skyVisible).map(Vec3d::ofBottomCenter).limit(25).min(Comparator.comparingDouble(pos -> mob.getPos().squaredDistanceTo(pos))); // jesus thats slow
    }
}
