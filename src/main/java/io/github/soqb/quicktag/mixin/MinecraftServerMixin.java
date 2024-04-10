package io.github.soqb.quicktag.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.soqb.quicktag.TagRegistry;
import io.github.soqb.quicktag.mixinhelper.HasTagRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityLike;
import net.minecraft.world.entity.EntityLookup;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin<T extends EntityLike>
        implements EntityLookup<T>, HasTagRegistry<T> {
    private TagRegistry<T> tagRegistry;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        tagRegistry = new TagRegistry<>(0);
    }

    @Override
    public TagRegistry<T> getTagRegistry() {
        return tagRegistry;
    }
}
