package vulpisgiardina.simplekey.event;

import vulpisgiardina.simplekey.Simplekey;
import vulpisgiardina.simplekey.client.screen.KeyWorkbenchScreen;
import vulpisgiardina.simplekey.core.init.MenuInit;
import vulpisgiardina.simplekey.client.screen.KeyDoorScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Simplekey.MODID, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MenuInit.KEY_DOOR_MENU.get(), KeyDoorScreen::new);
        event.register(MenuInit.KEY_WORKBENCH_MENU.get(), KeyWorkbenchScreen::new);
    }
}
