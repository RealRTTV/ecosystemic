package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import ca.rttv.ecosystemic.network.packet.s2c.play.VisitedSpaceCountS2CPacket;
import ca.rttv.ecosystemic.util.PacketUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
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
import java.util.stream.IntStream;

@Mixin(AnimalEntity.class)
abstract class AnimalEntityMixin extends MobEntityMixin {
    @Unique
    private final LinkedHashMap<BlockPos, Long> visitedSpaces = new LinkedHashMap<>();
    @Unique
    private long ticksMoved;
    @Unique
    @Environment(EnvType.CLIENT)
    private int visitedSpaceCount;

    protected AnimalEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(EntityType<? extends MobEntity> entityType, World world, CallbackInfo ci) {
        if (!(this instanceof AnimalEntityDuck duck)) {
            return;
        }

        if (!world.isClient) {
            BlockPos.iterate(getBlockPos().add(-1, 0, -1), getBlockPos().add(1, 0, 2)).forEach(mutablePos -> visitedSpaces.put(mutablePos.toImmutable(), -23000L));
        } else {
            duck.ecosystemic$visitedSpaceCount(12);
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
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    protected void ecosystemic$readCustomDataFromNbtTail(NbtCompound nbt, CallbackInfo ci) {
        if (!(this instanceof AnimalEntityDuck)) {
            return;
        }
        ticksMoved = nbt.getLong("TicksMoved");
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
        if (!(this instanceof AnimalEntityDuck)) {
            return;
        }

        if (!world.isClient) {
            int prevVisitedSpacesCount = visitedSpaces.size();
            visitedSpaces.put(getBlockPos(), ticksMoved); // this is why it is a map
            visitedSpaces.values().removeIf(entry -> entry <= ticksMoved - 24000);
            if (visitedSpaces.size() != prevVisitedSpacesCount) {
                PacketUtil.sendPacketToPlayers((ServerWorld) world, getBlockPos(), new Identifier("ecosystemic", "visitedspacecounts2cpacket")).accept(new VisitedSpaceCountS2CPacket(visitedSpaces.size(), uuid).toBuf());
            }
        } else {
            AnimalEntityDuck duck = (AnimalEntityDuck) this;
            if (duck.ecosystemic$visitedSpaceCount() < 12 && getRandom().nextInt(20) == 0) {
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
        if (!(this instanceof AnimalEntityDuck) || !(getVelocity().x != 0 || getVelocity().z != 0 || !(getVelocity().y == 0 || getVelocity().y < -0.0784 && getVelocity().y > -0.0785)) && getNavigation().getCurrentPath() != null) {
            return;
        }

        ticksMoved++; // it works ;)
    }

    @Inject(method = "isBreedingItem", at = @At("HEAD"), cancellable = true)
    private void isBreedingItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!(this instanceof AnimalEntityDuck duck) || duck.ecosystemic$visitedSpaceCount() >= 12) {
            return;
        }

        cir.setReturnValue(false);
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
    public void ecosystemic$visitedSpaceCount(int count) {
        visitedSpaceCount = count;
    }

    // this is the best feature of java
    public int ecosystemic$visitedSpaceCount() {
        return visitedSpaceCount;
    }
}
