package com.eyezah.station.mixin;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static net.minecraft.client.gui.DrawableHelper.drawSprite;
import static net.minecraft.client.gui.DrawableHelper.drawTexture;

@Mixin(InGameHud.class)
@Environment(EnvType.CLIENT)
public class MixinInGameHud {
	@Shadow private int scaledWidth;

	/**
	 * @author eyezah (FabriStation)
	 * @reason moves status effect overlays down
	 */
	@Overwrite
	public void renderStatusEffectOverlay(MatrixStack matrices) {
		MinecraftClient client = MinecraftClient.getInstance();
		int zOffset = ((MixinDrawableHelperAccessor) this).getZOffset();
		Collection collection;
		label40: {
			collection = client.player.getStatusEffects();
			if (!collection.isEmpty()) {
				Screen var4 = client.currentScreen;
				if (!(var4 instanceof AbstractInventoryScreen)) {
					break label40;
				}

				AbstractInventoryScreen abstractInventoryScreen = (AbstractInventoryScreen)var4;
				if (!abstractInventoryScreen.hideStatusEffectHud()) {
					break label40;
				}
			}

			return;
		}

		RenderSystem.enableBlend();
		int abstractInventoryScreen = 0;
		int i = 0;
		StatusEffectSpriteManager statusEffectSpriteManager = client.getStatusEffectSpriteManager();
		List<Runnable> list = Lists.newArrayListWithExpectedSize(collection.size());
		RenderSystem.setShaderTexture(0, HandledScreen.BACKGROUND_TEXTURE);
		Iterator var7 = Ordering.natural().reverse().sortedCopy(collection).iterator();

		while(var7.hasNext()) {
			StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var7.next();
			StatusEffect statusEffect = statusEffectInstance.getEffectType();
			if (statusEffectInstance.shouldShowIcon()) {
				int j = scaledWidth;
				int k = 6 + (client.textRenderer.fontHeight * 4);
				if (client.isDemo()) {
					k += 15;
				}

				if (statusEffect.isBeneficial()) {
					++abstractInventoryScreen;
					j -= 25 * abstractInventoryScreen;
				} else {
					++i;
					j -= 25 * i;
					k += 26;
				}

				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				float f = 1.0F;
				if (statusEffectInstance.isAmbient()) {
					drawTexture(matrices, j, k, zOffset, (float)165, (float)166, 24, 24, 256, 256);
				} else {
					drawTexture(matrices, j, k, zOffset, (float)141, (float)166, 24, 24, 256, 256);
					if (statusEffectInstance.getDuration() <= 200) {
						int l = 10 - statusEffectInstance.getDuration() / 20;
						f = MathHelper.clamp((float)statusEffectInstance.getDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos((float)statusEffectInstance.getDuration() * 3.1415927F / 5.0F) * MathHelper.clamp((float)l / 10.0F * 0.25F, 0.0F, 0.25F);
					}
				}

				Sprite l = statusEffectSpriteManager.getSprite(statusEffect);
				float finalF = f;
				int finalK = k;
				int finalJ = j;
				list.add(() -> {
					RenderSystem.setShaderTexture(0, l.getAtlas().getId());
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, finalF);
					drawSprite(matrices, finalJ + 3, finalK + 3, zOffset, 18, 18, l);
				});
			}
		}

		list.forEach(Runnable::run);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}
}
