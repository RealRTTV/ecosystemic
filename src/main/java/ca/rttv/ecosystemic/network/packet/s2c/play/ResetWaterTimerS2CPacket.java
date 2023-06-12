package ca.rttv.ecosystemic.network.packet.s2c.play;

import ca.rttv.ecosystemic.duck.EcosystemicClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

import java.util.UUID;

public record ResetWaterTimerS2CPacket(UUID uuid) implements Packet<ClientPlayPacketListener> {
    public ResetWaterTimerS2CPacket(PacketByteBuf buf) { this(buf.readUuid()); }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(uuid);
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        ((EcosystemicClientPlayPacketListener) listener).ecosystemic$onResetWaterTimer(this);
    }
}
