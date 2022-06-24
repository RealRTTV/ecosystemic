package ca.rttv.ecosystemic.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnimalEntity.class)
abstract class AnimalEntityMixin extends MobEntityMixin {
    protected AnimalEntityMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    protected void writeCustomDataToNbtTail(NbtCompound nbt, CallbackInfo ci) {

    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    protected void readCustomDataFromNbtTail(NbtCompound nbt, CallbackInfo ci) {

    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    protected void tickMovementTail(CallbackInfo ci) {

    }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    protected void tickMovementHead(CallbackInfo ci) {

    }
}
