package vulpisgiardina.simplekey.client.screen;

import vulpisgiardina.simplekey.Simplekey;
import vulpisgiardina.simplekey.menu.KeyDoorMenu;
import vulpisgiardina.simplekey.network.packets.UpdateKeyDoorPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class KeyDoorScreen extends AbstractContainerScreen<KeyDoorMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Simplekey.MODID, "textures/gui/key_door_gui.png");

    // 現在のパスコード
    private int[] currentCode = new int[8];

    public KeyDoorScreen(KeyDoorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        // GUIのサイズを指定（テクスチャ画像のサイズに合わせる）
        this.imageWidth = 176;
        this.imageHeight = 166;

        int fullCode = this.menu.initialCode;

        // BlockEntity be = menu.levelAccess.evaluate((level, pos) -> level.getBlockEntity(pos)).orElse(null);
        String codeStr = String.format("%08d", fullCode);
        for (int i = 0; i < 8; i++) {
            this.currentCode[i] = Character.getNumericValue(codeStr.charAt(i));
        }
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos;
        int y = this.topPos;

        // ボタン配置
        for (int i = 0; i < 8; i++) {
            final int index = i;
            int digitX = x + 15 + (i * 18);

            // ▲　数字を増やすボタン
            this.addRenderableWidget(Button.builder(Component.literal("▲"), button -> {
                this.currentCode[index] = (this.currentCode[index] + 1) % 10;
            }).bounds(digitX, y + 30, 16, 10).build());
            // ▼ 数字を減らすボタン
            this.addRenderableWidget(Button.builder(Component.literal("▼"), button -> {
                this.currentCode[index] = (this.currentCode[index] - 1 + 10) % 10;
            }).bounds(digitX, y + 56, 16, 10).build());
        }

        // OK
        this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
            Simplekey.LOGGER.info("KeyDoorScreen: OK Button Clicked.");
            ClientPacketDistributor.sendToServer(new UpdateKeyDoorPacket(this.menu.blockPos, getCodeAsInt()));
            this.onClose(); // GUIを閉じる
        }).bounds(x + 36, y + 84, 40, 20).build());

        // キャンセルボタン
        this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> {
            this.onClose(); // 何もせずGUIを閉じる
        }).bounds(x + 92, y + 84, 50, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // まず背景を暗く描画する（ワールドが透けて見える部分）
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        // 親クラスのrenderを呼び出して、スロットなど基本的な要素を描画
        super.render(graphics, mouseX, mouseY, partialTick);
        // スロットにカーソルを合わせた時のツールチップを描画
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        // 背景テクスチャを描画
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 176, 166);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // GUIのタイトルを描画
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);

        // KEYCODEを描画
        for (int i = 0; i < 8; i++) {
            String digit = String.valueOf(this.currentCode[i]);
            int digitX = 15 + (i * 18) + (8 - this.font.width(digit) / 2);
            graphics.drawString(this.font, digit, digitX, 44, 0xFF404040, false);
        }

        // プレイヤーのインベントリ名を描画（今回は不要ならコメントアウトしてもOK）
        // graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0x404040, false);
    }

    private int getCodeAsInt() {
        StringBuilder sb = new StringBuilder();
        for (int digit : this.currentCode) {
            sb.append(digit);
        }
        return Integer.parseInt(sb.toString());
    }
}
