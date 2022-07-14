package ca.rttv.ecosystemic.util;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

import java.util.Optional;
import java.util.UUID;

public class WorldUtils {
    public static Optional<Entity> getClientEntityByUuid(ClientWorld world, UUID uuid) {
        return Optional.ofNullable(world.entityManager.getLookup().get(uuid));
    }

    public static Optional<Entity> getServerEntityByUuid(ServerWorld world, UUID uuid) {
        return Optional.ofNullable(world.entityManager.getLookup().get(uuid));
    }
}
