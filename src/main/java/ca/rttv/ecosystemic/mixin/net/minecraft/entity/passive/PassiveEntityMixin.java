package ca.rttv.ecosystemic.mixin.net.minecraft.entity.passive;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PassiveEntity.class)
public abstract class PassiveEntityMixin extends PathAwareEntity {
    @Shadow public abstract boolean isBaby();

    protected PassiveEntityMixin(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    protected void ecosystemic$readCustomDataFromNbtTail(NbtCompound nbt, CallbackInfo ci) { }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    protected void ecosystemic$writeCustomDataToNbtTail(NbtCompound nbt, CallbackInfo ci) { }

    @Inject(method = "tickMovement", at = @At("HEAD"))
    protected void ecosystemic$tickMovementHead(CallbackInfo ci) { }
}
