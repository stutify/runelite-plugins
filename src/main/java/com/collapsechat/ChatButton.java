package com.collapsechat;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.runelite.api.gameval.InterfaceID;

import java.util.Arrays;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public enum ChatButton {
    ALL(InterfaceID.Chatbox.CHAT_ALL, InterfaceID.Chatbox.CHAT_ALL_GRAPHIC, InterfaceID.Chatbox.CHAT_ALL_TEXT1, false),
    GAME(InterfaceID.Chatbox.CHAT_GAME, InterfaceID.Chatbox.CHAT_GAME_GRAPHIC, InterfaceID.Chatbox.CHAT_GAME_TEXT1, true),
    PUBLIC(InterfaceID.Chatbox.CHAT_PUBLIC, InterfaceID.Chatbox.CHAT_PUBLIC_GRAPHIC, InterfaceID.Chatbox.CHAT_PUBLIC_TEXT1, true),
    PRIVATE(InterfaceID.Chatbox.CHAT_PRIVATE, InterfaceID.Chatbox.CHAT_PRIVATE_GRAPHIC, InterfaceID.Chatbox.CHAT_PRIVATE_TEXT1, true),
    FRIENDS(InterfaceID.Chatbox.CHAT_FRIENDSCHAT, InterfaceID.Chatbox.CHAT_FRIENDSCHAT_GRAPHIC, InterfaceID.Chatbox.CHAT_FRIENDSCHAT_TEXT1, true),
    CLAN(InterfaceID.Chatbox.CHAT_CLAN, InterfaceID.Chatbox.CHAT_CLAN_GRAPHIC, InterfaceID.Chatbox.CHAT_CLAN_TEXT1, true),
    TRADE(InterfaceID.Chatbox.CHAT_TRADE, InterfaceID.Chatbox.CHAT_TRADE_GRAPHIC, InterfaceID.Chatbox.CHAT_TRADE_TEXT, true),
    REPORT(InterfaceID.Chatbox.REPORTABUSE, InterfaceID.Chatbox.REPORTABUSE_GRAPHIC, InterfaceID.Chatbox.REPORTABUSE_TEXT1, true);

    public final int containerID;
    public final int graphicID;
    public final int textID;
    public final boolean shouldToggle;

    public static final int[] CONTAINER_IDS_TO_TOGGLE;
    public static final int[] ALL_GRAPHIC_IDS;

    static {
        CONTAINER_IDS_TO_TOGGLE = Arrays.stream(values())
                .filter(button -> button.shouldToggle)
                .mapToInt(button -> button.containerID)
                .toArray();

        ALL_GRAPHIC_IDS = Arrays.stream(values())
                .mapToInt(button -> button.graphicID)
                .toArray();
    }
}