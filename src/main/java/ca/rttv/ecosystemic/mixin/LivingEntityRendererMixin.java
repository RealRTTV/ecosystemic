package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LivingEntityRenderer.class)
final class LivingEntityRendererMixin<T extends LivingEntity> {
    @Unique
    private static final float[] RED_VISITED_SPACES   = {0x3f, 0x25, 0x42, 0x4f, 0x5d, 0x6e, 0x73, 0x83, 0xa8, 0xbf, 0xd0, 0xbe};
    @Unique
    private static final float[] GREEN_VISITED_SPACES = {0x00, 0x1f, 0x2c, 0x40, 0x5e, 0x66, 0x6f, 0x87, 0xa2, 0xba, 0xd1, 0xd4};
    @Unique
    private static final float[] BLUE_VISITED_SPACES  = {0x2a, 0x06, 0x09, 0x0e, 0x18, 0x32, 0x3c, 0x52, 0x79, 0x91, 0xb2, 0x92};

    @ModifyArgs(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "net/minecraft/client/render/entity/model/EntityModel.render(Lnet/minecraft/client/util/math/MatrixStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void ecosystemic$changeRenderTint(Args args, T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        Vec3f colors = livingEntity instanceof AnimalEntityDuck duck && duck.ecosystemic$visitedSpaceCount() < 12
                ? new Vec3f(RED_VISITED_SPACES[duck.ecosystemic$visitedSpaceCount()] / 255.0f,
                            GREEN_VISITED_SPACES[duck.ecosystemic$visitedSpaceCount()] / 255.0f,
                            BLUE_VISITED_SPACES[duck.ecosystemic$visitedSpaceCount()] / 255.0f)
                : new Vec3f(1.0f, 1.0f, 1.0f);
        args.set(4, colors.getX());
        args.set(5, colors.getY());
        args.set(6, colors.getZ());
    }
}
