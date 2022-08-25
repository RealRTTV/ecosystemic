package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import ca.rttv.ecosystemic.entity.ai.goal.AvoidRainGoal;
import ca.rttv.ecosystemic.entity.ai.goal.DrinkWaterGoal;
import ca.rttv.ecosystemic.entity.ai.goal.EscapeRainGoal;
import ca.rttv.ecosystemic.entity.ai.goal.LookAtSkyGoal;
import ca.rttv.ecosystemic.network.packet.s2c.play.VisitedSpaceCountS2CPacket;
import ca.rttv.ecosystemic.util.PacketUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.SoftOverride;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.IntSupplier;
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
    @Unique
    private int nibbleTimer;
    @Unique
    private EatGrassGoal eatGrassGoal;
    @Unique
    private DrinkWaterGoal drinkWaterGoal;
    @Unique
    private boolean nibblingWater;

    protected AnimalEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public native void lovePlayer(@Nullable PlayerEntity player);

    // if the entity already exists, it'll read nbt before any interactions occur
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(EntityType<? extends MobEntity> entityType, World world, CallbackInfo ci) {
        if (!(this instanceof AnimalEntityDuck duck)) {
            return;
        }

        if (world.isClient) {
            duck.ecosystemic$visitedSpaceCount(12);
        } else {
            int[] i = {0};
            BlockPos.iterate(getBlockPos().add(-1, 0, -1), getBlockPos().add(1, 0, 2)).forEach(mutablePos -> {
                visitedSpaces.put(mutablePos.toImmutable(), -20000L + i[0]);
                i[0] += 1000;
            });
            ticksWithSkylight = 6000;
        }
    }

    @SoftOverride
    protected void ecosystemic$initGoalsTail(CallbackInfo ci) {
        if (!(this instanceof AnimalEntityDuck duck)) {
            return;
        }

        goalSelector.add(-1, new EscapeRainGoal((PathAwareEntity) (Object) this));
        goalSelector.add(-1, new AvoidRainGoal((PathAwareEntity) (Object) this));
        goalSelector.add(4, new LookAtSkyGoal((PathAwareEntity) (Object) this, duck));
        //noinspection ConstantConditions -- checked
        if (goalSelector.getGoals().stream().noneMatch(goal -> goal.getGoal() instanceof EatGrassGoal grass && (eatGrassGoal = grass) != null)) {
            goalSelector.add(5, eatGrassGoal = new EatGrassGoal((PathAwareEntity) (Object) this));
        }
        goalSelector.add(5, drinkWaterGoal = new DrinkWaterGoal((AnimalEntity) (Object) this, duck));
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

    @Inject(method = "mobTick", at = @At("TAIL"))
    private void mobTick(CallbackInfo ci) {
        if (!(this instanceof AnimalEntityDuck)) {
            return;
        }

        nibbleTimer = nibblingWater ? drinkWaterGoal.timer() : eatGrassGoal.getTimer();
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
                PacketUtils.sendPacketToPlayers((ServerWorld) world, getBlockPos(), new Identifier("ecosystemic", "visitedspacecounts2cpacket"), new VisitedSpaceCountS2CPacket(visitedSpaces.size(), uuid));
            }
            if (world.isDay()) {
                ticksWithSkylight += world.isSkyVisible(getBlockPos()) ? 1 : -1;
            }
        } else {
            if (duck.ecosystemic$visitedSpaceCount() < 12 && getRandom().nextInt(10) == 0) {
                world.addParticle(
                        ParticleTypes.SNEEZE,
                        getX() - (double) (getWidth() + 1.0f) * 0.5 * (double) MathHelper.sin(headYaw * MathHelper.RADIANS_PER_DEGREE),
                        getEyeY() - 0.1f,
                        getZ() + (double) (getWidth() + 1.0f) * 0.5 * (double) MathHelper.cos(headYaw * MathHelper.RADIANS_PER_DEGREE),
                        getVelocity().x,
                        0.0d,
                        getVelocity().z
                );
            }
            nibbleTimer = Math.max(0, nibbleTimer - 1);
            if (nibbleTimer == 0) {
                nibblingWater = false;
            }
        }
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    protected void ecosystemic$tickMovementHead(CallbackInfo ci) {
        if (getVelocity().x == 0
         && getVelocity().z == 0
         && (getVelocity().y == 0 || getVelocity().y < -0.0784 && getVelocity().y > -0.0785)
         && getNavigation().getCurrentPath() != null)
        {
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

    @Inject(method = "handleStatus", at = @At("HEAD"), cancellable = true)
    private void handleStatus(byte status, CallbackInfo ci) {
        if (status == 10) {
            nibbleTimer = 40;
            nibblingWater = false;
            ci.cancel();
        }
    }

    @SoftOverride
    protected boolean ecosystemic$shouldDropLoot() {
        return this instanceof AnimalEntityDuck && visitedSpaces.size() >= 12;
    }

    @SoftOverride
    protected void ecosystemic$onEatingGrassTail(CallbackInfo ci) {
        lovePlayer(world.getClosestPlayer(this, 32));
    }

    @SuppressWarnings("unused")
    public void ecosystemic$visitedSpaceCount(int count) {
        visitedSpaceCount = count;
    }

    @SuppressWarnings("unused")
    public int ecosystemic$visitedSpaceCount() {
        return visitedSpaceCount;
    }

    @SuppressWarnings("unused")
    public int ecosystemic$ticksWithSkylight() {
        return ticksWithSkylight;
    }

    @SuppressWarnings("unused")
    public float ecosystemic$headAngle(float tickDelta) {
        if (this.nibbleTimer > 4 && this.nibbleTimer <= 36) {
            return (float) (Math.PI / 5) + 0.21991149F * MathHelper.sin(((float) (this.nibbleTimer - 4) - tickDelta) * 0.896875f);
        } else if (this.nibbleTimer > 0) {
            return (float) (Math.PI / 5);
        } else {
            return this.getPitch() * (float) (Math.PI / 180.0f);
        }
    }

    @SuppressWarnings("unused")
    public float ecosystemic$neckAngle(float tickDelta) {
        if (this.nibbleTimer <= 0) {
            return 0.0f;
        } else if (this.nibbleTimer >= 4 && this.nibbleTimer <= 36) {
            return 1.0f;
        } else if (this.nibbleTimer < 4) {
            return ((float) this.nibbleTimer - tickDelta) / 4.0f;
        } else {
            return -((float) (this.nibbleTimer - 40) - tickDelta) / 4.0f;
        }
    }

    @SuppressWarnings("unused")
    public void ecosystemic$waterTimer(int timer) {
        nibbleTimer = timer;
        nibblingWater = true;
    }

    @SuppressWarnings("unused")
    public void ecosystemic$onDrinkWater(IntSupplier drinkableWaterBlocks) { }

    @SuppressWarnings("unused")
    public Set<BlockPos> ecosystemic$visitedSpaces() {
        return visitedSpaces.keySet();
    }
}
