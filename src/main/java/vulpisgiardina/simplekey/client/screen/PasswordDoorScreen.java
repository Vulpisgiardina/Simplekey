package vulpisgiardina.simplekey.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import vulpisgiardina.simplekey.Simplekey;
import vulpisgiardina.simplekey.menu.PasswordDoorMenu;
import vulpisgiardina.simplekey.network.packets.OpenDoorPacket;
import vulpisgiardina.simplekey.network.packets.UpdatePasswordDoorPacket;

public class PasswordDoorScreen extends AbstractContainerScreen<PasswordDoorMenu> {
    private final ResourceLocation TEXTURE = this.menu.isEditable ? ResourceLocation.fromNamespaceAndPath(Simplekey.MODID, "textures/gui/password_door_gui.png") : ResourceLocation.fromNamespaceAndPath(Simplekey.MODID, "textures/gui/password_door_input_gui.png");

    // 現在のパスワード
    private final String currentPassword = this.menu.initialPassword;
    private String inputPassword = "";
    private boolean isErrorDisplaying = false;
    private long errorStartTime = 0;

    // レイアウト用定数
    private static final int BUTTON_SIZE = 20;
    private static final int BUTTON_SPACING = 2;

    public PasswordDoorScreen(PasswordDoorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        // GUIのサイズを指定（テクスチャ画像のサイズに合わせる）
        this.imageWidth = 176;
        this.imageHeight = 166;
        // if (!this.menu.isEditable) { this.imageHeight = 256; }
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos;
        int y = this.topPos;


        // パスワード設定画面
        if (this.menu.isEditable) {
            // 数字ボタン
            int numpadX = x + 95;
            int numpadY = y + 10;
            addNumpad(numpadX, numpadY);

            // C (Clear) / CE (BackSpace) ボタン (テンキーの下に配置)
            this.addRenderableWidget(Button.builder(Component.literal("C"), b -> inputPassword = "")
                    .bounds(numpadX, numpadY + (BUTTON_SIZE + BUTTON_SPACING) * 3, BUTTON_SIZE, BUTTON_SIZE).build());
            this.addRenderableWidget(Button.builder(Component.literal("CE"), b -> removeLastChar())
                    .bounds(numpadX + 2 * (BUTTON_SIZE + BUTTON_SPACING), numpadY + (BUTTON_SIZE + BUTTON_SPACING) * 3, BUTTON_SIZE, BUTTON_SIZE).build());

            int actWidth = 60;

            // OK
            this.addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> {
                Simplekey.LOGGER.info("PasswordDoorScreen: OK Button Clicked.");
                ClientPacketDistributor.sendToServer(new UpdatePasswordDoorPacket(this.menu.blockPos, inputPassword.isEmpty() ? "0": inputPassword));
                this.onClose(); // GUIを閉じる
            }).bounds(x + (this.imageWidth / 2) - 10 - actWidth, y + 130, actWidth, 20).build());

