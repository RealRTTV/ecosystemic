package ca.rttv.ecosystemic.mixin.net.minecraft.server.world;

import ca.rttv.ecosystemic.duck.LightDesireDuck;
import ca.rttv.ecosystemic.duck.PenDesireDuck;
import ca.rttv.ecosystemic.duck.ServerWorldDuck;
import ca.rttv.ecosystemic.network.packet.s2c.play.PenSizeS2CPacket;
import ca.rttv.ecosystemic.util.PacketUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.world.ServerWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements ServerWorldDuck {
    @Shadow
    public native <T extends Entity> List<? extends T> getEntitiesByType(TypeFilter<Entity, T> typeFilter, Predicate<? super T> predicate);

    @Shadow
    @Final
    List<ServerPlayerEntity> players;

    @Shadow
    @Final
    private ServerWorldProperties worldProperties;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
    private void sleeping(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        int bonusTicks = (int) (24000L - worldProperties.getTimeOfDay() % 24000L);
        getEntitiesByType(TypeFilter.instanceOf(AnimalEntity.class), entity -> entity instanceof LightDesireDuck && (((World) (Object) this).isRaining() && ((World) (Object) this).getBiome(entity.getLandingPosition()).value().getPrecipitationAt(entity.getLandingPosition()) != Biome.Precipitation.NONE) ^ ((World) (Object) this).isSkyVisible(entity.getLandingPosition())).forEach(entity -> ((LightDesireDuck) entity).ecosystemic$addSleepingTicks(bonusTicks));
    }

    @Override
    public void ecosystemic$sendPenSizePacket(Entity entity, PenDesireDuck duck) {
        if (entity == duck) {
            PacketUtils.sendPacketToPlayers((ServerWorld) (Object) this, entity.getBlockPos(), new Identifier("ecosystemic", "pens2cpacket"), new PenSizeS2CPacket(entity.getUuid(), duck.ecosystemic$penSize()));
        }
    }
}
