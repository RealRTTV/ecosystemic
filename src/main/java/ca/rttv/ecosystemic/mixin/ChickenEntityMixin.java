package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.entity.AnimalEntityHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.SoftOverride;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;

@Mixin(ChickenEntity.class)
abstract class ChickenEntityMixin extends AnimalEntityMixin {

    @Unique
    private final LinkedHashMap<BlockPos, Long> visitedSpaces = new LinkedHashMap<>();
    @Unique
    private long ticksMoved;

    protected ChickenEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @SoftOverride
    protected void initGoalsTail(CallbackInfo ci) {
        AnimalEntityHelper.addShelterGoals(goalSelector, (PathAwareEntity) (Object) this);
    }

    @SoftOverride
    protected void tickMovementTail(CallbackInfo ci) {
        AnimalEntityHelper.cacheVisitedSpace(world, visitedSpaces, getBlockPos(), ++ticksMoved);
    }

    @SoftOverride
    protected void readCustomDataFromNbtTail(NbtCompound nbt, CallbackInfo ci) {
        AnimalEntityHelper.readVisitedSpaces(nbt, visitedSpaces, (ticksMoved = nbt.getLong("TicksMoved")));
    }

    @SoftOverride
    protected void writeCustomDataToNbtTail(NbtCompound nbt, CallbackInfo ci) {
        AnimalEntityHelper.writeVisitedSpaces(nbt, visitedSpaces, world, ticksMoved);
    }
}
