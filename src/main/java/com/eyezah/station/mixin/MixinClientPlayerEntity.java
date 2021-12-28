package com.eyezah.station.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

import static com.eyezah.station.FabriStationAPI.runCommand;

@Mixin(ClientPlayerEntity.class)
abstract class MixinClientPlayerEntity {
	@Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
	private void onSendChatMessage(String message, CallbackInfo info) throws IOException {
		String[] parts;
		if (message.contains(" ")) {
			parts = message.split(" ");
		} else {
			parts = new String[]{message};
		}
		if (parts[0].equalsIgnoreCase("/s")) {
			runCommand(parts);
			info.cancel();
		}
	}
}
