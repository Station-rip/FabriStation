package com.eyezah.station.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.eyezah.station.FabriStation.*;

@Mixin(InGameHud.class)
public class NowPlayingHud {

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

			/*float maxTextPosX = client.getWindow().getScaledWidth() - client.textRenderer.getWidth(displayString);
			float maxTextPosY = client.getWindow().getScaledHeight() - client.textRenderer.fontHeight;
			textPosX = Math.min(textPosX, maxTextPosX);
			textPosY = Math.min(textPosY, maxTextPosY);*/

			int textColor = ((230 & 0xFF) << 24) | 0xEEEEEE;

			float height3 = textPosY + (client.textRenderer.fontHeight * 3);

			if (artist.equals("") && title.equals("")) {
				height3 = textPosY;
			}

			if (true) {
				if (!artist.equals("")) client.textRenderer.drawWithShadow(matrixStack, artist, client.getWindow().getScaledWidth() - textPosX - client.textRenderer.getWidth(artist), textPosY, textColor);
				if (!title.equals("")) client.textRenderer.drawWithShadow(matrixStack, title, client.getWindow().getScaledWidth() - textPosX - client.textRenderer.getWidth(title), textPosY + client.textRenderer.fontHeight, textColor);
				if (!line3.equals("")) {
					client.textRenderer.drawWithShadow(matrixStack, line3, client.getWindow().getScaledWidth() - textPosX - client.textRenderer.getWidth(line3), height3, textColor);
				}
			} else {
				if (!artist.equals("")) client.textRenderer.draw(matrixStack, artist, client.getWindow().getScaledWidth() - textPosX - client.textRenderer.getWidth(artist), textPosY, textColor);
				if (!title.equals("")) client.textRenderer.draw(matrixStack, title, client.getWindow().getScaledWidth() - textPosX - client.textRenderer.getWidth(title), textPosY + client.textRenderer.fontHeight, textColor);
				if (!line3.equals("")) {
					client.textRenderer.draw(matrixStack, line3, client.getWindow().getScaledWidth() - textPosX - client.textRenderer.getWidth(line3), height3, textColor);
				}
			}
		}
	}
}
