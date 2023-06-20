package ca.rttv.ecosystemic.entity.ai.goal;

import ca.rttv.ecosystemic.Nibble;
import ca.rttv.ecosystemic.duck.WaterDesireDuck;
import ca.rttv.ecosystemic.network.packet.s2c.play.ResetConsumingTimerS2CPacket;
import ca.rttv.ecosystemic.util.PacketUtils;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;

public class DrinkWaterGoal extends Goal {
    private final PathAwareEntity mob;
    private final WaterDesireDuck duck;
    private final World world;
    @Nullable
    private BlockPos waterPos;
    @Nullable
    private BlockPos targetPos;
    private int timer;
    private boolean canNavigate;

    public DrinkWaterGoal(PathAwareEntity entity, WaterDesireDuck duck) {
        mob = entity;
        this.duck = duck;
        world = entity.getWorld();
        targetPos = null;
        timer = 0;
        canNavigate = true;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK, Control.JUMP));
    }

    public int timer() {
        return timer;
    }

    @Override
    public boolean canStart() {
        if (mob.getRandom().nextInt(mob.isBaby() ? 100 : 100) > 0) {
            return false;
        }

        Optional<Pair<BlockPos, BlockPos>> pair = duck.ecosystemic$pen().stream().map(pos -> {
            if (world.getFluidState(pos.down().north()).isOf(Fluids.WATER) && !world.getBlockState(pos.north()).isSolid()) {
                return new Pair<>(pos, pos.north().down());
            } else if (world.getFluidState(pos.down().east()).isOf(Fluids.WATER) && !world.getBlockState(pos.east()).isSolid()) {
                return new Pair<>(pos, pos.east().down());
            } else if (world.getFluidState(pos.down().south()).isOf(Fluids.WATER) && !world.getBlockState(pos.south()).isSolid()) {
                return new Pair<>(pos, pos.south().down());
            } else if (world.getFluidState(pos.down().west()).isOf(Fluids.WATER) && !world.getBlockState(pos.west()).isSolid()) {
                return new Pair<>(pos, pos.west().down());
            } else {
                return null;
            }
        }).filter(Objects::nonNull).min(Comparator.comparingDouble(x -> Vec3d.ofBottomCenter(x.getLeft()).squaredDistanceTo(mob.getPos())));
        if (pair.isPresent()) {
            targetPos = pair.get().getLeft();
            waterPos = pair.get().getRight();
        } else if (world.getFluidState(mob.getBlockPos()).isOf(Fluids.WATER)) {
            targetPos = mob.getBlockPos();
            waterPos = mob.getBlockPos();
        }
        return true;
    }

    @Override
    public void tick() {
        if (mob.getNavigation().getCurrentPath() != null && mob.getNavigation().getCurrentPath().isFinished()) {
            if (timer == getTickCount(40) && world instanceof ServerWorld serverWorld) {
                PacketUtils.sendPacketToPlayers(serverWorld, mob.getBlockPos(), new Identifier("ecosystemic", "resetconsumingtimers2cpacket"), new ResetConsumingTimerS2CPacket(mob.getUuid(), 40, Nibble.WATER));
                duck.ecosystemic$timer(40, Nibble.WATER);
            }
            if (targetPos != null && waterPos != null && world.getFluidState(waterPos).isOf(Fluids.WATER)) {
                mob.getLookControl().lookAt(waterPos.getX() + 0.5d, waterPos.getY() + 1.0d, waterPos.getZ() + 0.5d, 1.0f, 1.0f);
                if (timer % getTickCount(4) == 0 && timer < getTickCount(32) && world instanceof ServerWorld serverWorld) {
                    serverWorld.playSoundFromEntity(null, mob, SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.NEUTRAL, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
                }
                if (timer == getTickCount(4)) {
                    if (mob.getBlockPos().equals(targetPos)) {
                        mob.emitGameEvent(GameEvent.DRINK);
                        if (mob instanceof PassiveEntity passiveEntity && passiveEntity.isBaby()) {
                            passiveEntity.growUp(60);
                        }
                        mob.emitGameEvent(GameEvent.EAT);
                        duck.ecosystemic$onDrinkWater();
                        mob.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, waterPos.ofCenter());
                    }
                }
            }
            if (waterPos != null && world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, world.getBlockState(waterPos)), waterPos.getX() + 0.5d, waterPos.getY() + 1.0d, waterPos.getZ() + 0.5d, 1, 0.25d, 0.25d, 0.25d, 0.05d);
            }
            timer = Math.max(timer - 1, 0);
        } else {
            if (!canNavigate) {
                if (timer % getTickCount(20) < 6 && timer % getTickCount(40) == 0 && world instanceof ServerWorld serverWorld) {
                    world.sendEntityStatus(mob, EntityStatuses.ADD_SPLASH_PARTICLES);
                    serverWorld.playSoundFromEntity(null, mob, SoundEvents.ENTITY_PLAYER_SPLASH, SoundCategory.NEUTRAL, 0.5f, world.random.nextFloat() * 0.1f + 0.9f);
                }
                timer = Math.max(timer - 1, 0);
            }
        }
    }

    @Override
    public void start() {
        timer = getTickCount(40);
        if (targetPos != null && waterPos != null) {
            canNavigate = mob.getNavigation().startMovingTo(targetPos.getX() + 0.5d, targetPos.getY(), targetPos.getZ() + 0.5d, 1.0d);
        } else {
            canNavigate = false;
        }
    }

    @Override
    public void stop() {
        if (timer > 0 && world instanceof ServerWorld serverWorld) {
            PacketUtils.sendPacketToPlayers(serverWorld, mob.getBlockPos(), new Identifier("ecosystemic", "resetconsumingtimers2cpacket"), new ResetConsumingTimerS2CPacket(mob.getUuid(), 0, Nibble.WATER));
            duck.ecosystemic$timer(0, Nibble.WATER);
        }
        timer = 0;
        duck.ecosystemic$onConsume(canNavigate && targetPos != null, Nibble.WATER);
        targetPos = null;
        waterPos = null;
        mob.getNavigation().stop();
        canNavigate = true;
    }

    @Override
    public boolean shouldContinue() {
        // this is so smart because once the block changes the mob won't be interested anymore, automatic!
        return targetPos == null || waterPos == null ? timer > 0 : canNavigate && world.getFluidState(waterPos).isOf(Fluids.WATER) && (!mob.getNavigation().isIdle() || mob.getBlockPos().equals(targetPos) && timer > 0);
    }
}
