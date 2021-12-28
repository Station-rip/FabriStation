package com.eyezah.station;

import com.google.gson.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
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
import static com.eyezah.station.Response.request;
import static net.minecraft.server.command.CommandManager.literal;

public class FabriStation implements ModInitializer {
	private static String server = "https://station.rip/api/";
	protected static String token = "";
	public static final Logger LOGGER = LogManager.getLogger("fabristation");

	public static String artist = "";
	public static String title = "";
	public static String line3 = "";
	private static int requestCount = 0;
	public static int queueType = 0;
	public static JsonElement queueData;

	private JsonElement config;

	public static void newRequest() {
		requestCount++;
		if (requestCount > 0) {
			line3 = "Talking to Station...";
		} else {
			line3 = "";
		}
	}

	public static void doneRequest() {
		requestCount--;
		if (requestCount > 0) {
			line3 = "Talking to Station...";
		} else {
			line3 = "";
		}
	}

	public static void error(ServerCommandSource source) {
		error(source, "Something went wrong. Try again later.");
	}

	static void error(ServerCommandSource source, String message) {
		try {
			source.getPlayer().sendMessage(Text.of(message), MessageType.SYSTEM, MinecraftClient.getInstance().player.getUuid());
		} catch (CommandSyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onInitialize() {
		LOGGER.info("The Gigachad has awoken");


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

		Thread timeoutThread = new Thread(() -> {
			try {
					avoidTimeout();
			} catch (InterruptedException | IOException e) {
				e.printStackTrace();
			}
		});
		timeoutThread.start();

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(literal("s")
				.then(literal("token")
					.executes(ctx -> {
						ctx.getSource().getPlayer().sendMessage(Text.of("Usage: §b/s token <token>§f\nObtain your token from §bhttps://station.rip#connect"), MessageType.SYSTEM, MinecraftClient.getInstance().player.getUuid());
						return 1;
					})
					.then(CommandManager.argument("message", greedyString())
						.executes(ctx -> {
							Thread checkTokenThread = new Thread(() -> {
								try {
									setToken(StringArgumentType.getString(ctx, "message"), ctx.getSource());
								} catch (CommandSyntaxException e) {
									e.printStackTrace();
									error(ctx.getSource(), "Your token was invalid. get a valid one from §bhttps://station.rip#tokens");
								}
							});
							checkTokenThread.start();
							return 1;
						})
					)
				)
				.then(literal("tune")
					.executes(ctx -> {
						ctx.getSource().getPlayer().sendMessage(Text.of("Usage: §b/s tune <station>"), MessageType.SYSTEM, MinecraftClient.getInstance().player.getUuid());
						return 1;
					})
					.then(CommandManager.argument("message", greedyString())
						.executes(ctx -> {
							try {
								request(server + "user-tune?token=" + token + "&query=" + base64Encode(StringArgumentType.getString(ctx, "message")), "tune", ctx.getSource());
							} catch (IOException e) {
								e.printStackTrace();
								error(ctx.getSource());
							}
							return 1;
						})
					)
				)
				.then(literal("leave")
					.executes(ctx -> {
						try {
							request(server + "user-leave?token=" + token, "leave", ctx.getSource());
						} catch (IOException e) {
							e.printStackTrace();
							error(ctx.getSource());
						}
						return 1;
					})
				)
				.then(literal("pause")
					.executes(ctx -> {
						try {
							request(server + "user-pause?token=" + token + "&pausetype=pause", "pause", ctx.getSource());
						} catch (IOException e) {
							e.printStackTrace();
							error(ctx.getSource());
						}
						return 1;
					})
				)
				.then(literal("resume")
					.executes(ctx -> {
						try {
							request(server + "user-pause?token=" + token + "&pausetype=resume", "resume", ctx.getSource());
						} catch (IOException e) {
							e.printStackTrace();
							error(ctx.getSource());
						}
						return 1;
					})
				)
				.then(literal("loop")
					.executes(ctx -> {
						try {
							request(server + "user-loop?token=" + token + "&looptype=loop", "loop", ctx.getSource());
						} catch (IOException e) {
							e.printStackTrace();
							error(ctx.getSource());
						}
						return 1;
					})
				)
				.then(literal("unloop")
					.executes(ctx -> {
						try {
							request(server + "user-loop?token=" + token + "&looptype=unloop", "unloop", ctx.getSource());
						} catch (IOException e) {
							e.printStackTrace();
							error(ctx.getSource());
						}
						return 1;
					})
				)
				.then(literal("shuffle")
					.executes(ctx -> {
						try {
							request(server + "user-shuffle?token=" + token, "shuffle", ctx.getSource());
						} catch (IOException e) {
							e.printStackTrace();
							error(ctx.getSource());
						}
						return 1;
					})
				)
				.then(literal("clear")
					.executes(ctx -> {
						try {
							request(server + "user-clear?token=" + token, "clear", ctx.getSource());
						} catch (IOException e) {
							e.printStackTrace();
							error(ctx.getSource());
						}
						return 1;
					})
				)
				.then(literal("next")
					.executes(ctx -> {
						try {
							request(server + "user-next?token=" + token, "next", ctx.getSource());
						} catch (IOException e) {
							e.printStackTrace();
							error(ctx.getSource());
						}
						return 1;
					})
				)
				.then(literal("play")
					.executes(ctx -> {
						ctx.getSource().getPlayer().sendMessage(Text.of("Usage: §b/s play <song/url>"), MessageType.SYSTEM, MinecraftClient.getInstance().player.getUuid());
						return 1;
					})
					.then(CommandManager.argument("message", greedyString())
						.executes(ctx -> {
							try {
								request(server + "user-play?token=" + token + "&query=" + base64Encode(StringArgumentType.getString(ctx, "message")), "play", ctx.getSource());
							} catch (IOException e) {
								e.printStackTrace();
								error(ctx.getSource());
							}
							return 1;
						})
					)
				)
				.then(literal("stations")
					.executes(ctx -> {
						try {
							request(server + "public-explore-stations", "stations", ctx.getSource());
						} catch (IOException e) {
							e.printStackTrace();
							error(ctx.getSource());
						}
						return 1;
					})
				)
				.then(literal("queue")
					.executes(ctx -> {
						try {
							request(server + "user-get-queue?token=" + token, "queue", ctx.getSource());
						} catch (IOException e) {
							e.printStackTrace();
							error(ctx.getSource());
						}
						return 1;
					})
				)
				.then(literal("volume")
						.executes(ctx -> {
							ctx.getSource().getPlayer().sendMessage(Text.of("Usage: §b/s volume <volume>"), MessageType.SYSTEM, MinecraftClient.getInstance().player.getUuid());
							return 1;
						})
						.then(CommandManager.argument("message", greedyString())
								.executes(ctx -> {
									try {
										System.out.println(server + "user-volume?token=" + token + "&volume=" + StringArgumentType.getString(ctx, "message"));
										request(server + "user-volume?token=" + token + "&volume=" + StringArgumentType.getString(ctx, "message"), "volume", ctx.getSource());
									} catch (IOException e) {
										e.printStackTrace();
										error(ctx.getSource());
									}
									return 1;
								})
						)
				)
			);
		});
	}

