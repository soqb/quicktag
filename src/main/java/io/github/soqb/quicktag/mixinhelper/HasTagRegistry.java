package io.github.soqb.quicktag.mixinhelper;

import io.github.soqb.quicktag.TagRegistry;
import net.minecraft.world.entity.EntityLike;

public interface HasTagRegistry<T extends EntityLike> {
    public TagRegistry<T> getTagRegistry();
}
