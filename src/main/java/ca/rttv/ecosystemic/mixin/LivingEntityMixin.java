package ca.rttv.ecosystemic.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract RandomGenerator getRandom();

    @Shadow
    public float bodyYaw;

    public LivingEntityMixin(EntityType<?> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "shouldDropLoot", at = @At("HEAD"), cancellable = true)
    private void shouldDropLoot(CallbackInfoReturnable<Boolean> cir) {
        if (!ecosystemic$shouldDropLoot()) {
            cir.setReturnValue(false);
        }
    }

    protected boolean ecosystemic$shouldDropLoot() {
        return true;
    }
}
