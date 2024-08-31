package uk.cloudmc.swrc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.cloudmc.swrc.hud.Hud;
import uk.cloudmc.swrc.hud.RaceLeaderboard;
import uk.cloudmc.swrc.hud.SplitTime;
import uk.cloudmc.swrc.track.TrackBuilder;

import java.io.File;

public class SWRC implements ClientModInitializer {

	public static Logger LOGGER = LoggerFactory.getLogger("SWRC");
	public static final String NAMESPACE = "swrc";
	public static final String VERSION = "2.0.0";

	public static final MinecraftClient instance = MinecraftClient.getInstance();

	private static Race race;
	private static TrackBuilder trackBuilder;

	public static final Hud raceLeaderboard = new RaceLeaderboard();
	public static final Hud splitTime = new SplitTime();

	@Override
	public void onInitializeClient() {
		File config_folder = FabricLoader.getInstance().getConfigDir().resolve(NAMESPACE).toFile();
		if (!config_folder.exists()) {
			config_folder.mkdir();
		}

		File tracks_folder = FabricLoader.getInstance().getConfigDir().resolve(NAMESPACE).resolve("tracks").toFile();
		if (!tracks_folder.exists()) {
			tracks_folder.mkdir();
		}

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(Commands.root));

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if (client.world == null) return;
			if (race == null) return;

			race.update();
		});

		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
			if (raceLeaderboard.shouldRender()) {
				raceLeaderboard.render(drawContext, tickDelta);
			}
			if (splitTime.shouldRender()) {
				splitTime.render(drawContext, tickDelta);
			}
		});
	}

	public static TrackBuilder getTrackBuilder() {
		return trackBuilder;
	}

	public static void setTrackBuilder(TrackBuilder trackBuilder) {
		SWRC.trackBuilder = trackBuilder;
	}

	public static Race getRace() {
		return race;
	}

	public static void setRace(Race race) {
		SWRC.race = race;
	}

	public static String getRaceName() {
		return race.getTrackName();
	}
}