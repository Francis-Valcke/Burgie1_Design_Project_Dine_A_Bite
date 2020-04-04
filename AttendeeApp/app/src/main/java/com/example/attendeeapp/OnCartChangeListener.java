package com.example.attendeeapp;

import com.example.attendeeapp.json.CommonFood;

/**
 * Interface to pass cart updates from menu item list adapter to the menu cart
 */
public interface OnCartChangeListener {
    public int onCartChangedAdd(CommonFood cartItem);

    public int onCartChangedRemove(CommonFood cartItem);
}
