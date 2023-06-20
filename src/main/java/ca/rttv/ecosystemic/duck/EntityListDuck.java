package ca.rttv.ecosystemic.duck;

import net.minecraft.entity.Entity;

import java.util.function.Consumer;

public interface EntityListDuck {
    void nonblockingForEach(Consumer<Entity> action);
}