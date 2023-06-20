package ca.rttv.ecosystemic.network.packet.s2c.play;

import ca.rttv.ecosystemic.duck.EcosystemicClientPlayPacketListener;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;

import java.util.UUID;

public record PenSizeS2CPacket(UUID uuid, int size) implements Packet<ClientPlayPacketListener> {
    public PenSizeS2CPacket(PacketByteBuf buf) {
        this(buf.readUuid(), buf.readVarInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeUuid(uuid);
        buf.writeVarInt(size);
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        ((EcosystemicClientPlayPacketListener) listener).ecosystemic$onPenSize(this);
    }
}
