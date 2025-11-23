package com.vulpisgiardina.simplekey.core.init;

import com.vulpisgiardina.simplekey.Simplekey;
import com.vulpisgiardina.simplekey.block.entity.KeyDoorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockEntityTypeInit {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Simplekey.MODID);

    public static final Supplier<BlockEntityType<KeyDoorBlockEntity>> KEY_DOOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "key_door",
            () -> new BlockEntityType<>(
                    KeyDoorBlockEntity::new,
                    BlockInit.KEY_DOOR.get()
            ));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITY_TYPES.register(eventBus);
    }
}
