package com.collapsechat;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;


@ConfigGroup(CollapseChatConfig.group)
public interface CollapseChatConfig extends Config {
    String group = "collapsechat";

    enum CollapsedButtonContent {
        STATIC_TEXT("Static text"),
        REPORT_BUTTON_TEXT("Report button text");

        private final String displayName;

        CollapsedButtonContent(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    String collapsedButtonContentKey = "collapsed-button-content";

    @ConfigItem(
            keyName = collapsedButtonContentKey,
            name = "Button content",
            description = "Content to display in the collapsed chat button",
            position = 1
    )
    default CollapsedButtonContent collapsedButtonContent() {
        return CollapsedButtonContent.STATIC_TEXT;
    }

    String collapsedButtonTransparentKey = "collapsed-button-transparent";
    @ConfigItem(
            keyName = collapsedButtonTransparentKey,
            name = "Transparent button",
            description = "Make collapsed button transparent",
            position = 3
    )
    default boolean collapsedButtonTransparent() {
        return false;
    }

    @ConfigSection(
            name = "Static text",
            description = "Customize the static text shown on the collapsed chat button",
            position = 4,
            closedByDefault = true
    )
    String staticTextConfigSection = "static-text-section";
    @ConfigItem(
            keyName = "collapsed-button-content-text",
            name = "Button text",
            description = "Configure custom text for the collapsed chat button",
            position = 1,
            section = staticTextConfigSection
    )
    default String collapsedButtonContentCustomText() {
        return "-";
    }
    @ConfigItem(
            keyName = "collapsed-button-content-text-hovered",
            name = "Button text on hover",
            description = "Configure custom text for the collapsed chat button on mouse hover",
            position = 2,
            section = staticTextConfigSection
    )
    default String collapsedButtonContentCustomTextHovered() {
        return "+";
    }
}
