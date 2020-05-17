package com.example.attendeeapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.jetbrains.annotations.NotNull;

/**
 * Handles the ViewPager slider to switch between global, stand or category menu views
 */
public class MenuFragmentAdapter extends FragmentStateAdapter {

    // Number of slider tabs available
    private static final int NUM_PAGES = 3;

    MenuFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Creates global, stand or category fragment depending on the slider position.
     *
     * @param position 0 = global fragment, 1 = stand fragment, 2 = category fragment.
     * @return The newly created fragment.
     */
    @NotNull
    @Override
    public MenuFragment createFragment(int position) {
        // Return a NEW fragment instance
        switch (position) {
            case 0:
                return new MenuFragmentGlobal();
            case 1:
                return new MenuFragmentStand();
            case 2:
                return new MenuFragmentCategory();
        }
        return new MenuFragmentGlobal();
    }

    /*public MenuFragment getFragment() {
        return fragment;
    }*/

    @Override
    public int getItemCount() {
        return NUM_PAGES;
    }

}
