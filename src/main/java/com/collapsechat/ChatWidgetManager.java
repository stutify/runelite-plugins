package com.collapsechat;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Consumer;

@Slf4j
@Singleton
public class ChatWidgetManager {
    @Inject
    private Client client;

    @Inject
    private CollapseChatConfig config;

    private static final String ORIGINAL_ALL_BUTTON_TEXT = "All";

    public void updateChatWidgets(ChatState state) {
        toggleChatButtonVisibility(state);
        updateAllButtonGraphic(state);
        updateButtonContent(state);
    }

    private void toggleChatButtonVisibility(ChatState state) {
        for (int buttonID : ChatButton.CONTAINER_IDS_TO_TOGGLE) {
            runOnWidgetIfPresent(buttonID, (Widget w) -> w.setHidden(state.isCollapsed()));
        }
    }

    private void updateAllButtonGraphic(ChatState state) {
        runOnWidgetIfPresent(ChatButton.ALL.graphicID, (Widget w) -> {
            if (config.collapsedButtonTransparent()) {
                w.setOpacity(state.isCollapsed() ? 255 : 0);
                return;
            }

            if (state.isCollapsed()) {
                if (state.isMouseOverAllButton) {
                    w.setSpriteId(SpriteID.ChatTabButton.HOVERED);
                } else if (state.hasUnseenMessages) {
                    w.setSpriteId(SpriteID.ChatTabButton.NEW_MESSAGES);
                } else {
                    w.setSpriteId(SpriteID.ChatTabButton.BUTTON);
                }
            }
        });
    }

    public void setupMouseListeners(ChatState state, Runnable onStateChanged) {
        runOnWidgetIfPresent(ChatButton.ALL.containerID, (Widget allButton) -> {
            allButton.setOnMouseOverListener((JavaScriptCallback) ev -> {
                state.isMouseOverAllButton = true;
                onStateChanged.run();
            });
            allButton.setOnMouseLeaveListener((JavaScriptCallback) ev -> {
                state.isMouseOverAllButton = false;
                onStateChanged.run();
            });
            allButton.setHasListener(true);
        });
    }

    private void updateButtonContent(ChatState state) {
        if (state.collapseState != ChatCollapseState.COLLAPSED) {
            updateTextWidgetIfChanged(ChatButton.ALL.textID, ORIGINAL_ALL_BUTTON_TEXT);
            return;
        }

        String textToSet = ORIGINAL_ALL_BUTTON_TEXT;
        switch (config.collapsedButtonContent()) {
            case STATIC_TEXT:
                textToSet = state.isMouseOverAllButton
                        ? config.collapsedButtonContentCustomTextHovered()
                        : config.collapsedButtonContentCustomText();
                break;
            case REPORT_BUTTON_TEXT:
                Widget reportText = client.getWidget(ChatButton.REPORT.textID);
                if (reportText != null) {
                    textToSet = reportText.getText();
                }
                break;
        }
        updateTextWidgetIfChanged(ChatButton.ALL.textID, textToSet);
    }

    private void updateTextWidgetIfChanged(int textWidgetID, String newText) {
        runOnWidgetIfPresent(textWidgetID, (Widget w) -> {
            if (!w.getText().equals(newText)) {
                w.setText(newText);
            }
        });
    }

    public Integer getSelectedChatButton() {
        for (int buttonGraphicId : ChatButton.ALL_GRAPHIC_IDS) {
            Widget graphic = client.getWidget(buttonGraphicId);
            if (graphic == null || graphic.getSpriteId() == -1) {
                continue;
            }

            int sprite = graphic.getSpriteId();
            if (sprite == SpriteID.ChatTabButton.SELECTED || sprite == SpriteID.ChatTabButton.SELECTED_HOVERED) {
                return buttonGraphicId;
            }
        }
        return null;
    }

    private void runOnWidgetIfPresent(int widgetID, Consumer<Widget> action) {
        Widget w = client.getWidget(widgetID);
        if (w != null) {
            action.accept(w);
        }
    }
}