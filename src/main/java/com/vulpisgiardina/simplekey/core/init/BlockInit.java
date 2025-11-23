package com.vulpisgiardina.simplekey.core.init;

import com.mojang.logging.LogUtils;
import com.vulpisgiardina.simplekey.Simplekey;
import com.vulpisgiardina.simplekey.block.KeyDoorBlock;
import com.vulpisgiardina.simplekey.block.KeyWorkbenchBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

import java.util.function.Supplier;
import java.util.function.Function;

public class BlockInit {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Simplekey.MODID);

    // 鍵用作業台の登録
    public static final DeferredBlock<KeyWorkbenchBlock> KEY_WORKBENCH = BLOCKS.register(
            "key_workbench",
            registryName -> new KeyWorkbenchBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
            ));
    public static final DeferredItem<BlockItem> KEY_WORKBENCH_ITEM = ItemInit.ITEMS.registerSimpleBlockItem("key_workbench", KEY_WORKBENCH);

    // 鍵付きドアの登録
    public static final DeferredBlock<KeyDoorBlock> KEY_DOOR = BLOCKS.register(
            "key_door",
            registryName -> new KeyDoorBlock(BlockSetType.IRON, BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, registryName))
                    .mapColor(MapColor.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(5.0F)
                    .sound(SoundType.METAL)
                    .noOcclusion()
            ));
    public static final DeferredItem<BlockItem> KEY_DOOR_ITEM = ItemInit.ITEMS.register(
            "key_door",
            registryName -> new BlockItem(
                    KEY_DOOR.get(),
                    new Item.Properties()
                            .setId(ResourceKey.create(Registries.ITEM, registryName))
                            .component(DataComponentInit.KEYCODE.get(), 0)
                            .useBlockDescriptionPrefix()
            ));


    // ブロックを登録し、対応するアイテムも自動で登録するヘルパーメソッド
    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<ResourceLocation, ? extends T> func) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, func);
        ItemInit.ITEMS.registerSimpleBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Function<ResourceLocation, ? extends T> func, Item.Properties properties) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, func);
        ItemInit.ITEMS.register(name, () -> new BlockItem(toReturn.get(), properties));
        return toReturn;
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
