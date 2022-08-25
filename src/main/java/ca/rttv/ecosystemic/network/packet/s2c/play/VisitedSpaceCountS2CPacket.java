package ca.rttv.ecosystemic.network.packet.s2c.play;

import ca.rttv.ecosystemic.duck.EcosystemicClientPlayPacketListener;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

import java.util.UUID;

public record VisitedSpaceCountS2CPacket(int count, UUID uuid) implements Packet<ClientPlayPacketListener> {
    public VisitedSpaceCountS2CPacket(PacketByteBuf buf) {
        this(buf.readInt(), buf.readUuid());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeInt(count);
        buf.writeUuid(uuid);
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        ((EcosystemicClientPlayPacketListener) listener).ecosystemic$onVisitedSpaceCount(this);
    }
}
