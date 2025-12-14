package com.collapsechat;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;

import java.time.temporal.ChronoUnit;

@Slf4j
@PluginDescriptor(
        name = "Collapse Chat",
        description = "Collapse chat to a single button when minimized",
        tags = {"chat", "collapse", "ui"}
)
public class CollapseChatPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Provides
    CollapseChatConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CollapseChatConfig.class);
    }

    @Inject
    private CollapseChatConfig config;

    private ChatCollapseState collapseState = ChatCollapseState.UNKNOWN;

    private boolean isMouseOverAllButton = false;

    private static final String ORIGINAL_ALL_BUTTON_TEXT = "All";

    private final int[] CHAT_BUTTONS_TO_TOGGLE = {
            InterfaceID.Chatbox.CHAT_GAME,
            InterfaceID.Chatbox.CHAT_PUBLIC,
            InterfaceID.Chatbox.CHAT_PRIVATE,
            InterfaceID.Chatbox.CHAT_FRIENDSCHAT,
            InterfaceID.Chatbox.CHAT_CLAN,
            InterfaceID.Chatbox.CHAT_TRADE,
            InterfaceID.Chatbox.REPORTABUSE
    };

    private final int[] CHAT_BUTTON_GRAPHIC_IDS = {
            InterfaceID.Chatbox.CHAT_ALL_GRAPHIC,
            InterfaceID.Chatbox.CHAT_GAME_GRAPHIC,
            InterfaceID.Chatbox.CHAT_PUBLIC_GRAPHIC,
            InterfaceID.Chatbox.CHAT_PRIVATE_GRAPHIC,
            InterfaceID.Chatbox.CHAT_FRIENDSCHAT_GRAPHIC,
            InterfaceID.Chatbox.CHAT_CLAN_GRAPHIC,
            InterfaceID.Chatbox.CHAT_TRADE_GRAPHIC,
    };

    @Override
    protected void shutDown() {
        clientThread.invokeLater(this::restoreChat);
        collapseState = ChatCollapseState.UNKNOWN;
    }

    @Override
    protected void startUp() {
        registerAllButtonListeners();
        clientThread.invokeLater(() -> {
            refreshCollapseState();
            updateButtonContent();
        });
    }

    @Schedule(
            period = 500,
            unit = ChronoUnit.MILLIS
    )
    public void every500Millis() {
        if (config.collapsedButtonContent() == CollapseChatConfig.CollapsedButtonContent.REPORT_BUTTON_TEXT) {
            updateButtonReportContent();
        }
    }

    @Subscribe
    void onScriptPostFired(ScriptPostFired event) {
        int scriptID = event.getScriptId();
        if (scriptID == ScriptID.CHAT_PROMPT_INIT) {
            registerAllButtonListeners();
            refreshCollapseState();
            updateButtonContent();
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (event.getMenuOption().equals("Switch tab")) {
            // We use clientThread because the widget state (sprite IDs)
            // updates immediately after the click is processed.
            clientThread.invokeLater(() -> {
                refreshCollapseState();
                updateButtonContent();
            });
        }
    }

    @Subscribe
    void onVarClientIntChanged(VarClientIntChanged event) {
        if (event.getIndex() == VarClientID.CHAT_LASTREBUILD) {
            // chat rebuild re-renders the report abuse button,
            // so we need to re-hide the buttons
            registerAllButtonListeners();
            refreshCollapseState();
            updateButtonContent();
        }
    }

    @Subscribe
    void onConfigChanged(ConfigChanged event) {
        if (event.getGroup().equals(CollapseChatConfig.group)) {
            clientThread.invokeLater(() -> {
                restoreChat();
                refreshCollapseState();
                updateButtonContent();
            });
        }
    }

    private void registerAllButtonListeners() {
        Widget allButton = client.getWidget(InterfaceID.Chatbox.CHAT_ALL_GRAPHIC);
        if (allButton == null) return;

        allButton.setHasListener(true);
        allButton.setOnMouseOverListener((JavaScriptCallback) ev -> {
            isMouseOverAllButton = true;
            updateButtonContent();
        });
        allButton.setOnMouseLeaveListener((JavaScriptCallback) ev -> {
            isMouseOverAllButton = false;
            updateButtonContent();
        });
    }

    private void updateButtonContent() {
        if (collapseState != ChatCollapseState.COLLAPSED) {
            updateAllTextIfChanged(ORIGINAL_ALL_BUTTON_TEXT);
            return;
        }

        String textToSet = ORIGINAL_ALL_BUTTON_TEXT;

        switch (config.collapsedButtonContent()) {
            case STATIC_TEXT:
                textToSet = isMouseOverAllButton
                        ? config.collapsedButtonContentCustomTextHovered()
                        : config.collapsedButtonContentCustomText();
                break;
            case REPORT_BUTTON_TEXT:
                Widget reportText = client.getWidget(InterfaceID.Chatbox.REPORTABUSE_TEXT1);
                if (reportText != null) {
                    textToSet = reportText.getText();
                }
                break;
        }

        updateAllTextIfChanged(textToSet);
    }

    private void updateButtonReportContent() {
        if (config.collapsedButtonContent() != CollapseChatConfig.CollapsedButtonContent.REPORT_BUTTON_TEXT) return;
        if (collapseState != ChatCollapseState.COLLAPSED) {
            updateAllTextIfChanged(ORIGINAL_ALL_BUTTON_TEXT);
            return;
        }

        Widget reportText = client.getWidget(InterfaceID.Chatbox.REPORTABUSE_TEXT1);
        if (reportText == null) return;
        updateAllTextIfChanged(reportText.getText());
    }

    private void updateAllTextIfChanged(String newText) {
        Widget allText = client.getWidget(InterfaceID.Chatbox.CHAT_ALL_TEXT1);
        if (allText == null) return;
        if (!allText.getText().equals(newText)) {
            allText.setText(newText);
        }
    }

    private void refreshCollapseState() {
        Integer selectedChatButton = getSelectedChatButton();
        collapseState = selectedChatButton == null ? ChatCollapseState.COLLAPSED : ChatCollapseState.EXPANDED;
        boolean hidden = collapseState == ChatCollapseState.COLLAPSED;
        for (int buttonID : CHAT_BUTTONS_TO_TOGGLE) {
            setWidgetHiddenIfPresent(buttonID, hidden);
        }
        updateAllButtonTransparency(collapseState);
    }

    private Integer getSelectedChatButton() {
        for (int buttonGraphicId : CHAT_BUTTON_GRAPHIC_IDS) {
            final Widget graphic = client.getWidget(buttonGraphicId);
            if (graphic == null) {
                continue;
            }

            final int sprite = graphic.getSpriteId();
            if (sprite == -1) {
                continue;
            }

            final boolean selected = sprite == SpriteID.ChatTabButton.SELECTED
                    || sprite == SpriteID.ChatTabButton.SELECTED_HOVERED;

            if (selected) {
                return buttonGraphicId;
            }
        }
        return null;
    }

    private void restoreChat() {
        updateAllTextIfChanged(ORIGINAL_ALL_BUTTON_TEXT);
        for (int buttonID : CHAT_BUTTONS_TO_TOGGLE) {
            setWidgetHiddenIfPresent(buttonID, false);
        }
        updateAllButtonTransparency(ChatCollapseState.EXPANDED);
    }

    private void updateAllButtonTransparency(ChatCollapseState collapseState) {
        boolean collapsed = collapseState == ChatCollapseState.COLLAPSED;
        Widget allButtonGraphic = client.getWidget(InterfaceID.Chatbox.CHAT_ALL_GRAPHIC);
        if (allButtonGraphic == null) return;
        boolean shouldBeTransparent = collapsed && config.collapsedButtonTransparent();
        allButtonGraphic.setOpacity(shouldBeTransparent ? 255 : 0);
    }

    private void setWidgetHiddenIfPresent(int widgetID, boolean hidden) {
        Widget w = client.getWidget(widgetID);
        if (w == null) return;
        w.setHidden(hidden);
    }
}