	private void setToken(String token, ServerCommandSource source) throws CommandSyntaxException {
		newRequest();
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			final HttpGet httpGet = new HttpGet(server + "user-get-profile?token=" + token);
			try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
				String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
				JsonParser parser = new JsonParser();
				JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();
				doneRequest();
				if (jsonObject.get("type").getAsInt() == 0 && jsonObject.get("code").getAsString().equals("invalid user token")) {
					source.getPlayer().sendMessage(Text.of("Your token was invalid. get a valid one from §bhttps://station.rip#tokens"), MessageType.SYSTEM, MinecraftClient.getInstance().player.getUuid());
				} else {
					if (jsonObject.get("type").getAsInt() == 1) {
						source.getPlayer().sendMessage(Text.of("Your Discord account §b" + jsonObject.get("code").getAsJsonObject().get("name").getAsString() + "§f has been connected!"), MessageType.SYSTEM, MinecraftClient.getInstance().player.getUuid());
					} else {
						source.getPlayer().sendMessage(Text.of("Your Discord account has been connected!"), MessageType.SYSTEM, MinecraftClient.getInstance().player.getUuid());
					}
					this.token = token;
					FileWriter updatingToken = new FileWriter(FabricLoader.getInstance().getConfigDir().toString() + "\\fabristation.txt");
					updatingToken.write(token);
					updatingToken.close();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			error(source, "Your token was invalid. get a valid one from §bhttps://station.rip#tokens");
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
					} else {
						JsonArray queue = jsonObject.get("code").getAsJsonObject().get("queue").getAsJsonArray();
						if (queue.size() > 0) {
							artist = queue.get(0).getAsJsonObject().get("artist").getAsString();
							title = queue.get(0).getAsJsonObject().get("title").getAsString();
						} else {
							artist = "";
							title = "";
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			Thread.sleep(5 * 1000);
		}
	}

	public String base64Encode(String string) {
		byte[] encodedBytes = Base64.getEncoder().encode(string.getBytes());
		return new String(encodedBytes);
	}
}
