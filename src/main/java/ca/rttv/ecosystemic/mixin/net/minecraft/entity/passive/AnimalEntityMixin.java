package ca.rttv.ecosystemic.mixin.net.minecraft.entity.passive;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import ca.rttv.ecosystemic.entity.ai.goal.AvoidRainGoal;
import ca.rttv.ecosystemic.entity.ai.goal.DrinkWaterGoal;
import ca.rttv.ecosystemic.entity.ai.goal.EscapeRainGoal;
import ca.rttv.ecosystemic.entity.ai.goal.LookAtSkyGoal;
import ca.rttv.ecosystemic.mixin.net.minecraft.entity.mob.MobEntityMixin;
import ca.rttv.ecosystemic.registry.GameRulesRegistry;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.SoftOverride;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends MobEntityMixin {
    @Unique
    private final Set<BlockPos> visitedSpaces = new HashSet<>(64);
    @Unique
    @ClientOnly
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
    @Unique
    private int ticksExisting;

    protected AnimalEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public native void lovePlayer(@Nullable PlayerEntity player);

    @Shadow
    public abstract float getPathfindingFavor(BlockPos pos, WorldView world);

    // if the entity already exists, it'll read nbt before any interactions occur
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(EntityType<? extends MobEntity> entityType, World world, CallbackInfo ci) {
        if (!(this instanceof AnimalEntityDuck duck)) {
            return;
        }

        if (world.isClient) {
            duck.ecosystemic$visitedSpaceCount(12);
        } else {
            ecosystemic$calculateVisitedSpaces();
            ticksWithSkylight = 6000;
            ticksExisting = 0;
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

        // do not cache
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

        ticksWithSkylight = nbt.getInt("TicksWithSkylight");
        failedLoveAttempts = nbt.getInt("FailedLoveAttempts");
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    protected void ecosystemic$tickMovementTail(CallbackInfo ci) {
        if (!(this instanceof AnimalEntityDuck duck)) {
            return;
        }

        if (!getWorld().isClient) {
            if (getWorld().isDay()) {
                ticksWithSkylight += getWorld().isSkyVisible(getBlockPos()) ? 1 : -1;
            }
        } else {
            if (duck.ecosystemic$visitedSpaceCount() < 12 && getRandom().nextInt(10) == 0) {
                getWorld().addParticle(
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
        if (!(this instanceof AnimalEntityDuck)) {
            return;
        }

        ticksExisting++;
        if (!getWorld().isClient && ticksExisting % getWorld().getGameRules().getInt(GameRulesRegistry.ECOSYSTEMIC_VISITABLE_SPACES_CALCULATE_INTERVAL) == 0) { // todo gamerule for value
            ecosystemic$calculateVisitedSpaces();
        }
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
        if (!(getWorld() instanceof ServerWorld serverWorld)) {
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

    public void ecosystemic$calculateVisitedSpaces() {
        visitedSpaces.clear();
        BlockPos[] offsets = new BlockPos[]{
                BlockPos.ORIGIN.offset(Direction.NORTH),
                BlockPos.ORIGIN.offset(Direction.EAST),
                BlockPos.ORIGIN.offset(Direction.SOUTH),
                BlockPos.ORIGIN.offset(Direction.WEST),
                BlockPos.ORIGIN.offset(Direction.NORTH).offset(Direction.UP),
                BlockPos.ORIGIN.offset(Direction.EAST).offset(Direction.UP),
                BlockPos.ORIGIN.offset(Direction.SOUTH).offset(Direction.UP),
                BlockPos.ORIGIN.offset(Direction.WEST).offset(Direction.UP),
                BlockPos.ORIGIN.offset(Direction.NORTH).offset(Direction.DOWN),
                BlockPos.ORIGIN.offset(Direction.EAST).offset(Direction.DOWN),
                BlockPos.ORIGIN.offset(Direction.SOUTH).offset(Direction.DOWN),
                BlockPos.ORIGIN.offset(Direction.WEST).offset(Direction.DOWN)
        };
        List<BlockPos> stack = new LinkedList<>();
        stack.add(getBlockPos());
        while (visitedSpaces.size() < 64 && stack.size() > 0) {
            BlockPos pos = stack.remove(0);
            if (getPathfindingFavor(pos, getWorld()) > getWorld().method_42309(pos) && getWorld().isAir(pos) && getPathfindingFavor(pos, getWorld()) != 0) {
                visitedSpaces.add(pos);
                for (BlockPos offset : offsets) {
                    if (!visitedSpaces.contains(pos.add(offset)) && !stack.contains(pos.add(offset))) {
                        stack.add(pos.add(offset));
                    }
                }
            }
        }
    }

    @SoftOverride
    protected boolean ecosystemic$shouldDropLoot() {
        return this instanceof AnimalEntityDuck && visitedSpaces.size() >= 12;
    }

    @SoftOverride
    protected void ecosystemic$onEatingGrassTail(CallbackInfo ci) {
        lovePlayer(getWorld().getClosestPlayer(this, 32));
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
        return visitedSpaces;
    }

    @SuppressWarnings("unused")
    public void ecosystemic$addSleepingTicks(int count) {}

    @SuppressWarnings("unused")
    public boolean ecosystemic$addPitch() {
        return false;
    }
}
