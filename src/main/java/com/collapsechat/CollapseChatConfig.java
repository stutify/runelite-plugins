package com.collapsechat;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import lombok.RequiredArgsConstructor;

@ConfigGroup(CollapseChatConfig.GROUP)
public interface CollapseChatConfig extends Config {
    String GROUP = "collapsechat";

    // 2. Enum for Logic (used for switch statements in Manager/Plugin)
    @RequiredArgsConstructor
    enum CollapsedButtonContent {
        STATIC_TEXT("Static text"),
        REPORT_BUTTON_TEXT("Report button text");

        private final String displayName;

        @Override
        public String toString() {
            return displayName;
        }
    }

    class Keys {
        public static final String CONTENT_TYPE = "collapsed-button-content";
        public static final String TRANSPARENT = "collapsed-button-transparent";
        public static final String STATIC_TEXT_SECTION = "static-text-section";
        public static final String CUSTOM_TEXT = "collapsed-button-content-text";
        public static final String CUSTOM_TEXT_HOVER = "collapsed-button-content-text-hovered";
    }

    @ConfigItem(
            keyName = Keys.CONTENT_TYPE,
            name = "Button content",
            description = "Content to display in the collapsed chat button",
            position = 1
    )
    default CollapsedButtonContent collapsedButtonContent() {
        return CollapsedButtonContent.STATIC_TEXT;
    }

    @ConfigItem(
            keyName = Keys.TRANSPARENT,
            name = "Transparent button",
            description = "Make collapsed button transparent",
            position = 2
    )
    default boolean collapsedButtonTransparent() {
        return false;
    }

    @ConfigSection(
            name = "Static text",
            description = "Customize the static text shown on the collapsed chat button",
            position = 3,
            closedByDefault = true
    )
    String staticTextConfigSection = Keys.STATIC_TEXT_SECTION;

    @ConfigItem(
            keyName = Keys.CUSTOM_TEXT,
            name = "Button text",
            description = "Configure custom text for the collapsed chat button",
            position = 4,
            section = Keys.STATIC_TEXT_SECTION
    )
    default String collapsedButtonContentCustomText() {
        return "-";
    }

    @ConfigItem(
            keyName = Keys.CUSTOM_TEXT_HOVER,
            name = "Button text on hover",
            description = "Configure custom text for the collapsed chat button on mouse hover",
            position = 5,
            section = Keys.STATIC_TEXT_SECTION
    )
    default String collapsedButtonContentCustomTextHovered() {
        return "+";
    }
}