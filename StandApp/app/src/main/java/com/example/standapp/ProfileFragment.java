package com.example.standapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    private MaterialAlertDialogBuilder dialogStandName;
    private MaterialAlertDialogBuilder dialogBrandName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_account, container, false);
        final TextView standName = view.findViewById(R.id.stand_name);
        final TextView brandName = view.findViewById(R.id.brand_name);
        Button editStandNameButton = view.findViewById(R.id.edit_stand_name_button);
        Button editBrandNameButton = view.findViewById(R.id.edit_brand_name_button);

        final Bundle bundle = this.getArguments();

        // dialog for editing stand name
        final View inputStandNameLayout = inflater.inflate(R.layout.edit_name_dialog, null, false);
        final TextInputEditText editTextStandName = inputStandNameLayout.findViewById(R.id.edit_text_name);
        dialogStandName = new MaterialAlertDialogBuilder(Objects.requireNonNull(this.getContext()))
                .setView(inputStandNameLayout)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        standName.setText(editTextStandName.getText());
                        if (bundle != null) bundle.putString("standName", Objects.requireNonNull(editTextStandName.getText()).toString());
                        ViewGroup parent = (ViewGroup) inputStandNameLayout.getParent();
                        parent.removeView(inputStandNameLayout);
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        ViewGroup parent = (ViewGroup) inputStandNameLayout.getParent();
                        if (parent != null) parent.removeView(inputStandNameLayout);
                    }
                });

        editStandNameButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogStandName.show();
            }

        });

        // dialog for editing brand name
        final View inputBrandNameLayout = inflater.inflate(R.layout.edit_name_dialog, null, false);
        final TextInputEditText editTextBrandName = inputBrandNameLayout.findViewById(R.id.edit_text_name);
        dialogBrandName = new MaterialAlertDialogBuilder(Objects.requireNonNull(this.getContext()))
                .setView(inputBrandNameLayout)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        brandName.setText(editTextBrandName.getText());
                        if (bundle != null) bundle.putString("brandName", Objects.requireNonNull(editTextBrandName.getText()).toString());
                        ViewGroup parent = (ViewGroup) inputBrandNameLayout.getParent();
                        parent.removeView(inputBrandNameLayout);
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        ViewGroup parent = (ViewGroup) inputBrandNameLayout.getParent();
                        if (parent != null) parent.removeView(inputBrandNameLayout);
                    }
                });

        editBrandNameButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dialogBrandName.show();
            }

        });

        return view;
    }
}
