package ca.rttv.ecosystemic.entity.ai.goal;

import ca.rttv.ecosystemic.Nibble;
import ca.rttv.ecosystemic.duck.EatingDesireDuck;
import ca.rttv.ecosystemic.network.packet.s2c.play.ResetConsumingTimerS2CPacket;
import ca.rttv.ecosystemic.util.PacketUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

public class EcosystemicEatGrassGoal extends Goal {
    private final PathAwareEntity mob;
    private final EatingDesireDuck duck;
    private final World world;
    @Nullable
    private BlockPos targetPos;
    private int timer;
    private boolean canNavigate;


    public EcosystemicEatGrassGoal(PathAwareEntity entity, EatingDesireDuck duck) {
        mob = entity;
        this.duck = duck;
        world = entity.getWorld();
        targetPos = null;
        timer = 0;
        canNavigate = true;
        setControls(EnumSet.of(Control.MOVE, Control.JUMP));
    }

    public int timer() {
        return timer;
    }

    @Override
    public boolean canStart() {
        if (mob.getRandom().nextInt(mob.isBaby() ? 100 : 100) > 0) {
            return false;
        }
        if (mob.getPathfindingFavor(mob.getBlockPos()) > 0.5f) {
            targetPos = mob.getBlockPos();
        } else {
            targetPos = duck.ecosystemic$pen().stream().filter(pos -> mob.getPathfindingFavor(pos) > 0.5f).min(Comparator.comparingDouble(pos -> Vec3d.ofBottomCenter(pos).squaredDistanceTo(mob.getPos()))).orElse(null);
        }
        return true;
    }

    @Override
    public void start() {
        this.timer = this.getTickCount(40);
        if (targetPos != null) {
            canNavigate = this.mob.getNavigation().startMovingTo(targetPos.getX() + 0.5d, targetPos.getY(), targetPos.getZ() + 0.5d, 1.0d);
        } else {
            canNavigate = false;
        }
    }

    @Override
    public void tick() {
        if (mob.getNavigation().getCurrentPath() != null && mob.getNavigation().getCurrentPath().isFinished()) {
            if (timer == getTickCount(40) && world instanceof ServerWorld serverWorld) {
                PacketUtils.sendPacketToPlayers(serverWorld, mob.getBlockPos(), new Identifier("ecosystemic", "resetconsumingtimers2cpacket"), new ResetConsumingTimerS2CPacket(mob.getUuid(), 40, Nibble.GRASS));
                duck.ecosystemic$timer(40, Nibble.GRASS);
            }
            SoundEvent sound = SoundEvents.BLOCK_STONE_BREAK;
            if (targetPos != null && mob.getPathfindingFavor(targetPos) > 0.5f) {
                sound = SoundEvents.ENTITY_GENERIC_EAT;
                mob.getLookControl().lookAt(targetPos.getX() + 0.5d, targetPos.getY() + 1.0d, targetPos.getZ() + 0.5d, 1.0f, 1.0f);
                if (timer == getTickCount(4)) {
                    if (mob.getBlockPos().equals(targetPos)) {
                        world.breakBlock(targetPos.down(), false);
                        world.setBlockState(targetPos.down(), Blocks.DIRT.getDefaultState());
                        if (mob instanceof PassiveEntity passiveEntity && passiveEntity.isBaby()) {
                            passiveEntity.growUp(60);
                        }
                        mob.onEatingGrass();
                    }
                }
            }
            if (timer % getTickCount(4) == 0 && timer < getTickCount(32) && world instanceof ServerWorld serverWorld) {
                serverWorld.playSoundFromEntity(null, mob, sound, SoundCategory.NEUTRAL, 0.5f, world.random.nextFloat() * 0.2f + 1.0f);
            }
            if (world instanceof ServerWorld serverWorld) {
                serverWorld.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, world.getBlockState(mob.getBlockPos().down())), mob.getBlockX() + 0.5d, mob.getBlockY() + 1.0d, mob.getBlockZ() + 0.5d, 1, 0.25d, 0.25d, 0.25d, 0.05d);
            }
            timer = Math.max(timer - 1, 0);
        }
    }

    @Override
    public void stop() {
        if (timer > 0 && world instanceof ServerWorld serverWorld) {
            PacketUtils.sendPacketToPlayers(serverWorld, mob.getBlockPos(), new Identifier("ecosystemic", "resetconsumingtimers2cpacket"), new ResetConsumingTimerS2CPacket(mob.getUuid(), 0, Nibble.GRASS));
            duck.ecosystemic$timer(0, Nibble.GRASS);
        }
        timer = 0;
        duck.ecosystemic$onConsume(canNavigate && targetPos != null, Nibble.GRASS);
        targetPos = null;
        mob.getNavigation().stop();
        canNavigate = true;
    }

    @Override
    public boolean shouldContinue() {
        // this is so smart because once the block changes the mob won't be interested anymore, automatic!
        return targetPos == null ? timer > 0 : canNavigate && mob.getPathfindingFavor(targetPos) > 0.5f && (!mob.getNavigation().isIdle() || (mob.getBlockPos().equals(targetPos) && timer > 0));
    }
}
