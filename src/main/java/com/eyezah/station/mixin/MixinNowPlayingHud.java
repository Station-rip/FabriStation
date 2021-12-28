package com.eyezah.station.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.eyezah.station.FabriStationAPI.*;

@Mixin(InGameHud.class)
class MixinNowPlayingHud {

	@Inject(at = @At("TAIL"), method = "render")
	public void render(MatrixStack matrixStack, float tickDelta, CallbackInfo info) {
		MinecraftClient client = MinecraftClient.getInstance();

		if (!client.options.debugEnabled) {

			float textPosX = 5;
			float textPosY = 5;

			double guiScale = client.getWindow().getScaleFactor();
			if (guiScale > 0) {
				textPosX /= guiScale;
				textPosY /= guiScale;
			}

			int textColor = ((230 & 0xFF) << 24) | 0xEEEEEE;

			float height3 = textPosY + (client.textRenderer.fontHeight * 3);

			if (getArtist().equals("") && getTitle().equals("")) {
				height3 = textPosY;
			}

			if (true) {
				if (!getArtist().equals("")) client.textRenderer.drawWithShadow(matrixStack, getArtist(), client.getWindow().getScaledWidth() - textPosX - client.textRenderer.getWidth(getArtist()), textPosY, textColor);
				if (!getTitle().equals("")) client.textRenderer.drawWithShadow(matrixStack, getTitle(), client.getWindow().getScaledWidth() - textPosX - client.textRenderer.getWidth(getTitle()), textPosY + client.textRenderer.fontHeight, textColor);
				if (!getSubLine().equals("")) {
					client.textRenderer.drawWithShadow(matrixStack, getSubLine(), client.getWindow().getScaledWidth() - textPosX - client.textRenderer.getWidth(getSubLine()), height3, textColor);
				}
			} else {
				if (!getArtist().equals("")) client.textRenderer.draw(matrixStack, getArtist(), client.getWindow().getScaledWidth() - textPosX - client.textRenderer.getWidth(getArtist()), textPosY, textColor);
				if (!getTitle().equals("")) client.textRenderer.draw(matrixStack, getTitle(), client.getWindow().getScaledWidth() - textPosX - client.textRenderer.getWidth(getTitle()), textPosY + client.textRenderer.fontHeight, textColor);
				if (!getSubLine().equals("")) {
					client.textRenderer.draw(matrixStack, getSubLine(), client.getWindow().getScaledWidth() - textPosX - client.textRenderer.getWidth(getSubLine()), height3, textColor);
				}
			}
		}
	}
}
