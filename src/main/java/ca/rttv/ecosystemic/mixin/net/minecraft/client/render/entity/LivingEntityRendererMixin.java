package ca.rttv.ecosystemic.mixin.net.minecraft.client.render.entity;

import ca.rttv.ecosystemic.duck.ConsumingDesireDuck;
import ca.rttv.ecosystemic.duck.PenDesireDuck;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Shadow
    @SuppressWarnings("ShadowModifiers")
    private M model;
    @Unique
    private static final float[] RED_VISITED_SPACES   = {0xC2, 0xB5, 0x9E, 0x8, 0x8A, 0x7B, 0x40, 0x58, 0x56, 0x53, 0x47, 0x41};
    @Unique
    private static final float[] GREEN_VISITED_SPACES = {0xC2, 0xB6, 0xA7, 0xA0, 0x92, 0x8A, 0x3C, 0x6E, 0x67, 0x5C, 0x51, 0x42};
    @Unique
    private static final float[] BLUE_VISITED_SPACES  = {0x95, 0x88, 0x77, 0x6C, 0x61, 0x5A, 0x16, 0x52, 0x41, 0x3A, 0x33, 0x26};

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "net/minecraft/client/render/entity/model/EntityModel.render(Lnet/minecraft/client/util/math/MatrixStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void ecosystemic$changeRenderTint(Args args, T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        Vector3f colors = livingEntity instanceof PenDesireDuck duck && duck.ecosystemic$penSize() < 12
                ? new Vector3f(RED_VISITED_SPACES[duck.ecosystemic$penSize()] / 255.0f,
                            GREEN_VISITED_SPACES[duck.ecosystemic$penSize()] / 255.0f,
                            BLUE_VISITED_SPACES[duck.ecosystemic$penSize()] / 255.0f)
                : new Vector3f(1.0f, 1.0f, 1.0f);
        args.set(4, colors.x);
        args.set(5, colors.y);
        args.set(6, colors.z);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;animateModel(Lnet/minecraft/entity/Entity;FFF)V", shift = At.Shift.AFTER))
    private void animateModel(T livingEntity, float f, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!(livingEntity instanceof ConsumingDesireDuck duck)) {
            return;
        }

        duck.ecosystemic$headParts(model).forEach(part -> part.pivotY = duck.ecosystemic$basePivotY() + duck.ecosystemic$neckAngle(tickDelta) * duck.ecosystemic$neckMultiplier());
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/entity/Entity;FFFFF)V", shift = At.Shift.AFTER))
    private void setAngles(T livingEntity, float f, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!(livingEntity instanceof ConsumingDesireDuck duck)) {
            return;
        }

        duck.ecosystemic$headParts(model).forEach(part -> part.pitch = (duck.ecosystemic$addPitch() ? part.pitch : 0) + duck.ecosystemic$headAngle(tickDelta));
    }
}
