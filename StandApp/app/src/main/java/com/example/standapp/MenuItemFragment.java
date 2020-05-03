package com.example.standapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.example.standapp.json.CommonFood;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MenuItemFragment extends DialogFragment {

    private Toolbar toolbar;
    private OnMenuItemChangedListener mOnMenuItemChangedListener;

    @Override
    public void onAttach(@NonNull Context context) {
        // Called when a fragment is first attached to its context.
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);

        onAttachToParentFragment(getParentFragment());
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.menu_item_dialog, container, false);
        toolbar = view.findViewById(R.id.toolbar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextInputEditText nameInput = view.findViewById(R.id.menu_item_name);
        final TextInputEditText priceInput = view.findViewById(R.id.menu_item_price);
        final TextInputEditText stockInput = view.findViewById(R.id.menu_item_stock);
        final TextInputEditText descriptionInput = view.findViewById(R.id.menu_item_description);
        final TextInputEditText prepTimeInput = view.findViewById(R.id.menu_item_prep_time);

        final View finalView = view;

        toolbar.setTitle("New menu item");
        toolbar.inflateMenu(R.menu.dialog_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Check if required field are filled in to be able to save menu item
                if (Objects.requireNonNull(nameInput.getText()).toString().isEmpty()
                        || Objects.requireNonNull(priceInput.getText()).toString().isEmpty()
                        || Objects.requireNonNull(stockInput.getText()).toString().isEmpty()
                        || Objects.requireNonNull(prepTimeInput.getText()).toString().isEmpty()) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(finalView.getContext())
                            .setTitle("Invalid menu item")
                            .setMessage("The menu item you tried to add is invalid, " +
                                    "please try again. " +
                                    "You should fill in the necessary fields.")
                            .setNeutralButton("Ok", null);
                    alertDialog.show();
                } else {
                    // Save the new/changed menu item in CommonFood object
                    // And send to container (parent) fragment
                    String name = Objects.requireNonNull(nameInput.getText()).toString();
                    BigDecimal price = new BigDecimal(Objects.requireNonNull(priceInput.getText()).toString());
                    int stock = Integer.parseInt(Objects.requireNonNull(stockInput.getText()).toString());
                    String description = Objects.requireNonNull(descriptionInput.getText()).toString();
                    int preparationTime = Integer.parseInt(Objects.requireNonNull(prepTimeInput.getText())
                            .toString()) * 60;
                    List<String> category = new ArrayList<>();
                    category.add("");
                    CommonFood menuItem = new CommonFood(name, price, preparationTime, stock, "",
                            description, category);

                    if (mOnMenuItemChangedListener != null) {
                        mOnMenuItemChangedListener.onMenuItemChanged(menuItem);
                    }

                    dismiss();
                    return true;
                }
                return false;
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    /**
     * Listener for adding or changing menu items from within MenuItemFragment (DialogFragment)
     *
     * Container (parent) fragment must implement this interface
     *
     * https://stackoverflow.com/questions/23142956/sending-data-from-nested-fragments-to-parent-fragment
     */
    public interface OnMenuItemChangedListener {
        void onMenuItemChanged(CommonFood item);
    }

    /**
     * Attach the interface to parent fragment
     *
     * @param fragment: Container (parent) fragment
     */
    private void onAttachToParentFragment(Fragment fragment) {
        try {
            mOnMenuItemChangedListener = (OnMenuItemChangedListener) fragment;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(
                    fragment.toString() + " must implement OnPlayerSelectionSetListener");
        }
    }
}
