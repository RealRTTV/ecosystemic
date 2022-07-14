package ca.rttv.ecosystemic.network.packet.s2c.play;

import ca.rttv.ecosystemic.duck.EcosystemicClientPlayPacketListener;
import io.netty.buffer.Unpooled;
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

    public PacketByteBuf toBuf() {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        write(buf);
        return buf;
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        ((EcosystemicClientPlayPacketListener) listener).onVisitedSpaceCount(this);
    }
}
