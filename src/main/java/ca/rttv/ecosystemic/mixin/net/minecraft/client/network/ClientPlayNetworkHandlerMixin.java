package ca.rttv.ecosystemic.mixin.net.minecraft.client.network;

import ca.rttv.ecosystemic.duck.EcosystemicClientPlayPacketListener;
import ca.rttv.ecosystemic.duck.PenDesireDuck;
import ca.rttv.ecosystemic.duck.WaterDesireDuck;
import ca.rttv.ecosystemic.network.packet.s2c.play.PenSizeS2CPacket;
import ca.rttv.ecosystemic.network.packet.s2c.play.ResetConsumingTimerS2CPacket;
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
    public void ecosystemic$onPenSize(PenSizeS2CPacket packet) {
            if (client.world != null && client.world.entityManager.getLookup().get(packet.uuid()) instanceof PenDesireDuck duck) {
                duck.ecosystemic$penSize(packet.size());
            }
    }

    @Override
    public void ecosystemic$onResetConsumingTimer(ResetConsumingTimerS2CPacket packet) {
        client.execute(() -> WorldUtils.getClientEntityByUuid(world, packet.uuid()).ifPresent(entity -> {
            if (entity instanceof WaterDesireDuck duck) {
                duck.ecosystemic$timer(packet.ticks(), packet.type());
            }
        }));
    }
}
