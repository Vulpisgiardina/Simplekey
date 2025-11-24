package vulpisgiardina.simplekey.client.screen;

import vulpisgiardina.simplekey.Simplekey;
import vulpisgiardina.simplekey.core.init.DataComponentInit;
import vulpisgiardina.simplekey.menu.KeyWorkbenchMenu;
import vulpisgiardina.simplekey.network.packets.UpdateKeyOnWorkbenchPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.NotNull;

public class KeyWorkbenchScreen extends AbstractContainerScreen<KeyWorkbenchMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Simplekey.MODID, "textures/gui/container/key_workbench_gui.png");
    private EditBox nameField;
    private ItemStack lastKeyStack = ItemStack.EMPTY;

    public KeyWorkbenchScreen(KeyWorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int x = this.leftPos;
        int y = this.topPos;

        this.nameField = new EditBox(this.font, x + 62, y + 24, 103, 12, Component.translatable("container.simplekey.key_edit"));
        this.nameField.setCanLoseFocus(false);
        this.nameField.setTextColor(-1);
        this.nameField.setTextColorUneditable(-1);
        this.nameField.setBordered(false);
        this.nameField.setMaxLength(50);
        this.nameField.setResponder(this::onNameChanged); // テキスト変更時の処理は空でOK
        this.addWidget(this.nameField);
        this.nameField.setEditable(false); // 最初は編集不可
        this.addRenderableWidget(this.nameField);

        // ダイヤルボタン
        for (int i = 0; i < 8; i++) {
            final int index = i;
            int digitX = x + 8 + (i * 18);
            this.addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
                int current = this.menu.data.get(index);
                this.menu.data.set(index, (current + 1) % 10);
            }).bounds(digitX, y + 44, 16, 10).build());
            this.addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
                int current = this.menu.data.get(index);
                this.menu.data.set(index, (current - 1 + 10) % 10);
            }).bounds(digitX, y + 67, 16, 10).build());
        }

        // OKボタン
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
            Simplekey.LOGGER.info("KeyWorkbenchScreen: OK Button Clicked.");
            ClientPacketDistributor.sendToServer(new UpdateKeyOnWorkbenchPacket(getCodeAsIntFromData(), this.nameField.getValue()));
        }).bounds(x + 160, y + 50, 40, 20).build());

        this.updateUIFromSlot(this.menu.getSlot(0).getItem());
    }

    // ウィンドウサイズが変わったときに呼ばれる
    /*
    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String s = this.nameField.getValue();
        this.init(minecraft, width, height);
        this.nameField.setValue(s);
    }

     */

    // キー入力のハンドリング (テキストボックスに必要)
    @Override
    public boolean keyPressed(@NotNull KeyEvent event) {
        if (this.nameField.keyPressed(event) || this.nameField.canConsumeInput()) {
            return true;
        }

        return super.keyPressed(event);
    }

    // スロットの状態が変わったときに呼ばれる
    @Override
    protected void containerTick() {
        super.containerTick();
        // 毎ティック、スロットに置かれたアイテムをチェックし、UIを更新する
        ItemStack currentStack = this.menu.getSlot(0).getItem();
        if (!ItemStack.matches(currentStack, this.lastKeyStack)) {
            this.lastKeyStack = currentStack.copy();
            this.updateUIFromSlot(currentStack);
        }
    }

    // UI更新用のヘルパーメソッド
    private void updateUIFromSlot(ItemStack stack) {
        if (stack.isEmpty()) {
            // アイテムが空の場合
            this.nameField.setValue("");
            this.nameField.setEditable(false);
            this.nameField.setCanLoseFocus(false);
            // ダイヤルを0にリセット
            for (int i = 0; i < 8; i++) {
                this.menu.data.set(i, 0);
            }
        } else {
            // アイテムがある場合
            // テキストボックスを更新
            this.nameField.setValue(stack.getHoverName().getString());
            this.nameField.setEditable(true);
            this.nameField.setCanLoseFocus(true);
            // this.setFocused(this.nameField);

            // ダイヤルを更新
            int fullCode = stack.getOrDefault(DataComponentInit.KEYCODE, 0);
            String codeStr = String.format("%08d", fullCode);
            for (int i = 0; i < 8; i++) {
                this.menu.data.set(i, Character.getNumericValue(codeStr.charAt(i)));
            }
        }
    }

    /*
    // テキストボックスを更新する処理をまとめたメソッド
    private void updateTextField() {
        Slot keySlot = this.menu.getSlot(0); // 鍵スロットを取得
        ItemStack currentStack = keySlot.getItem();

        // 前回の状態と現在の状態が違う場合のみ処理
        if (!ItemStack.matches(currentStack, this.lastKeyStack)) {
            this.nameField.setValue(currentStack.isEmpty() ? "" : currentStack.getHoverName().getString());
            this.nameField.setEditable(!currentStack.isEmpty());
            this.nameField.setCanLoseFocus(!currentStack.isEmpty());

            // フォーカスを設定
            if (!currentStack.isEmpty()) {
                this.setFocused(this.nameField);
            }

            // 前回の状態を更新
            this.lastKeyStack = currentStack.copy();
        }
    }

     */

    private void onNameChanged(String name) {
        // OKボタンでまとめて送るので、ここでは何もしなくて良い
    }

    private int getCodeAsIntFromData() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(this.menu.data.get(i));
        }
        return Integer.parseInt(sb.toString());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                this.menu.getSlot(0).hasItem() ? ResourceLocation.withDefaultNamespace("container/anvil/text_field") : ResourceLocation.withDefaultNamespace("container/anvil/text_field_disabled"),
                this.leftPos + 59,
                this.topPos + 20,
                110,
                16);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // GUIのタイトルとプレイヤーインベントリのタイトルを描画
        //super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
        // ダイヤルの数字を描画
        for (int i = 0; i < 8; i++) {
            String digit = String.valueOf(this.menu.data.get(i));
            int digitX = 8 + (i * 18) + (8 - this.font.width(digit) / 2);
            graphics.drawString(this.font, digit, digitX, 57, 0xFF404040, false);
        }
    }
}
