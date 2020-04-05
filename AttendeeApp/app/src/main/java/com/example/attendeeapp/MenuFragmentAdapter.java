package com.example.attendeeapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Handles the viewpager slider to switch between global and stand menu views
 */
public class MenuFragmentAdapter extends FragmentStateAdapter {

    // Number of slider tabs available
    private static final int NUM_PAGES = 2;
    MenuFragment fragment1;
    MenuFragment fragment2;

    public MenuFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Creates global or stand fragment depending on the slider position
     * @param position: 0 = global fragment, 1 = stand fragment
     * @return the newly created fragment
     */
    @Override
    public MenuFragment createFragment(int position) {
        // Return a NEW fragment instance
        switch (position) {
            case 0:
                fragment1 = new MenuFragmentGlobal();
                return fragment1;
            case 1:
                fragment2 = new MenuFragmentStand();
                return fragment2;
            default:
                return null;
        }
    }

    /*public MenuFragment getFragment() {
        return fragment;
    }*/

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

}
