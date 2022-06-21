package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.entity.ai.goal.AvoidRainGoal;
import ca.rttv.ecosystemic.entity.ai.goal.EscapeRainGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(ChickenEntity.class)
public abstract class ChickenEntityMixin extends PassiveEntity {

    @Unique
    private final LinkedHashMap<BlockPos, Integer> visitedSpaces = new LinkedHashMap<>();

    protected ChickenEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "initGoals", at = @At("TAIL"))
    private void initGoals(CallbackInfo ci) {
        this.goalSelector.add(-1, new EscapeRainGoal(this)); // this works somehow
        this.goalSelector.add(-1, new AvoidRainGoal(this)); // this works somehow
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void tickMovement(CallbackInfo ci) {
        if (!world.isClient) {
            visitedSpaces.put(getBlockPos(), ((ServerWorld) world).getServer().getTicks());
            // if these lists work well, and I understand then, I can cut runtime by stopping once I hit one that is too recent to be discarded
            visitedSpaces.entrySet().removeIf(entry -> entry.getValue() <= ((ServerWorld) world).getServer().getTicks());
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        visitedSpaces.clear();
        nbt.getList("VisitedSpaces", 10).stream().map(element -> (NbtCompound) element).forEach(compound -> visitedSpaces.put(BlockPos.fromLong(compound.getLong("Pos")), compound.getInt("Tick")));
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtList list = new NbtList();
        visitedSpaces.forEach((pos, tick) -> {
            NbtCompound compound = new NbtCompound();
            compound.putLong("Pos", pos.asLong());
            compound.putInt("Tick", tick);
            list.add(compound);
        });
        nbt.put("VisitedSpaces", list);
    }
}
