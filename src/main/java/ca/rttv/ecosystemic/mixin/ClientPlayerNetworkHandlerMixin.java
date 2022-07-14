package ca.rttv.ecosystemic.mixin;

import ca.rttv.ecosystemic.duck.AnimalEntityDuck;
import ca.rttv.ecosystemic.duck.EcosystemicClientPlayPacketListener;
import ca.rttv.ecosystemic.network.packet.s2c.play.VisitedSpaceCountS2CPacket;
import ca.rttv.ecosystemic.util.WorldUtils;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPlayNetworkHandler.class)
final class ClientPlayerNetworkHandlerMixin implements EcosystemicClientPlayPacketListener {
    @Shadow
    private ClientWorld world;

    @Override
    public void onVisitedSpaceCount(VisitedSpaceCountS2CPacket packet) {
        WorldUtils.getClientEntityByUuid(world, packet.uuid()).ifPresent(entity -> {
            if (entity instanceof AnimalEntityDuck duck) {
                duck.ecosystemic$visitedSpaceCount(packet.count());
            }
        });
    }
}
