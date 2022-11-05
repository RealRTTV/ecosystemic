package ca.rttv.ecosystemic.mixin.net.minecraft.entity.passive;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.CowEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.SoftOverride;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.IntSupplier;

@Mixin(CowEntity.class)
public abstract class CowEntityMixin extends PassiveEntityMixin implements AnimalEntityDuck {
    protected CowEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    // if the entity already exists, it'll read nbt before any interactions occur
    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(EntityType<? extends PassiveEntity> entityType, World world, CallbackInfo ci) {
        replenishTicks = 24000;
    }

    @Unique
    private int replenishTicks;

    @SoftOverride
    protected void ecosystemic$tickMovementHead(CallbackInfo ci) {
        super.ecosystemic$tickMovementHead(ci);
        if (replenishTicks < 24000) {
            replenishTicks++;
        }
    }

    @SoftOverride
    protected void ecosystemic$writeCustomDataToNbtTail(NbtCompound nbt, CallbackInfo ci) {
        super.ecosystemic$writeCustomDataToNbtTail(nbt, ci);
        nbt.putInt("ReplenishTicks", replenishTicks);
    }

    @SoftOverride
    protected void ecosystemic$readCustomDataFromNbtTail(NbtCompound nbt, CallbackInfo ci) {
        super.ecosystemic$readCustomDataFromNbtTail(nbt, ci);
        replenishTicks = nbt.getInt("ReplenishTicks");
    }

    // todo, de-sync with server and client
    @ModifyExpressionValue(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/CowEntity;isBaby()Z"))
    private boolean interactMob(boolean original) {
        return original || replenishTicks < 24000;
    }

    @Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;setStackInHand(Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)V"))
    private void interactMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        replenishTicks = 0;
    }

    @Override
    public List<ModelPart> ecosystemic$headParts(EntityModel<?> model) {
        return List.of(((CowEntityModel<?>) model).getHead());
    }

    @Override
    public void ecosystemic$onDrinkWater(IntSupplier drinkableWaterBlocks) {
        replenishTicks += (int) (800.0f * ((float) Math.min(12, drinkableWaterBlocks.getAsInt()) / 4.0f));
    }

    @Override
    public float ecosystemic$basePivotY() {
        return 4.0f;
    }

    @Override
    public float ecosystemic$neckMultiplier() {
        return 9.0f;
    }

    @Override
    public void ecosystemic$addSleepingTicks(int count) {
        replenishTicks = Math.min(24000, replenishTicks + count);
    }
}
