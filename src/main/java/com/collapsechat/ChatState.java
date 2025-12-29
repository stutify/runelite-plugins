package com.collapsechat;

public class ChatState {
    public ChatCollapseState collapseState = ChatCollapseState.UNKNOWN;
    public boolean isMouseOverAllButton = false;

    public void reset() {
        this.collapseState = ChatCollapseState.UNKNOWN;
        this.isMouseOverAllButton = false;
    }

    public boolean isCollapsed() {
        return collapseState == ChatCollapseState.COLLAPSED;
    }
}