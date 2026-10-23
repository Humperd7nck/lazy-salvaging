package com.lazysalvaging;

public enum StoredLootAction {
    KEEP("Keep"),
    DROP("Drop"),
    ALCH("Alch"),
    STORE("Store");
    private final String label;

    StoredLootAction(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
