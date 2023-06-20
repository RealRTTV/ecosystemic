package ca.rttv.ecosystemic.mixin.net.minecraft.world;

import ca.rttv.ecosystemic.duck.EntityListDuck;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.world.EntityList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Consumer;

@Mixin(EntityList.class)
public class EntityListMixin implements EntityListDuck {
    @Shadow
    private Int2ObjectMap<Entity> entities;

    @Override
    public void nonblockingForEach(Consumer<Entity> action) {
        for (Entity entity : entities.values()) {
            action.accept(entity);
        }
    }
}
