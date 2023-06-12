package ca.rttv.ecosystemic.mixin.net.minecraft.server.world;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import ca.rttv.ecosystemic.network.packet.s2c.play.VisitedSpaceCountS2CPacket;
import ca.rttv.ecosystemic.registry.GameRulesRegistry;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.world.EntityList;
import net.minecraft.world.ServerWorldProperties;
import net.minecraft.world.World;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
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
public abstract class ServerWorldMixin {
    @Shadow
    public native <T extends Entity> List<? extends T> getEntitiesByType(TypeFilter<Entity, T> typeFilter, Predicate<? super T> predicate);

    @Shadow
    @Final
    EntityList entityList;

    @Shadow
    @Final
    private MinecraftServer server;

    @Shadow
    @Final
    List<ServerPlayerEntity> players;

    @Shadow
    @Final
    private ServerWorldProperties worldProperties;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;setTimeOfDay(J)V"))
    private void sleeping(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        int bonusTicks = (int) (24000L - worldProperties.getTimeOfDay() % 24000L);
        getEntitiesByType(TypeFilter.instanceOf(AnimalEntity.class), entity -> entity instanceof AnimalEntityDuck).forEach(entity -> ((AnimalEntityDuck) entity).ecosystemic$addSleepingTicks(bonusTicks));
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/EntityList;forEach(Ljava/util/function/Consumer;)V"))
    private void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        if (server.getTicks() % ((World) (Object) this).getGameRules().getInt(GameRulesRegistry.ECOSYSTEMIC_VISITABLE_SPACES_PACKET_INTERVAL) == 0) {
            VisitedSpaceCountS2CPacket packet = new VisitedSpaceCountS2CPacket();
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            entityList.forEach(entity -> {
                if (entity instanceof AnimalEntityDuck duck) {
                    packet.add(entity.getUuid(), duck.ecosystemic$visitedSpaces().size());
                }
            });
            packet.write(buf);
            players.forEach(player -> ServerPlayNetworking.send(player, new Identifier("ecosystemic", "visitedspacecounts2cpacket"), buf));
        }
    }
}
