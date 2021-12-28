package com.eyezah.station;

import com.google.gson.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Scanner;

import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.literal;

public class FabriStation implements ModInitializer {
	private static String server = "https://station.rip/api/";
	protected static String token = "";
	protected static final Logger LOGGER = LogManager.getLogger("FabriStation");

	protected static String artist = "";
	protected static String title = "";
	protected static String line3 = "";
	protected static String discordName = "";
	protected static boolean isActive = false;
	private static int requestCount = 0;
	protected static int queueType = 0;
	protected static JsonElement queueData;

	private JsonElement config;

	protected static void newRequest() {
		requestCount++;
		if (requestCount > 0) {
			line3 = "Talking to Station...";
		} else {
			line3 = "";
		}
	}

	protected static void doneRequest() {
		requestCount--;
		if (requestCount > 0) {
			line3 = "Talking to Station...";
		} else {
			line3 = "";
		}
	}

	protected static void error() {
		sendMessage("Something went wrong. Try again later.");
	}

	@Override
	public void onInitialize() {
		LOGGER.warn("The Gigachad has awoken");
		LOGGER.error("It's too late! Save yourselves!");


		File file = new File(FabricLoader.getInstance().getConfigDir().toFile(), "fabristation.txt");

		if (file.exists()) {
			File configFile = new File(FabricLoader.getInstance().getConfigDir().toString() + "\\fabristation.txt");
			try {
				Scanner configScanner = new Scanner(configFile);
				String tempToken = "";
				while (configScanner.hasNextLine()) {
					tempToken = configScanner.nextLine();
				}
				token = tempToken;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				throw new UncheckedIOException("Couldn't load FabriStation config :(", e);
			}
		} else {
			try {
				new File(FabricLoader.getInstance().getConfigDir().toString() + "\\fabristation.txt").createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		if (!token.equals("")) setToken(token, true);

		Thread timeoutThread = new Thread(() -> {
			try {
					avoidTimeout();
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		});
		timeoutThread.start();
	}

	private static void setToken(String tryToken) throws CommandSyntaxException {
		setToken(tryToken, false);
	}

	private static void setToken(String tryToken, boolean forProfile) {
		newRequest();
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			final HttpGet httpGet = new HttpGet(server + "user-get-profile?token=" + tryToken);
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
				JsonParser parser = new JsonParser();
				JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();
				doneRequest();
				if (jsonObject.get("type").getAsInt() == 0 && jsonObject.get("code").getAsString().equals("invalid user token")) {
					if (!forProfile) sendMessage("Your token was invalid. get a valid one from §bhttps://station.rip#tokens");
				} else {
					if (jsonObject.get("type").getAsInt() == 1) {
						if (!forProfile) sendMessage("Your Discord account §b" + jsonObject.get("code").getAsJsonObject().get("name").getAsString() + "§f has been connected!");
						discordName = jsonObject.get("code").getAsJsonObject().get("name").getAsString() + "#" + jsonObject.get("code").getAsJsonObject().get("discriminator").getAsString();
						if (forProfile) LOGGER.info("Logged into Discord as " + discordName);
					} else {
						if (!forProfile) sendMessage("Your Discord account has been connected!");
					}
					token = tryToken;
					FileWriter updatingToken = new FileWriter(FabricLoader.getInstance().getConfigDir().toString() + "\\fabristation.txt");
					updatingToken.write(token);
					updatingToken.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (!forProfile) sendMessage("Your token was invalid. get a valid one from §bhttps://station.rip#tokens");
		}
	}

	private void avoidTimeout() throws InterruptedException, IOException {
		while (true) {
			try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
				final HttpGet httpGet = new HttpGet(server + "user-get-queue?token=" + token);
				try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
					String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
					JsonParser parser = new JsonParser();
					JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();
					if (!(jsonObject.get("type").getAsInt() == 0 && jsonObject.get("code").getAsString() == "rate limited")) {
						queueData = jsonObject.get("code");
						queueType = jsonObject.get("type").getAsInt();
					}
					if (queueType == 0) {
						artist = "";
						title = "";
						isActive = false;
					} else {
						JsonArray queue = jsonObject.get("code").getAsJsonObject().get("queue").getAsJsonArray();
						if (queue.size() > 0) {
							artist = queue.get(0).getAsJsonObject().get("artist").getAsString();
							title = queue.get(0).getAsJsonObject().get("title").getAsString();
							isActive = true;
						} else {
							artist = "";
							title = "";
							isActive = false;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Thread.sleep(5 * 1000);
		}
	}

	public static void registerCommands(CommandDispatcher<ServerCommandSource> registerer) {
				registerer.register(literal("s")
						.then(literal("token")
								.executes(ctx -> 1)
								.then(CommandManager.argument("message", greedyString())
										.executes(ctx -> 1)
								)
						)
						.then(literal("help")
								.executes(ctx -> 1)
						)
						.then(literal("tune")
								.executes(ctx -> 1)
								.then(CommandManager.argument("message", greedyString())
										.executes(ctx -> 1)
								)
						)
						.then(literal("leave")
								.executes(ctx -> 1)
						)
						.then(literal("pause")
								.executes(ctx -> 1)
						)
						.then(literal("resume")
								.executes(ctx -> 1)
						)
						.then(literal("loop")
								.executes(ctx -> 1)
						)
						.then(literal("unloop")
								.executes(ctx -> 1)
						)
						.then(literal("shuffle")
								.executes(ctx -> 1)
						)
						.then(literal("clear")
								.executes(ctx -> 1)
						)
						.then(literal("next")
								.executes(ctx -> 1)
						)
						.then(literal("play")
								.executes(ctx -> 1)
								.then(CommandManager.argument("message", greedyString())
										.executes(ctx -> 1)
								)
						)
						.then(literal("stations")
								.executes(ctx -> 1)
						)
						.then(literal("queue")
								.executes(ctx -> 1)
						)
						.then(literal("volume")
								.executes(ctx -> 1)
								.then(CommandManager.argument("message", greedyString())
										.executes(ctx -> 1)
								)
						)
						.executes(ctx -> 1)
				);

	}

	protected static void sendMessage(String message) {
		MinecraftClient.getInstance().player.sendSystemMessage(Text.of(message), MinecraftClient.getInstance().player.getUuid());
	}

	private static String greedy(String[] parts, int i) {
		if (i < 0) i = 0;
		String out = "";
		while (i < parts.length) {
			if (!out.equals("")) out += " ";
			out += parts[i];
			i++;
		}
		return out;
	}

	protected static int runCommand(String[] parts) throws IOException {
		if (parts.length < 2) {
			sendMessage("§b§lFabriStation§f for §bStation§f Discord music bot.\n\n§bhttps://station.rip");
			return 1;
		} else if (parts[1].equalsIgnoreCase("token")) {
			if (parts.length < 3) {
				sendMessage("Usage: §b/s token <token>§f\\nObtain your token from §bhttps://station.rip#connect");
				return 0;
			} else {
				Thread checkTokenThread = new Thread(() -> {
					try {
						setToken(greedy(parts, 2));
					} catch (CommandSyntaxException e) {
						e.printStackTrace();
						sendMessage("Your token was invalid. get a valid one from §bhttps://station.rip#tokens");
					}
				});
				checkTokenThread.start();
				return 1;
			}
		} else if (parts[1].equalsIgnoreCase("help")) {
			sendMessage("\n\n");
			runCommand(new String[]{"/s"});
			sendMessage("§f\nGo to §bhttps://station.rip#tokens§f to obtain your token.\n\nConnect your account using §b/s token <token>\n\n§fJoin a voice channel in a Discord server that has Station and control the music without leaving your game!");
			return 1;
		} else if (parts[1].equalsIgnoreCase("tune")) {
			if (parts.length < 3) {
				sendMessage("Usage: §b/s tune <station>");
				return 0;
			} else {
				Response.request(server + "user-tune?token=" + token + "&query=" + base64Encode(greedy(parts, 2)), "tune");
				return 1;
			}
		} else if (parts[1].equalsIgnoreCase("leave")) {
			Response.request(server + "user-leave?token=" + token, "leave");
			return 1;
		} else if (parts[1].equalsIgnoreCase("pause")) {
			Response.request(server + "user-pause?token=" + token + "&pausetype=pause", "pause");
			return 1;
		} else if (parts[1].equalsIgnoreCase("resume")) {
			Response.request(server + "user-pause?token=" + token + "&pausetype=resume", "resume");
			return 1;
		} else if (parts[1].equalsIgnoreCase("loop")) {
			Response.request(server + "user-loop?token=" + token + "&looptype=loop", "loop");
			return 1;
		} else if (parts[1].equalsIgnoreCase("unloop")) {
			Response.request(server + "user-loop?token=" + token + "&looptype=unloop", "unloop");
			return 1;
		} else if (parts[1].equalsIgnoreCase("shuffle")) {
			Response.request(server + "user-shuffle?token=" + token, "shuffle");
			return 1;
		} else if (parts[1].equalsIgnoreCase("clear")) {
			Response.request(server + "user-clear?token=" + token, "clear");
			return 1;
		} else if (parts[1].equalsIgnoreCase("next")) {
			Response.request(server + "user-next?token=" + token, "next");
			return 1;
		} else if (parts[1].equalsIgnoreCase("play")) {
			if (parts.length < 3) {
				sendMessage("Usage: §b/s play <song/url>");
				return 0;
			} else {
				Response.request(server + "user-play?token=" + token + "&query=" + base64Encode(greedy(parts, 2)), "play");
				return 1;
			}
		} else if (parts[1].equalsIgnoreCase("stations")) {
			Response.request(server + "public-explore-stations", "stations");
			return 1;
		} else if (parts[1].equalsIgnoreCase("queue")) {
			Response.request(server + "user-get-queue?token=" + token, "queue");
			return 1;
		} else if (parts[1].equalsIgnoreCase("volume")) {
			if (parts.length < 3) {
				sendMessage("Usage: §b/s volume <volume>");
				return 0;
			} else {
				Response.request(server + "user-volume?token=" + token + "&volume=" + parts[2], "volume");
				return 1;
			}
		}

		sendMessage("Unknown command. Type §/s help§f for help.");
		return 1;
	}

	private static String base64Encode(String string) {
		byte[] encodedBytes = Base64.getEncoder().encode(string.getBytes());
		return new String(encodedBytes);
	}
}
