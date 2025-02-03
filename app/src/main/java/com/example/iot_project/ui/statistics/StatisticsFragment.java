package com.example.iot_project.ui.statistics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.iot_project.MainActivity;
import com.example.iot_project.R;
import com.example.iot_project.databinding.FragmentStatisticsBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

public class StatisticsFragment extends Fragment {

private FragmentStatisticsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        StatisticsViewModel slideshowViewModel =
                new ViewModelProvider(this).get(StatisticsViewModel.class);

    binding = FragmentStatisticsBinding.inflate(inflater, container, false);
    View root = binding.getRoot();


        // set new_item button
        FloatingActionButton newItemButton = requireActivity().findViewById(R.id.new_item_button);
        newItemButton.setVisibility(View.INVISIBLE);

        return root;
    }

@Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}