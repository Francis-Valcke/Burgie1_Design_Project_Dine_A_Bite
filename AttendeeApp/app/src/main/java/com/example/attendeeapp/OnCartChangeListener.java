package com.example.attendeeapp;

/**
 * Interface to pass cart updates from menu item list to the cart
 */
public interface OnCartChangeListener {
    public int onCartChangedAdd(MenuItem cartItem);

    public int onCartChangedRemove(MenuItem cartItem);
}
