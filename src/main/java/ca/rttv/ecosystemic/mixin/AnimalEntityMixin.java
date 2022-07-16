package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import ca.rttv.ecosystemic.network.packet.s2c.play.VisitedSpaceCountS2CPacket;
import ca.rttv.ecosystemic.util.PacketUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.SoftOverride;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

@Mixin(AnimalEntity.class)
abstract class AnimalEntityMixin extends MobEntityMixin {
    @Unique
    private final Map<BlockPos, Long> visitedSpaces = new LinkedHashMap<>();
    @Unique
    private long ticksMoved;
    @Unique
    @Environment(EnvType.CLIENT)
    private int visitedSpaceCount;
    @Unique
    private int ticksWithSkylight;
    @Unique
    private int failedLoveAttempts; // mojmap has weird names, so I will too

    protected AnimalEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(EntityType<? extends MobEntity> entityType, World world, CallbackInfo ci) {
        if (!(this instanceof AnimalEntityDuck duck)) {
            return;
        }

        if (world.isClient) {
            duck.ecosystemic$visitedSpaceCount(12);
        } else {
            BlockPos.iterate(getBlockPos().add(-1, 0, -1), getBlockPos().add(1, 0, 2)).forEach(mutablePos -> visitedSpaces.put(mutablePos.toImmutable(), -20000L));
            ticksWithSkylight = 6000;
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    protected void ecosystemic$writeCustomDataToNbtTail(NbtCompound nbt, CallbackInfo ci) {
        if (!(this instanceof AnimalEntityDuck)) {
            return;
        }

        nbt.putLong("TicksMoved", ticksMoved);
        nbt.putIntArray("VisitedSpaces", visitedSpaces.entrySet()
                .stream()
                .flatMapToInt(entry -> IntStream.of(
                        (int) ((entry.getKey().asLong() & 0xFFFFFFFF00000000L) >>> 32L),
                        (int) (entry.getKey().asLong() & 0x00000000FFFFFFFFL),
                        (int) (ticksMoved - entry.getValue()))
                )
                .toArray());
        nbt.putInt("TicksWithSunlight", ticksWithSkylight);
        nbt.putInt("FailedLoveAttempts", failedLoveAttempts);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    protected void ecosystemic$readCustomDataFromNbtTail(NbtCompound nbt, CallbackInfo ci) {
        if (!(this instanceof AnimalEntityDuck)) {
            return;
        }

        ticksMoved = nbt.getLong("TicksMoved");
        ticksWithSkylight = nbt.getInt("TicksWithSkylight");
        failedLoveAttempts = nbt.getInt("FailedLoveAttempts");
        visitedSpaces.clear();
        // normally an nbt list cannot hold multiple types so split the BlockPos into 2 ints with 1 remaining for the time visited since 3 ints is better than 2 longs (96 bits vs 128 bits)
        int[] list = nbt.getIntArray("VisitedSpaces");
        if (list.length % 3 != 0) {
            LOGGER.error("VisitedSpaces length must be divisible by 3 to work properly, ignoring remaining " + list.length % 2 + " ints");
        }
        for (int i = 0; i < list.length / 3 * 3; ) {
            visitedSpaces.put(BlockPos.fromLong((long) list[i++] << 32 | list[i++]), list[i++] + ticksMoved);
        }
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    protected void ecosystemic$tickMovementTail(CallbackInfo ci) {
        if (!(this instanceof AnimalEntityDuck duck)) {
            return;
        }

        if (!world.isClient) {
            int prevVisitedSpacesCount = visitedSpaces.size();
            visitedSpaces.put(getBlockPos(), ticksMoved); // this is why it is a map
            visitedSpaces.values().removeIf(entry -> entry <= ticksMoved - 24000);
            if (visitedSpaces.size() != prevVisitedSpacesCount) {
                PacketUtils.sendPacketToPlayers((ServerWorld) world, getBlockPos(), new Identifier("ecosystemic", "visitedspacecounts2cpacket")).accept(new VisitedSpaceCountS2CPacket(visitedSpaces.size(), uuid).toBuf());
            }
            if (world.isDay()) {
                ticksWithSkylight += world.isSkyVisible(getBlockPos()) ? 1 : -1;
            }
        } else {
            if (duck.ecosystemic$visitedSpaceCount() < 12 && getRandom().nextInt(10) == 0) {
                world.addParticle(
                        ParticleTypes.SNEEZE,
                        getX() - (double) (getWidth() + 1.0f) * 0.5 * (double) MathHelper.sin(bodyYaw * MathHelper.RADIANS_PER_DEGREE),
                        getEyeY() - 0.1f,
                        getZ() + (double) (getWidth() + 1.0f) * 0.5 * (double) MathHelper.cos(bodyYaw * MathHelper.RADIANS_PER_DEGREE),
                        getVelocity().x,
                        0.0d,
                        getVelocity().z
                );
            }
        }
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    protected void ecosystemic$tickMovementHead(CallbackInfo ci) {
        if (getVelocity().x == 0 && getVelocity().z == 0 && (getVelocity().y == 0 || getVelocity().y < -0.0784 && getVelocity().y > -0.0785) && getNavigation().getCurrentPath() != null) {
            return; // has to be moving and have a current path for pathfinding
        }

        if (!(this instanceof AnimalEntityDuck)) {
            return;
        }

        ticksMoved++; // it works ;)
    }

    @Inject(method = "isBreedingItem", at = @At("HEAD"), cancellable = true)
    private void isBreedingItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!(this instanceof AnimalEntityDuck duck)) {
            return;
        }

        if (duck.ecosystemic$visitedSpaceCount() >= 12) {
            return;
        }

        cir.setReturnValue(false);
    }

    @Inject(method = "lovePlayer", at = @At("HEAD"), cancellable = true)
    private void lovePlayer(PlayerEntity player, CallbackInfo ci) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        if (ticksWithSkylight < 3000 && failedLoveAttempts++ < 5) {
            serverWorld.spawnParticles(ParticleTypes.SMOKE, getPos().x, getPos().y, getPos().z, 7, 0.5d, 0.5d, 0.5d, 0.02d);
            ci.cancel();
            return;
        }
        failedLoveAttempts = 0;
    }

    @Inject(method = "isInLove", at = @At("HEAD"), cancellable = true)
    private void isInLove(CallbackInfoReturnable<Boolean> cir) {
        if (!(this instanceof AnimalEntityDuck duck) || duck.ecosystemic$visitedSpaceCount() >= 12) {
            return;
        }

        cir.setReturnValue(false);
    }

    @SoftOverride
    protected boolean ecosystemic$shouldDropLoot() {
        return this instanceof AnimalEntityDuck && visitedSpaces.size() >= 12;
    }

    // this is the best feature of java
    @SuppressWarnings("unused")
    public void ecosystemic$visitedSpaceCount(int count) {
        visitedSpaceCount = count;
    }

    // this is the best feature of java
    @SuppressWarnings("unused")
    public int ecosystemic$visitedSpaceCount() {
        return visitedSpaceCount;
    }

    // this is the best feature of java
    @SuppressWarnings("unused")
    public int ecosystemic$ticksWithSkylight() {
        return ticksWithSkylight;
    }
}
