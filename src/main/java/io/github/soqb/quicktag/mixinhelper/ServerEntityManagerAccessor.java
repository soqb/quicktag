package io.github.soqb.quicktag.mixinhelper;

import net.minecraft.world.entity.EntityIndex;
import net.minecraft.world.entity.EntityLike;

public interface ServerEntityManagerAccessor<T extends EntityLike> {
    public EntityIndex<T> getIndex();
}
