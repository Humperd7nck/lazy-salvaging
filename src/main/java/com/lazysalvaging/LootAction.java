package com.lazysalvaging;

public enum LootAction {
    KEEP("Keep"),
    DROP("Drop"),
    ALCH("Alch");
    private final String label;

    LootAction(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
