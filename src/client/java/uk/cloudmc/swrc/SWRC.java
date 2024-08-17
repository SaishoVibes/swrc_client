package uk.cloudmc.swrc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class SWRC implements ClientModInitializer {

	public static Logger LOGGER = LoggerFactory.getLogger("SWRC");
	public static final String NAMESPACE = "swrc";

	public static final MinecraftClient instance = MinecraftClient.getInstance();

	@Override
	public void onInitializeClient() {
		File config_folder = FabricLoader.getInstance().getConfigDir().resolve(NAMESPACE).toFile();
		if (!config_folder.exists()) {
			config_folder.mkdir();
		}
	}
}