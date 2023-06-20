package ca.rttv.ecosystemic.mixin.net.minecraft.entity.passive;

import ca.rttv.ecosystemic.Nibble;
import ca.rttv.ecosystemic.duck.*;
import ca.rttv.ecosystemic.entity.ai.goal.*;
import ca.rttv.ecosystemic.mixin.net.minecraft.entity.mob.MobEntityMixin;
import ca.rttv.ecosystemic.registry.GameRulesRegistry;
import net.minecraft.block.SideShapeType;
import net.minecraft.entity.EntityStatuses;
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
import net.minecraft.world.LightType;
import net.minecraft.world.World;
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
import java.util.Set;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends MobEntityMixin {
    @Unique
    private final Set<BlockPos> pen = new HashSet<>(64);
    @Unique
    @ClientOnly
    private int penSize;
    @Unique
    private double lightLove;
    @Unique
    private int nibbleTimer;
    @Unique
    private EcosystemicEatGrassGoal eatGrassGoal;
    @Unique
    private DrinkWaterGoal drinkWaterGoal;
    @Unique
    private Nibble nibble;
    @Unique
    // server thread only
    private int ticksExisting;
    @Unique
    private double grassLove;
    @Unique
    private double waterLove;

    protected AnimalEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public native void lovePlayer(@Nullable PlayerEntity player);

    // if the entity already exists, it'll read nbt before any interactions occur
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(EntityType<? extends MobEntity> entityType, World world, CallbackInfo ci) {
        if (world.isClient) {
            if (this instanceof PenDesireDuck duck) {
                duck.ecosystemic$penSize(12);
            }
        } else {
            if (this instanceof LightDesireDuck) {
                lightLove = Math.min(15, world.getLightLevel(LightType.SKY, getBlockPos()) + 5) / 15.0d;
            }
            if (this instanceof EatingDesireDuck) {
                grassLove = 0.0d;
            }
            if (this instanceof WaterDesireDuck) {
                waterLove = 0.0d;
            }
            ticksExisting = 0;
        }
    }

    @Inject(method = "handleStatus", at = @At("HEAD"), cancellable = true)
    private void handleStatus(byte status, CallbackInfo ci) {
        if (status == EntityStatuses.ADD_SPLASH_PARTICLES) {
            for(int i = 0; i < 5; i++) { // stolen from VillagerEntity.class
                // should always spawn because it impacts gameplay
                getWorld().addParticle(ParticleTypes.WATER_SPLASH, true, this.getParticleX(1.0), this.getRandomBodyY() + 1.0, this.getParticleZ(1.0), random.nextGaussian() * 0.02, random.nextGaussian() * 0.02, random.nextGaussian() * 0.02);
            }
            ci.cancel();
        }
    }

    @SoftOverride
    protected void ecosystemic$initGoalsTail(CallbackInfo ci) {
        if (this instanceof DryDesireDuck duck) {
            goalSelector.add(-1, new EscapeRainGoal((PathAwareEntity) (Object) this, duck));
            goalSelector.add(-1, new AvoidRainGoal((PathAwareEntity) (Object) this));
        }
        if (this instanceof LightDesireDuck duck) {
            goalSelector.add(4, new LookAtSkyGoal((PathAwareEntity) (Object) this, duck));
        }
        if (this instanceof EatingDesireDuck duck) {
            goalSelector.getGoals().removeIf(goal -> goal.getGoal() instanceof EatGrassGoal);
            goalSelector.add(5, eatGrassGoal = new EcosystemicEatGrassGoal((PathAwareEntity) (Object) this, duck));
        }
        if (this instanceof WaterDesireDuck duck) {
            goalSelector.add(5, drinkWaterGoal = new DrinkWaterGoal((AnimalEntity) (Object) this, duck));
        }
    }

    @Inject(method = "mobTick", at = @At("TAIL"))
    private void mobTick(CallbackInfo ci) {
        if (this instanceof WaterDesireDuck && this instanceof EatingDesireDuck) {
            nibbleTimer = (nibble == Nibble.WATER ? drinkWaterGoal.timer() : eatGrassGoal.timer()) * 2;
        } else if (this instanceof WaterDesireDuck) {
            nibbleTimer = drinkWaterGoal.timer() * 2;
        } else if (this instanceof EatingDesireDuck) {
            nibbleTimer = eatGrassGoal.timer() * 2;
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    protected void ecosystemic$writeCustomDataToNbtTail(NbtCompound nbt, CallbackInfo ci) {
        if (this instanceof LightDesireDuck) {
            nbt.putDouble("LightLove", lightLove);
        }
        if (this instanceof EatingDesireDuck) {
            nbt.putDouble("GrassLove", grassLove);
        }
        if (this instanceof WaterDesireDuck) {
            nbt.putDouble("WaterLove", waterLove);
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    protected void ecosystemic$readCustomDataFromNbtTail(NbtCompound nbt, CallbackInfo ci) {
        if (this instanceof LightDesireDuck) {
            lightLove = nbt.getDouble("LightLove");
        }
        if (this instanceof EatingDesireDuck) {
            grassLove = nbt.getDouble("GrassLove");
        }
        if (this instanceof WaterDesireDuck) {
            waterLove = nbt.getDouble("WaterLove");
        }
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    protected void ecosystemic$tickMovementTail(CallbackInfo ci) {
        if (getWorld().isClient) {
            if (this instanceof PenDesireDuck duck && duck.ecosystemic$penSize() < 12 && getRandom().nextInt(10) == 0) {
                getWorld().addParticle(
                        ParticleTypes.SNEEZE,
                        getX() - (double) (getWidth() + 1.0f) * 0.5 * (double) MathHelper.sin(headYaw * MathHelper.RADIANS_PER_DEGREE),
                        getEyeY() - 0.1f,
                        getZ() + (double) (getWidth() + 1.0f) * 0.5 * (double) MathHelper.cos(headYaw * MathHelper.RADIANS_PER_DEGREE),
                        0.0d,
                        0.0d,
                        0.0d
                );
            }
            if (this instanceof ConsumingDesireDuck && nibbleTimer > 0) { // ceil div probably means that this should check for odd numbers
                nibbleTimer -= 1;
            }
        } else {
            if (this instanceof LightDesireDuck) {
                double effect = Math.min(15, getWorld().getLightLevel(LightType.SKY, getBlockPos()) + 5) / (15.0d * 20_000.0d);
                lightLove = (lightLove + effect) / 1.00005;
            }
        }
    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    protected void ecosystemic$tickMovementHead(CallbackInfo ci) {
        if (this instanceof PenDesireDuck duck) {
            if (ticksExisting++ % getWorld().getGameRules().getInt(GameRulesRegistry.ECOSYSTEMIC_VISITABLE_SPACES_CALCULATE_INTERVAL) == 0 && getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.getProfiler().push("Animal Pen Calculation");
                ecosystemic$calculatePen();
                ((ServerWorldDuck) serverWorld).ecosystemic$sendPenSizePacket(this, duck);
                serverWorld.getProfiler().pop();
            }
        }
    }

    @Inject(method = "isBreedingItem", at = @At("HEAD"), cancellable = true)
    private void isBreedingItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof PenDesireDuck) {
            if (ecosystemic$penSize() < 12) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "lovePlayer", at = @At("HEAD"), cancellable = true)
    private void lovePlayer(PlayerEntity player, CallbackInfo ci) {
        if (this instanceof LightDesireDuck) {
            if (!(getWorld() instanceof ServerWorld serverWorld)) {
                return;
            }

            if (random.nextDouble() + 0.1d <= lightLove && random.nextInt(5) == 0) {
                serverWorld.spawnParticles(ParticleTypes.SMOKE, getPos().x, getPos().y, getPos().z, 7, 0.5d, 0.5d, 0.5d, 0.02d);
                ci.cancel();
            }
        }
    }

    @Inject(method = "isInLove", at = @At("HEAD"), cancellable = true)
    private void isInLove(CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof PenDesireDuck duck && duck.ecosystemic$penSize() < 12) {
            cir.setReturnValue(false);
        }
    }

    public void ecosystemic$calculatePen() {
        pen.clear();
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
        LinkedList<BlockPos> stack = new LinkedList<>();
        stack.add(getLandingPosition());
        while (pen.size() < 64 && !stack.isEmpty()) {
            BlockPos pos = stack.removeFirst();
            pen.add(pos);
            for (BlockPos offset : offsets) {
                if (!pen.contains(pos.add(offset))
                        && getWorld().getBlockState(pos.add(offset)).getCollisionShape(getWorld(), pos.add(offset)).isEmpty()
                        && getWorld().getBlockState(pos.add(offset).down()).isSideSolid(getWorld(), pos.add(offset).down(), Direction.UP, SideShapeType.FULL)
                        && !stack.contains(pos.add(offset))) {
                    stack.addLast(pos.add(offset));
                }
            }
        }
    }

    @SoftOverride
    protected boolean ecosystemic$shouldDropLoot() {
        return this instanceof PenDesireDuck && ecosystemic$penSize() >= 12;
    }

    @SoftOverride
    protected void ecosystemic$onEatingGrassTail(CallbackInfo ci) {
        lovePlayer(getWorld().getClosestPlayer(this, 32));
    }

    @SuppressWarnings("unused")
    public void ecosystemic$penSize(int size) {
        penSize = size;
    }

    @SuppressWarnings("unused")
    public int ecosystemic$penSize() {
        return getWorld().isClient ? penSize : pen.size();
    }

    @SuppressWarnings("unused")
    public double ecosystemic$lightLove() {
        return lightLove;
    }

    @SuppressWarnings("unused")
    public float ecosystemic$headAngle(float tickDelta) {
        if (this.nibbleTimer > 4 && this.nibbleTimer <= 36) {
            return (float) (Math.PI / 5) + 0.22f * MathHelper.sin(((float) (this.nibbleTimer - 4) - tickDelta) * 0.9f);
        } else if (this.nibbleTimer > 0) {
            return (float) (Math.PI / 5);
        } else {
            return this.getPitch() * MathHelper.RADIANS_PER_DEGREE;
        }
    }

    @SuppressWarnings("unused")
    public float ecosystemic$neckAngle(float tickDelta) {
        if (this.nibbleTimer <= 0) {
            return 0.0f;
        } else if (this.nibbleTimer <= 36 && 4 <= this.nibbleTimer) {
            return 1.0f;
        } else if (this.nibbleTimer < 4) {
            return ((float) this.nibbleTimer - tickDelta) / 4.0f;
        } else {
            return -((float) (this.nibbleTimer - 40) - tickDelta) / 4.0f;
        }
    }

    @SuppressWarnings("unused")
    public void ecosystemic$timer(int timer, Nibble type) {
        nibbleTimer = timer;
        nibble = type;
    }

    @SuppressWarnings("unused")
    public void ecosystemic$onDrinkWater() {
    }

    @SuppressWarnings("unused")
    public Set<BlockPos> ecosystemic$pen() {
        return pen;
    }

    @SuppressWarnings("unused")
    public void ecosystemic$addSleepingTicks(int count) {
    }

    @SuppressWarnings("unused")
    public boolean ecosystemic$addPitch() {
        return false;
    }

    @SuppressWarnings("unused")
    public void ecosystemic$onConsume(boolean success, Nibble type) {
        double effect = success ? 0.05d : 0.0d;
        switch (type) {
            case WATER -> grassLove = (grassLove + effect) / 1.05d;
            case GRASS -> waterLove = (waterLove + effect) / 1.05d;
            default -> throw new RuntimeException("Nibble type cannot be null");
        }
    }
}
