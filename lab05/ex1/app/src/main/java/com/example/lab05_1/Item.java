package com.example.lab05_1;

public class Item {
    private String name;
    private int icon;
    private boolean checked;

    public Item(String name, int icon, boolean checked) {
        this.name = name;
        this.icon = icon;
        this.checked = checked;
    }

    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
