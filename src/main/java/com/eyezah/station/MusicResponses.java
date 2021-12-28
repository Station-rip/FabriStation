package com.eyezah.station;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.eyezah.station.FabriStation.sendMessage;

class MusicResponses {

	public static void send(String command, int type, JsonElement code) throws CommandSyntaxException {

		if (type == 0) {
			String emsg = code.getAsString();
			String msg = "";
			if (emsg.equals("inactive")) msg = "You're not currently using Station";
			if (emsg.equals("not in vc")) msg = "You're not in a voice channel.";
			if (emsg.equals("wrong vc")) msg = "Station is already active in another voice channel.";
			if (emsg.equals("queue maxed")) msg = "Your queue is full.";
			if (emsg.equals("below 0")) msg = "Number can't be below 0.";
			if (emsg.equals("nothing changed")) msg = "Nothing changed.";
			if (emsg.equals("no songs")) msg = "You've reached the end of your queue.";
			if (emsg.equals("invalid volume")) msg = "Invalid volume specified.";
			if (emsg.equals("already paused")) msg = "Station is already paused.";
			if (emsg.equals("already resumed")) msg = "Station is already resumed.";
			if (emsg.equals("already looped")) msg = "Station is already looped.";
			if (emsg.equals("already unlooped")) msg = "Station is already unlooped.";
			if (emsg.equals("invalid values")) msg = "Invalid value specified.";
			if (emsg.equals("invalid url")) msg = "Invalid URL specified.";
			if (emsg.equals("rate limited")) {
				if (command.equals("queue")) {
					msg = "dontmessage";
					send("queue", FabriStation.queueType, FabriStation.queueData);
				} else {
					msg = "This action was rate limited. Try again in a few seconds.";
				}
			}
			if (emsg.equals("no text channel")) msg = "Your discord server isn't set up for Station.";
			if (emsg.equals("invalid user token")) {
				msg = "You're not connected to a Discord account.";
				FabriStation.token = "";
			}
			if (emsg.equals("no track")) msg = "Couldn't find your music.";
			if (emsg.equals("no station")) msg = "That station doesn't exist.";
			if (emsg.equals("no queue")) {
				if (command.equals("shuffle")) {
					msg = "Your queue isn't long enough to shuffle.";
				} else {
					msg = "Your queue is empty.";
				}
			}
			if (emsg.equals("inactive") || emsg.equals("not in vc") || emsg.equals("wrong vc") || emsg.equals("no songs") || emsg.equals("invalid user token") || (emsg.equals("no queue") && !command.equals("shuffle"))) {
				FabriStation.artist = "";
				FabriStation.title = "";
				FabriStation.isActive = false;
			}
			if (msg.equals("")) msg = "Something went wrong: " + emsg;

			if (!msg.equals("dontmessage")) sendMessage(msg);
		} else {
			String msg = "";

			if (command.equals("tune")) {
				JsonObject jsonObject = code.getAsJsonObject();
				msg = "Tuning to §b" + jsonObject.get("displayname").getAsString() + "§f!";
			} else if (command.equals("leave")) {
				msg = "Station disconnected from your voice channel.";
			} else if (command.equals("pause")) {
				msg = "Paused.";
			} else if (command.equals("resume")) {
				msg = "Resumed.";
			} else if (command.equals("shuffle")) {
				msg = "Shuffled your queue.";
			} else if (command.equals("clear")) {
				msg = "Cleared your queue.";
			} else if (command.equals("loop")) {
				msg = "Looped current track.";
			} else if (command.equals("unloop")) {
				msg = "Unlooped current track";
			} else if (command.equals("volume")) {
				msg = "Set volume to §b%" + code.getAsString() + "§f.";
			} else if (command.equals("next")) {
				JsonObject jsonObject = code.getAsJsonObject();
				msg = "Skipping to §b" + jsonObject.get("title").getAsString() + "§f by §b" + jsonObject.get("artist").getAsString() + "§f.";
				FabriStation.artist = jsonObject.get("artist").getAsString();
				FabriStation.title = jsonObject.get("title").getAsString();
			} else if (command.equals("play")) {
				JsonObject jsonObject = code.getAsJsonObject();
				if (jsonObject.get("position").getAsInt() == 0) {
					msg = "Playing §b" + jsonObject.get("title").getAsString() + "§f by §b" + jsonObject.get("artist").getAsString() + "§f.";
					FabriStation.artist = jsonObject.get("artist").getAsString();
					FabriStation.title = jsonObject.get("title").getAsString();
				} else {
					msg = "Queued §b" + jsonObject.get("title").getAsString() + "§f by §b" + jsonObject.get("artist").getAsString() + "§f.";
				}
			} else if (command.equals("stations")) {
				msg = "\n§f§oTry this station";
				JsonArray jsonArray = code.getAsJsonArray();
				int i = 0;
				while (i < jsonArray.size() && i < 1) {
					JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
					msg += "\n\n§b§l" + jsonObject.get("displayname").getAsString() + "\n\n§7§o" + jsonObject.get("description").getAsString() + "\n§f   §n/s tune " + jsonObject.get("triggername").getAsString() + "\n";
					i++;
				}
			} else if (command.equals("queue")) {
				JsonArray jsonArray = code.getAsJsonObject().get("queue").getAsJsonArray();
				int i = 1;
				msg = "\n§f§oUpcoming tracks";
				while (i < jsonArray.size() && i < 4) {
					JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
					msg += "\n\n§f" + i + ". §7" + jsonObject.get("artist").getAsString() + "\n§b   " + jsonObject.get("title").getAsString();
					i++;
				}
				if (i == 1) msg = "Your queue is empty.";
				if (jsonArray.size() > 0) {
					FabriStation.artist = jsonArray.get(0).getAsJsonObject().get("artist").getAsString();
					FabriStation.title = jsonArray.get(0).getAsJsonObject().get("title").getAsString();
					FabriStation.isActive = true;
				} else {
					FabriStation.artist = "";
					FabriStation.title = "";
					FabriStation.isActive = false;
				}
			}
			if (msg.equals("")) {
				sendMessage("Success but unknown command: " + command);
			} else {
				sendMessage(msg);
			}
		}
	}


}
