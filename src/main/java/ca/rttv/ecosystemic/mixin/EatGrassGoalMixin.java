package ca.rttv.ecosystemic.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EatGrassGoal.class)
abstract class EatGrassGoalMixin {
    @Shadow
    @Final
    private MobEntity mob;

    @Shadow
    @Final
    private World world;

    @Redirect(method = "canStart", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z", ordinal = 0)) // todo, change with mixin
    private boolean isOf(BlockState self, Block block) {
        return Block.isShapeFullCube(self.getCollisionShape(world, mob.getBlockPos().down()));
    }

    @Inject(method = "tick", at = @At(value = "JUMP", opcode = Opcodes.IFEQ, ordinal = 2), cancellable = true)
    private void tick(CallbackInfo ci) {
        if (world.getBlockState(mob.getBlockPos().down()).isOf(Blocks.DIRT) // is dirt
         || mob instanceof PathAwareEntity pathAwareEntity && pathAwareEntity.getPathfindingFavor(mob.getBlockPos().down()) > 0.0f && pathAwareEntity.getPathfindingFavor(mob.getBlockPos().down()) != world.m_jwglzkvy(mob.getBlockPos().down())
         // ^^ do I like this block
         || world.getFluidState(mob.getBlockPos().down()).isOf(Fluids.WATER) // is there water below me
         || world.getFluidState(mob.getBlockPos()).isOf(Fluids.WATER) // am I in water?
         || world.isAir(mob.getBlockPos())) // am I floating (ie, i jumped)
        {
            // why grass here?
            // why not grass here?
            // what is grass, where is here?
            // why am I here?
            // who am I to judge why the grass is there
            return;
        }

        if (world instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(ParticleTypes.ANGRY_VILLAGER, mob.getPos().x, mob.getPos().y, mob.getPos().z, 25, 0.5d, 0.5d, 0.5d, 0.02d);
        }

        ci.cancel();
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z")) // todo, change with mixin extras to be a `condition || our lengthy stuff`
    private boolean tick(BlockState instance, Block block) {
        return mob instanceof PathAwareEntity pathAwareEntity && pathAwareEntity.getPathfindingFavor(mob.getBlockPos()) > 0.0f && pathAwareEntity.getPathfindingFavor(mob.getBlockPos()) != world.m_jwglzkvy(mob.getBlockPos());
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getRawIdFromState(Lnet/minecraft/block/BlockState;)I"))
    private BlockState tick(@Nullable BlockState state) {
        return world.getBlockState(mob.getBlockPos().down());
    }
}
