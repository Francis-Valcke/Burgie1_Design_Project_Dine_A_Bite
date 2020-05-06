package com.example.standapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MenuItemFragment extends DialogFragment {

    private Context mContext;
    private Toolbar toolbar;
    private ChipGroup chipGroup;
    private OnMenuItemChangedListener mOnMenuItemChangedListener;

    private ArrayList<String> categories = new ArrayList<>(Arrays.asList(
            "American",
            "Italian",
            "Japanese",
            "Asian",
            "Mexican",
            "Belgian",
            "Burger",
            "Fries",
            "Pizza"
    ));

    @Override
    public void onAttach(@NonNull Context context) {
        // Called when a fragment is first attached to its context.
        super.onAttach(context);
        mContext = context;
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
        chipGroup = view.findViewById(R.id.chip_group_category);

        return view;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextInputEditText nameInput = view.findViewById(R.id.menu_item_name);
        final TextInputEditText priceInput = view.findViewById(R.id.menu_item_price);
        final TextInputEditText stockInput = view.findViewById(R.id.menu_item_stock);
        final TextInputEditText descriptionInput = view.findViewById(R.id.menu_item_description);
        final TextInputEditText prepTimeInput = view.findViewById(R.id.menu_item_prep_time);

        final View finalView = view;

        Bundle bundle = getArguments();
        CommonFood item = null;
        int position = -1;
        boolean isEditing = false;
        if (bundle != null && bundle.getSerializable("menu_item") != null) {
            item = (CommonFood) bundle.getSerializable("menu_item");
            position = bundle.getInt("menu_item_position");
            if (item != null) {
                isEditing = true;

                nameInput.setText(item.getName());
                priceInput.setText(item.getPrice().toString());
                stockInput.setText("0");
                descriptionInput.setText(item.getDescription());
                prepTimeInput.setText("" + item.getPreparationTime());

                if (!item.getCategory().isEmpty()) {
                    for (String category : categories) {
                        @SuppressLint("InflateParams")
                        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_category, null);
                        chip.setText(category);
                        if (item.getCategory().contains(category)) chip.setChecked(true);
                        chipGroup.addView(chip);
                    }
                    for (String category : item.getCategory()) {
                        if (!categories.contains(category)) {
                            @SuppressLint("InflateParams")
                            final Chip addedChip = (Chip) getLayoutInflater()
                                    .inflate(R.layout.chip_category, null);
                            addedChip.setText(category);
                            addedChip.setChecked(true);
                            addedChip.setCloseIconVisible(true);
                            addedChip.setOnCloseIconClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    chipGroup.removeView(addedChip);
                                }
                            });
                            chipGroup.addView(addedChip);
                        }
                    }
                } else {
                    for (String category : categories) {
                        @SuppressLint("InflateParams")
                        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_category, null);
                        chip.setText(category);
                        chipGroup.addView(chip);
                    }
                }

                // Editing preparation time is disabled,
                // because the backend will re-calculate this time
                prepTimeInput.setEnabled(false);
            }
        } else {
            for (String category : categories) {
                @SuppressLint("InflateParams")
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.chip_category, null);
                chip.setText(category);
                chipGroup.addView(chip);
            }
        }

        // Adding categories
        Chip addChip = view.findViewById(R.id.add_chip);
        addChip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ask for name of new category
                @SuppressLint("InflateParams")
                final View inputCategoryLayout = getLayoutInflater()
                        .inflate(R.layout.edit_name_dialog, null);
                final TextInputEditText editTextCategory
                        = inputCategoryLayout.findViewById(R.id.edit_text_name);
                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(mContext)
                        .setView(inputCategoryLayout)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Create and add new category ass chip to chip group
                                @SuppressLint("InflateParams")
                                final Chip addedChip = (Chip) getLayoutInflater()
                                        .inflate(R.layout.chip_category, null);
                                addedChip.setText(Objects.requireNonNull(editTextCategory.getText())
                                        .toString());
                                addedChip.setChecked(true);
                                addedChip.setCloseIconVisible(true);
                                addedChip.setOnCloseIconClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        chipGroup.removeView(addedChip);
                                    }
                                });
                                chipGroup.addView(addedChip);
                            }
                        }).setTitle("Add new food category");
                dialog.show();
            }
        });

        final CommonFood finalItem = item;
        final int finalPosition = position;
        final boolean finalIsEditing = isEditing;

        toolbar.setTitle("New menu item");
        toolbar.inflateMenu(R.menu.dialog_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // Beware: the MenuItem object as argument is NOT a CommonFood object
                switch(item.getItemId()) {
                    case R.id.action_save:
                        // Check if required field are filled in to be able to save the menu item
                        // (except for description field)
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
                            String name = Objects.requireNonNull(nameInput.getText()).toString();
                            BigDecimal price = new BigDecimal(Objects.requireNonNull(priceInput.getText()).toString());
                            int stock = Integer.parseInt(Objects.requireNonNull(stockInput.getText()).toString());
                            String description = Objects.requireNonNull(descriptionInput.getText()).toString();

                            // Get categories that are checked
                            ArrayList<String> categories = new ArrayList<>();
                            for (int id : chipGroup.getCheckedChipIds()) {
                                Chip chip = chipGroup.findViewById(id);
                                categories.add(chip.getText().toString());
                            }

                            CommonFood menuItem;
                            if (finalIsEditing) {
                                toolbar.setTitle("Edit menu item");

                                // Save the changed/edited menu item
                                menuItem = finalItem;
                                menuItem.setName(name);
                                menuItem.setPrice(price);
                                menuItem.increaseStock(stock); // addedStock
                                menuItem.setDescription(description);
                                menuItem.replaceCategoryList(categories);

                                // Send to container (parent) fragment
                                if (mOnMenuItemChangedListener != null) {
                                    mOnMenuItemChangedListener.onMenuItemChanged(menuItem, stock,
                                            finalPosition);
                                }
                            } else {
                                toolbar.setTitle("New menu item");

                                // Save the new menu item in CommonFood object
                                int preparationTime = Integer.parseInt(Objects.requireNonNull(prepTimeInput
                                        .getText()).toString()) * 60;
                                List<String> category = new ArrayList<>();
                                category.add("");
                                menuItem = new CommonFood(name, price, preparationTime, stock,
                                        "", description, category);

                                // Send to container (parent) fragment
                                if (mOnMenuItemChangedListener != null) {
                                    mOnMenuItemChangedListener.onMenuItemAdded(menuItem);
                                }
                            }

                            dismiss();
                            return true;
                        }

                        break;

                    case R.id.action_delete:
                        if (finalIsEditing) {

                            AlertDialog.Builder alertDialog
                                    = new AlertDialog.Builder(finalView.getContext())
                                    .setTitle("Delete menu item")
                                    .setMessage("Are you sure?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            // Send to container (parent) fragment
                                            if (mOnMenuItemChangedListener != null) {
                                                mOnMenuItemChangedListener.onMenuItemDeleted(finalPosition);
                                            }

                                            dismiss();

                                        }
                                    }).setNegativeButton("No", null);
                            alertDialog.show();
                            return true;

                        } else {
                            break;
                        }
                }
                // Not the correct actions happened
                // - delete a new / not already existing item
                // - required fields not filled in
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

    /**
     * Listener for adding or changing menu items from within MenuItemFragment (DialogFragment)
     * Container (parent) fragment must implement this interface
     *
     * https://stackoverflow.com/questions/23142956/sending-data-from-nested-fragments-to-parent-fragment
     */
    public interface OnMenuItemChangedListener {

        void onMenuItemAdded(CommonFood item);

        void onMenuItemChanged(CommonFood item, int addedStock, int position);

        void onMenuItemDeleted(int position);

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
                    fragment.toString() + " must implement OnMenuItemChangedListener");
        }
    }
}
