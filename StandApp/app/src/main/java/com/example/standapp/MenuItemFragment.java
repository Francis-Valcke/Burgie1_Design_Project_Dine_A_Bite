package com.example.standapp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

public class MenuItemFragment extends Fragment {

    @Override
    public void onAttach(@NonNull Context context) {
        // Called when a fragment is first attached to its context.
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.add_menu_item_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextInputEditText nameInput = view.findViewById(R.id.menu_item_name);
        TextInputEditText priceInput = view.findViewById(R.id.menu_item_price);
        TextInputEditText stockInput = view.findViewById(R.id.menu_item_stock);
        TextInputEditText descriptionInput = view.findViewById(R.id.menu_item_description);
        TextInputEditText prepTimeInput = view.findViewById(R.id.menu_item_prep_time);
    }
}
