package uk.cloudmc.swrc;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.text.Text;

public class SWRCModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            SWRCConfig config = SWRCConfig.getInstance();

            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.of("SWRC Configuration"))
                    .setSavingRunnable(config::save);

            ConfigCategory Category = builder.getOrCreateCategory(Text.of("Settings"));
            ConfigEntryBuilder packet = builder.entryBuilder();

            Category.addEntry(packet.startStrField(Text.of("RC Key"), config.rc_key)
                    .setDefaultValue("")
                    .setSaveConsumer(newValue -> config.rc_key = newValue)
                    .build());

            Category.addEntry(packet.startStrField(Text.of("Race Header Text"), config.header_text)
                    .setDefaultValue("S1 @ %s")
                    .setSaveConsumer(newValue -> config.header_text = newValue)
                    .build());


            return builder.build();
        };
    }
}
