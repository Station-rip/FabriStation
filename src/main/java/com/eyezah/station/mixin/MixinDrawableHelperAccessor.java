package com.eyezah.station.mixin;

import net.minecraft.client.gui.DrawableHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(DrawableHelper.class)
public interface MixinDrawableHelperAccessor {
	@Accessor("zOffset")
	int getZOffset();
}
