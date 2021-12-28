package com.eyezah.station;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.http.ParseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Response {

	public static void request(String request, String command) throws ParseException, IOException {
		if (FabriStation.token.equals("")) {
			FabriStation.sendMessage("You're not connected to a Discord account.");
		} else {
			LOOKUP_THREAD.execute(() -> {
				try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
					FabriStation.newRequest();
					final HttpGet httpGet = new HttpGet(request);

					try (CloseableHttpResponse response = httpClient.execute(httpGet)) {

						String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
						JsonParser parser = new JsonParser();
						JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();
						MusicResponses.send(command, jsonObject.get("type").getAsInt(), jsonObject.get("code"));
						FabriStation.doneRequest();
					}
				} catch (IOException | CommandSyntaxException e) {
					e.printStackTrace();
					FabriStation.doneRequest();
					FabriStation.sendMessage("Couldn't connect to Station. Are you online?");
				}
			});
		}
	}

	private static final ExecutorService LOOKUP_THREAD = Executors.newSingleThreadExecutor();
}