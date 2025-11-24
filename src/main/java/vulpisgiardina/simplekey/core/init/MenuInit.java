package vulpisgiardina.simplekey.core.init;

import vulpisgiardina.simplekey.Simplekey;
import vulpisgiardina.simplekey.menu.KeyDoorMenu;
import vulpisgiardina.simplekey.menu.KeyWorkbenchMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MenuInit {
    public static final DeferredRegister<MenuType<?>> MENU = DeferredRegister.create(Registries.MENU, Simplekey.MODID);

    public static final Supplier<MenuType<KeyDoorMenu>> KEY_DOOR_MENU = MENU.register(
            "key_door",
            () -> IMenuTypeExtension.create(KeyDoorMenu::new)
    );

    public static final Supplier<MenuType<KeyWorkbenchMenu>> KEY_WORKBENCH_MENU = MENU.register(
            "key_workbench",
            () -> IMenuTypeExtension.create(KeyWorkbenchMenu::new));

    public static void register(IEventBus eventBus) {
        MENU.register(eventBus);
    }
}
