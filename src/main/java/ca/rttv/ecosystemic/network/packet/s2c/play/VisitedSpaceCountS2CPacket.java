package ca.rttv.ecosystemic.network.packet.s2c.play;

import ca.rttv.ecosystemic.duck.EcosystemicClientPlayPacketListener;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public final class VisitedSpaceCountS2CPacket implements Packet<ClientPlayPacketListener> {
    private final Map<UUID, Integer> counts;

    public VisitedSpaceCountS2CPacket() {
        counts = new HashMap<>();
    }

    public VisitedSpaceCountS2CPacket(PacketByteBuf buf) {
        counts = new HashMap<>();
        int len = buf.readVarInt();
        for (int i = 0; i < len; i++) {
            counts.put(buf.readUuid(), buf.readInt());
        }
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(counts.size());
        for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
            buf.writeUuid(entry.getKey());
            buf.writeInt(entry.getValue());
        }
    }

    @Override
    public void apply(ClientPlayPacketListener listener) {
        ((EcosystemicClientPlayPacketListener) listener).ecosystemic$onVisitedSpaceCount(this);
    }

    public void forEach(BiConsumer<UUID, Integer> consumer) {
        for (Map.Entry<UUID, Integer> entry : counts.entrySet()) {
            consumer.accept(entry.getKey(), entry.getValue());
        }
    }

    public void add(UUID uuid, int count) {
        counts.put(uuid, count);
    }
}
