package ca.rttv.ecosystemic.entity.ai.goal;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import ca.rttv.ecosystemic.network.packet.s2c.play.ResetWaterTimerS2CPacket;
import ca.rttv.ecosystemic.util.PacketUtils;
import ca.rttv.ecosystemic.util.SupplierUtil;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

public class DrinkWaterGoal extends Goal {
    private final AnimalEntity mob;
    private final AnimalEntityDuck duck;
    private final World world;
    private Optional<BlockPos> waterPos;
    private int timer;

    public DrinkWaterGoal(AnimalEntity mob, AnimalEntityDuck duck) {
        this.mob = mob;
        this.duck = duck;
        world = mob.world;
        timer = 40;
        waterPos = Optional.empty();
        setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
    }

    @Override
    public boolean canStart() {
        if (mob.getRandom().nextInt(this.mob.isBaby() ? 50 : 500) == 0) {
            return getWaterPos().isPresent();
        }
        return false;
    }

    @Override
    public void start() {
        timer = getTickCount(40);
        waterPos = getWaterPos();
        if (world instanceof ServerWorld serverWorld) {
            PacketUtils.sendPacketToPlayers(serverWorld, mob.getBlockPos(), new Identifier("ecosystemic", "resetwatertimers2cpacket"), new ResetWaterTimerS2CPacket(mob.getUuid()));
            duck.ecosystemic$waterTimer(40);
        }
        mob.getNavigation().stop();
        mob.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, Vec3d.ofBottomCenter(waterPos.orElseThrow(() -> new RuntimeException("Previously confirmed that water was nearby, but now it isn't?!?!"))));
    }

    @Override
    public void stop() {
        timer = 0;
        waterPos = Optional.empty();
    }

    @Override
    public boolean shouldContinue() {
        return timer > 0;
    }

    public int timer() {
        return timer;
    }

    @Override
    public void tick() {
        timer = Math.max(0, timer - 1);
        if (timer == getTickCount(4) && waterPos.isPresent()) {
            IntSupplier drinkableWaterBlocks = SupplierUtil.memoize(() ->
                    (int) duck.ecosystemic$visitedSpaces()
                              .stream()
                              .flatMap(space -> Stream.of(
                                  space.add(1, -1, 0),
                                  space.add(-1, -1, 0),
                                  space.add(0, -1, 1),
                                  space.add(0, -1, -1)
                              ))
                              .distinct()
                              .map(pos -> world.getFluidState(pos).isOf(Fluids.WATER) && world.getFluidState(pos.up()).isEmpty() && world.getBlockState(pos.up()).getCollisionShape(world, pos).isEmpty())
                              .count()
            );
            mob.emitGameEvent(GameEvent.DRINK);
            if (mob.isBaby()) {
                mob.growUp(60);
            }
            duck.ecosystemic$onDrinkWater(drinkableWaterBlocks);
        }
        if (timer / 2 < 7 && timer % 2 == 0 && world instanceof ServerWorld serverWorld) {
            Vec3d vec = mob.getPos().add(mob.getRotationVector());
            serverWorld.spawnParticles(ParticleTypes.WATER_SPLASH,
                    vec.x,
                    vec.y,
                    vec.z,
                    16,
                    0.2d,
                    0.1d,
                    0.2d,
                    0.02d);

            world.playSoundFromEntity(null, mob, SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.NEUTRAL, 0.7f, 0.8f + world.random.nextFloat() * 0.4f);
        }
    }

    private Optional<BlockPos> getWaterPos() {
        BlockPos[] possiblePositions = new BlockPos[4];
        possiblePositions[0] = mob.getBlockPos().add(1, -1, 0); // Positive X
        possiblePositions[1] = mob.getBlockPos().add(-1, -1, 0); // Negative X
        possiblePositions[2] = mob.getBlockPos().add(0, -1, 1); // Positive Z
        possiblePositions[3] = mob.getBlockPos().add(0, -1, -1); // Negative Z
        for (BlockPos possible : possiblePositions) {
            if (world.getFluidState(possible).isOf(Fluids.WATER)) {
                return Optional.of(possible);
            }
        }

        return Optional.empty();
    }
}
