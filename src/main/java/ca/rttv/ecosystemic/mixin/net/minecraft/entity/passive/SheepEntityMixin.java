package ca.rttv.ecosystemic.mixin.net.minecraft.entity.passive;

import ca.rttv.ecosystemic.duck.*;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.SheepEntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SheepEntity.class)
public abstract class SheepEntityMixin extends AnimalEntity implements DryDesireDuck, PenDesireDuck, WaterDesireDuck, EatingDesireDuck, LightDesireDuck {
    @Shadow
    public native boolean isSheared();

    @Unique
    private int regrowTicks;

    protected SheepEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("HEAD"))
    private void ecosystemic$initGoalsSuperCall(CallbackInfo ci) {
        super.initGoals();
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void tickMovement(CallbackInfo ci) {
        if (regrowTicks++ >= 24000 && !isSheared()) {
            setSheared(true);
            regrowTicks = 0;
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        regrowTicks = nbt.getInt("RegrowTicks");
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt("RegrowTicks", regrowTicks);
    }

    @Override
    public float ecosystemic$basePivotY() {
        return 6.0f;
    }

    @Override
    public List<ModelPart> ecosystemic$headParts(EntityModel<?> model) {
        return List.of(((SheepEntityModel<?>) model).head);
    }

    @Override
    public float ecosystemic$neckMultiplier() {
        return isBaby() ? 5.0f : 9.0f;
    }

    @ModifyArg(method = "onEatingGrass", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/SheepEntity;growUp(I)V"), index = 0)
    private int onEatingGrass(int value) { // ill do it myself
        return 0;
    }

    @ModifyArg(method = "onEatingGrass", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/SheepEntity;setSheared(Z)V"), index = 0)
    private boolean setSheared(boolean sheared) {
        return isSheared();
    }

    @Override
    public void ecosystemic$onDrinkWater() {
        ecosystemic$addSleepingTicks(1800);
    }

//    @ModifyExpressionValue(method = "sheared", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/random/RandomGenerator;nextInt(I)I"))
//    private int sheared(int value) {
//        return MathHelper.ceilDiv(Math.min(12, SupplierUtil.drinkableWaterBlocks(this, this).getAsInt()), 4);
//    }

    @Override
    public void ecosystemic$addSleepingTicks(int count) {
        regrowTicks = Math.min(24000, regrowTicks + count);
    }

    /**
     * @author rsv
     * @reason if anything modifies this code, there will be a conflict, guaranteed, a crash is the safest bet, and someone can submit a bug report and I can work on integration with the other developer
     */
    @Overwrite
    public float getNeckAngle(float delta) {
        return ecosystemic$neckAngle(delta);
    }

    /**
     * @author rsv
     * @reason if anything modifies this code, there will be a conflict, guaranteed, a crash is the safest bet, and someone can submit a bug report and I can work on integration with the other developer
     */
    @Overwrite
    public float getHeadAngle(float delta) {
        return ecosystemic$headAngle(delta);
    }
}
