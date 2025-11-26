package vulpisgiardina.simplekey.core.init;

import vulpisgiardina.simplekey.Simplekey;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class CreativeTabInit {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Simplekey.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LOCK_AND_KEY_TAB = CREATIVE_MODE_TABS.register("lock_and_key_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ItemInit.KEY_ITEM.get())) // タブのアイコン
                    .title(Component.translatable("itemGroup.simplekey.lock_and_key")) // タブの名前
                    .displayItems((displayParameters, output) -> {
                        // このタブに表示するアイテムを追加
                        output.accept(ItemInit.KEY_ITEM.get());
                        output.accept(ItemInit.KEY_TOOL.get());
                        output.accept(BlockInit.KEY_WORKBENCH.get());
                        output.accept(BlockInit.KEY_DOOR.get());
                        output.accept(BlockInit.PASSWORD_DOOR.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
