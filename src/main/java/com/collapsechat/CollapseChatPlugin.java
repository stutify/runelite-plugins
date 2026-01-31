package com.collapsechat;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.ScriptID;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.VarClientIntChanged;
import net.runelite.api.gameval.VarClientID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;

import javax.inject.Inject;
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
    @Inject
    private ChatWidgetManager widgetManager;

    private final ChatState state = new ChatState();

    @Provides
    CollapseChatConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CollapseChatConfig.class);
    }

    @Inject
    private CollapseChatConfig config;

    @Override
    protected void startUp() {
        state.reset();
        clientThread.invokeLater(this::refreshAll);
    }

    @Override
    protected void shutDown() {
        state.reset();
        clientThread.invokeLater(this::refreshChatWidgets);
    }

    private void refreshAll() {
        widgetManager.setupMouseListeners(state, this::refreshChatWidgets);
        updateChatState();
        refreshChatWidgets();
    }

    private void refreshChatWidgets() {
        widgetManager.updateChatWidgets(state);
    }

    @Subscribe
    void onScriptPostFired(ScriptPostFired event) {
        if (event.getScriptId() == ScriptID.CHAT_PROMPT_INIT) {
            refreshAll();
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event) {
        if (!"Switch tab".equals(event.getMenuOption())) return;

        clientThread.invokeLater(() -> {
            updateChatState();
            refreshChatWidgets();
        });
    }

    @Subscribe
    void onVarClientIntChanged(VarClientIntChanged event) {
        if (event.getIndex() == VarClientID.CHAT_LASTREBUILD) {
            refreshAll();
        }
    }

    @Subscribe
    void onConfigChanged(ConfigChanged event) {
        if (CollapseChatConfig.GROUP.equals(event.getGroup())) {
            clientThread.invokeLater(this::refreshChatWidgets);
        }
    }

    @Subscribe
    public void onChatMessage(ChatMessage event) {
        if (state.isCollapsed()) {
            if (isSubscribedToChatMessageType(event.getType())) {
                state.hasUnseenMessages = true;
            }
        }
        clientThread.invokeLater(this::refreshChatWidgets);
    }

    @Schedule(period = 500, unit = ChronoUnit.MILLIS)
    public void reportTextPoller() {
        if (config.collapsedButtonContent() == CollapseChatConfig.CollapsedButtonContent.REPORT_BUTTON_TEXT) {
            clientThread.invokeLater(this::refreshChatWidgets);
        }
    }

    private boolean isUsingSplitPrivateChat() {
        return client.getVarpValue(VarPlayerID.OPTION_PM) == 1;
    }

    private boolean isPrivateChatHiddenWithChat() {
        return client.getVarbitValue(VarbitID.HIDE_PM_ALONGSIDE_CHATBOX) == 1;
    }

    private boolean arePrivateMessagesHiddenOnCollapse() {
        return !isUsingSplitPrivateChat() || isPrivateChatHiddenWithChat();
    }

    private boolean isSubscribedToChatMessageType(ChatMessageType messageType) {
        switch (messageType) {
            case PUBLICCHAT:
                return config.highlightOnUnreadPublicMessages();
            case PRIVATECHAT:
                return config.highlightOnUnreadPrivateMessages() && arePrivateMessagesHiddenOnCollapse();
            case CLAN_CHAT:
                return config.highlightOnUnreadClanChatMessages();
            case TRADE:
                return config.highlightOnUnreadTradeMessages();
            case FRIENDSCHAT:
                return config.highlightOnUnreadFriendsChatMessages();
            default:
                return false;
        }
    }

    private void updateChatState() {
        state.selectedChatButton = widgetManager.getSelectedChatButton();
        if (state.selectedChatButton == null) {
            state.collapseState = ChatCollapseState.COLLAPSED;
        } else {
            state.collapseState = ChatCollapseState.EXPANDED;
            state.hasUnseenMessages = false;
        }
    }
}
