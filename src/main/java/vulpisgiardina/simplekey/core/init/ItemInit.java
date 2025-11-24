package vulpisgiardina.simplekey.core.init;

import vulpisgiardina.simplekey.Simplekey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public class ItemInit {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Simplekey.MODID);

    public static final DeferredItem<Item> KEY_ITEM = ITEMS.register(
            "key_item",
            registryName -> new Item(
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, registryName))
                            .component(DataComponentInit.KEYCODE.get(), 0))
    );

    public static final DeferredItem<Item> KEY_TOOL = ITEMS.registerSimpleItem(
            "key_tool",
            Item.Properties::new
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
