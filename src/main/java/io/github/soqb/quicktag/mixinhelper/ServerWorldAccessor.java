package io.github.soqb.quicktag.mixinhelper;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerEntityManager;

public interface ServerWorldAccessor {
    public ServerEntityManager<Entity> getEntityManager();
}