            // キャンセルボタン
            this.addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> {
                this.onClose(); // 何もせずGUIを閉じる
            }).bounds(x + (this.imageWidth / 2) + 10, y + 130, actWidth, 20).build());
        }

        // パスワード入力画面
        else {
            // テンキーの配置 (中央下寄り)
            int numpadX = x + (this.imageWidth / 2) - ((BUTTON_SIZE * 3 + BUTTON_SPACING * 2) / 2) - 23;
            int numpadY = y + 55;
            addNumpad(numpadX, numpadY);

            // 右側の機能キー (C, CE, ENTER)
            int funcX = numpadX + (BUTTON_SIZE + BUTTON_SPACING) * 3 + 4;
            int funcWidth = 40;

            // C
            this.addRenderableWidget(Button.builder(Component.translatable("gui.simplekey.clear"), b -> {
                if(!isErrorDisplaying) inputPassword = "";
            }).bounds(funcX, numpadY, funcWidth, 20).build());

            // CE
            this.addRenderableWidget(Button.builder(Component.translatable("gui.simplekey.clearentry"), b -> {
                if(!isErrorDisplaying) removeLastChar();
            }).bounds(funcX, numpadY + 22, funcWidth, 20).build());

            // ENTER (縦長にするか、一番下に配置)
            this.addRenderableWidget(Button.builder(Component.translatable("gui.simplekey.enter"), b -> onEnterClicked())
                    .bounds(funcX, numpadY + 44, funcWidth, 42).build());
        }
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

        // 入力ボックス
        // パスワード設定画面
        if (this.menu.isEditable) {
            int boxWidth = 75;
            int boxX = x + 10;
            int boxY = y + 75;
            graphics.fill(boxX, boxY, boxX + boxWidth, boxY + 16, 0xFF000000);
            this.renderOutline(graphics, boxX - 1, boxY - 1, boxWidth + 2, 18, 0xFFFFFFFF);
        }
        // パスワード入力画面
        else {
            int boxWidth = 110;
            int boxX = x + (this.imageWidth - boxWidth) / 2;
            int boxY = y + 25;
            graphics.fill(boxX, boxY, boxX + boxWidth, boxY + 20, 0xFF000080);

            // 枠線
            this.renderOutline(graphics, boxX - 1, boxY - 1, boxWidth + 2, 22, 0xFFFFFFFF);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // GUIのタイトルを描画
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);

        // パスワード設定画面
        if (this.menu.isEditable) {
            // 現在のパスワード
            graphics.drawString(this.font, Component.translatable("gui.simplekey.current_password"), 10, 25, 0xFF404040, false);
            graphics.drawString(this.font, this.menu.initialPassword, 14, 39, 0xFF404040, false);

            // 「新しいパスワード」
            graphics.drawString(this.font, Component.translatable("gui.simplekey.new_password"), 10, 59, 0xFF404040, false);

            // 入力中の新しいパスワード
            graphics.drawString(this.font, this.inputPassword, 14, 79, 0xFFFFFFFF, false);
        }

        // パスワード入力画面
        else {
            // ミス後クールタイム経過チェック
            if (isErrorDisplaying) {
                if (System.currentTimeMillis() - errorStartTime > 1000) {
                    // クールタイムリセット
                    isErrorDisplaying = false;
                    inputPassword = ""; // 入力をクリア
                }
            }

            Component textToShow = isErrorDisplaying ? Component.translatable("gui.simplekey.password_door.wrong") : Component.literal(inputPassword);

            int boxWidth = 100;
            int textWidth = this.font.width(textToShow);
            int boxCenter = (this.imageWidth - boxWidth) / 2 + boxWidth / 2;

            graphics.drawString(this.font, textToShow, boxCenter - (textWidth / 2), 31, 0xFFFFFFFF, false);
        }
    }

    // 枠線描画メソッド
    private void renderOutline(GuiGraphics graphics, int x, int y, int width, int height, int color) {
        // 上の線
        graphics.fill(x, y, x + width, y + 1, color);
        // 下の線
        graphics.fill(x, y + height - 1, x + width, y + height, color);
        // 左の線
        graphics.fill(x, y + 1, x + 1, y + height - 1, color);
        // 右の線
        graphics.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    /**
     * テンキー(0-9)を生成して配置するヘルパーメソッド
     */
    private void addNumpad(int startX, int startY) {
        // 1-9
        for (int i = 1; i <= 9; i++) {
            int finalI = i;
            int col = (i - 1) % 3;
            int row = (i - 1) / 3; // 電話配列(上が123)にする場合
            // row = 2 - ((i - 1) / 3); // 電卓配列(上が789)にする場合

            this.addRenderableWidget(Button.builder(Component.literal(String.valueOf(i)), b -> onNumberButtonClicked(finalI))
                    .bounds(startX + col * (BUTTON_SIZE + BUTTON_SPACING),
                            startY + row * (BUTTON_SIZE + BUTTON_SPACING),
                            BUTTON_SIZE, BUTTON_SIZE).build());
        }
        // 0 (中央下)
        this.addRenderableWidget(Button.builder(Component.literal("0"), b -> onNumberButtonClicked(0))
                .bounds(startX + (BUTTON_SIZE + BUTTON_SPACING),
                        startY + 3 * (BUTTON_SIZE + BUTTON_SPACING),
                        BUTTON_SIZE, BUTTON_SIZE).build());
    }

    private void onNumberButtonClicked(int number) {
        if (isErrorDisplaying) return; // エラー表示中は入力を受け付けない
        if (inputPassword.length() < 10) { // 最大文字数制限
            inputPassword += number;
        }
    }

    private void removeLastChar() {
        if (!inputPassword.isEmpty()) {
            inputPassword = inputPassword.substring(0, inputPassword.length() - 1);
        }
    }

    private void onEnterClicked() {
        if (isErrorDisplaying) return;

        // 正しいパスワードを入力している
        if (currentPassword.equals(inputPassword)) {
            Simplekey.LOGGER.info("PasswordDoorScreen: Password is correct.");
            ClientPacketDistributor.sendToServer(new OpenDoorPacket(this.menu.blockPos));
            this.onClose();
        }
        // パスワードが間違っている
        else {
            isErrorDisplaying = true;
            errorStartTime = System.currentTimeMillis();
        }
    }

}
