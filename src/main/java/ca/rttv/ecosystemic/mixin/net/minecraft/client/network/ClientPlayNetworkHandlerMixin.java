package ca.rttv.ecosystemic.mixin.net.minecraft.client.network;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import ca.rttv.ecosystemic.duck.EcosystemicClientPlayPacketListener;
import ca.rttv.ecosystemic.network.packet.s2c.play.ResetWaterTimerS2CPacket;
import ca.rttv.ecosystemic.network.packet.s2c.play.VisitedSpaceCountS2CPacket;
import ca.rttv.ecosystemic.util.WorldUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("unused")
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements EcosystemicClientPlayPacketListener {
    @Shadow
    private ClientWorld world;

    @Shadow
    @Final
    private MinecraftClient client;

    @Override
    public void ecosystemic$onVisitedSpaceCount(VisitedSpaceCountS2CPacket packet) {
        client.execute(() -> packet.forEach((uuid, count) -> {
            AnimalEntityDuck duck = (AnimalEntityDuck) client.world.entityManager.getLookup().get(uuid);
            if (duck != null) {
                duck.ecosystemic$visitedSpaceCount(count);
            }
        }));
    }

    @Override
    public void ecosystemic$onResetWaterTimer(ResetWaterTimerS2CPacket resetWaterTimerS2CPacket) {
        client.execute(() -> WorldUtils.getClientEntityByUuid(world, resetWaterTimerS2CPacket.uuid()).ifPresent(entity -> {
            if (entity instanceof AnimalEntityDuck duck) {
                duck.ecosystemic$waterTimer(40);
            }
        }));
    }
}
