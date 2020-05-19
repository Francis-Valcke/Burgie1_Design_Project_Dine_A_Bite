package com.example.attendeeapp;

import com.example.attendeeapp.json.CommonFood;

/**
 * Interface to pass cart updates from the menuItem list adapter from fragments to the menu cart in MenuActivity.
 */
public interface OnCartChangeListener {

    int onCartChangedAdd(CommonFood cartItem);

    int onCartChangedRemove(CommonFood cartItem);

}
