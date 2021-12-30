package com.eyezah.station;

import java.io.IOException;

public class FabriStationAPI {
	public static String getArtist() {
		return FabriStation.artist;
	}
	public static String getTitle() {
		return FabriStation.title;
	}
	public static String getSubLine() {
		return FabriStation.line3;
	}
	public static boolean isActive() {
		return FabriStation.isActive;
	}
	public static String getDiscordUsername() {
		return FabriStation.discordName;
	}
	public static int runCommand(String[] parts) {
		try {
			return FabriStation.runCommand(parts);
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
	}
}
