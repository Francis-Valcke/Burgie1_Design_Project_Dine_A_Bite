package com.example.attendeeapp;

/**
 * Interface to pass cart updates from menu item list adapter to the menu cart
 */
public interface OnCartChangeListener {
    public int onCartChangedAdd(MenuItem cartItem);

    public int onCartChangedRemove(MenuItem cartItem);
}
