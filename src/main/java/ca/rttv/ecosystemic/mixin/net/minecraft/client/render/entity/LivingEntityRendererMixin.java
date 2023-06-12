package ca.rttv.ecosystemic.mixin.net.minecraft.client.render.entity;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
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
    private static final float[] RED_VISITED_SPACES   = {0x3f, 0x25, 0x42, 0x4f, 0x5d, 0x6e, 0x73, 0x83, 0xa8, 0xbf, 0xd0, 0xbe};
    @Unique
    private static final float[] GREEN_VISITED_SPACES = {0x00, 0x1f, 0x2c, 0x40, 0x5e, 0x66, 0x6f, 0x87, 0xa2, 0xba, 0xd1, 0xd4};
    @Unique
    private static final float[] BLUE_VISITED_SPACES  = {0x2a, 0x06, 0x09, 0x0e, 0x18, 0x32, 0x3c, 0x52, 0x79, 0x91, 0xb2, 0x92};

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "net/minecraft/client/render/entity/model/EntityModel.render(Lnet/minecraft/client/util/math/MatrixStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void ecosystemic$changeRenderTint(Args args, T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        Vector3f colors = livingEntity instanceof AnimalEntityDuck duck && duck.ecosystemic$visitedSpaceCount() < 12
                ? new Vector3f(RED_VISITED_SPACES[duck.ecosystemic$visitedSpaceCount()] / 255.0f,
                            GREEN_VISITED_SPACES[duck.ecosystemic$visitedSpaceCount()] / 255.0f,
                            BLUE_VISITED_SPACES[duck.ecosystemic$visitedSpaceCount()] / 255.0f)
                : new Vector3f(1.0f, 1.0f, 1.0f);
        args.set(4, colors.x);
        args.set(5, colors.y);
        args.set(6, colors.z);
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;animateModel(Lnet/minecraft/entity/Entity;FFF)V", shift = At.Shift.AFTER))
    private void animateModel(T livingEntity, float f, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!(livingEntity instanceof AnimalEntityDuck duck)) {
            return;
        }

        duck.ecosystemic$headParts(model).forEach(part -> part.pivotY = duck.ecosystemic$basePivotY() + duck.ecosystemic$neckAngle(tickDelta) * duck.ecosystemic$neckMultiplier());
    }

    @Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/EntityModel;setAngles(Lnet/minecraft/entity/Entity;FFFFF)V", shift = At.Shift.AFTER))
    private void setAngles(T livingEntity, float f, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (!(livingEntity instanceof AnimalEntityDuck duck)) {
            return;
        }

        duck.ecosystemic$headParts(model).forEach(part -> part.pitch = (duck.ecosystemic$addPitch() ? part.pitch : 0) + duck.ecosystemic$headAngle(tickDelta));
    }
}
