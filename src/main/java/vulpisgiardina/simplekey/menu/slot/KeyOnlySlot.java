package vulpisgiardina.simplekey.menu.slot;

import vulpisgiardina.simplekey.Simplekey;
import vulpisgiardina.simplekey.core.init.ItemInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.NotNull;

public class KeyOnlySlot extends ResourceHandlerSlot {
    // スロットが空のときに表示する、鍵のシルエット画像の場所を定義
    public static final ResourceLocation EMPTY_KEY_SLOT_ICON = ResourceLocation.fromNamespaceAndPath(Simplekey.MODID, "container/slot/key_item");

    public KeyOnlySlot(ItemStacksResourceHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, itemHandler::set, index, xPosition, yPosition);
    }

    /**
     * このスロットにアイテムを置けるかどうかを判定するメソッド
     * @param stack 置こうとしているアイテム
     * @return 鍵アイテムならtrue、それ以外はfalseを返す
     */
    @Override
    public boolean mayPlace(@NotNull ItemStack stack) {
        return stack.is(ItemInit.KEY_ITEM.get());
    }

    /**
     * スロットが空のときに表示する背景アイコンを返すメソッド
     * @return シルエット画像のResourceLocation
     */
    @Override
    public ResourceLocation getNoItemIcon() {
        return EMPTY_KEY_SLOT_ICON;
    }
}
