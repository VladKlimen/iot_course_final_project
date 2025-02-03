package com.example.iot_project;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iot_project.classes.Exercise;
import com.example.iot_project.classes.SpaceItemDecoration;
import com.example.iot_project.interfaces.ExercisesRecyclerViewInterface;
import com.example.iot_project.ui.exercises.ExercisesViewModel;

import java.util.List;

public class ExercisesActivity extends AppCompatActivity implements ExercisesRecyclerViewInterface {

    private ExercisesAdapter adapter;
    ExercisesViewModel exercisesViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        RecyclerView recyclerView = findViewById(R.id.recyclerview_exercises_activity);
        // initialise your ViewModel here
        exercisesViewModel = new ViewModelProvider(this).get(ExercisesViewModel.class);
        adapter = new ExercisesAdapter(exercisesViewModel.getExercises().getValue(), this);
        recyclerView.setAdapter(adapter);
        // Register an observer
        exercisesViewModel.getExercises().observe(this, exercises -> {
            adapter.setExercises(exercises);
        });

        // items decoration
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        recyclerView.addItemDecoration(new SpaceItemDecoration(spacingInPixels));

        // dynamic background
        AnimationDrawable animationDrawable = (AnimationDrawable) recyclerView.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();
    }

    @Override
    public void onItemClick(int position) {
        // Pass the selected icon ID back to the new training activity
        Intent intent = new Intent();
        intent.putExtra("exercise", adapter.getItem(position));
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private static class ExercisesAdapter extends RecyclerView.Adapter<ExercisesViewHolder> {
        private final ExercisesRecyclerViewInterface recyclerViewInterface;
        private List<Exercise> exercises;

        protected ExercisesAdapter(List<Exercise> exercises,
                                   ExercisesRecyclerViewInterface recyclerViewInterface) {
            this.exercises = exercises;
            this.recyclerViewInterface = recyclerViewInterface;
        }

        @NonNull
        @Override
        public ExercisesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exercises, parent, false);
            return new ExercisesViewHolder(itemView, recyclerViewInterface);
        }

        @Override
        public void onBindViewHolder(@NonNull ExercisesViewHolder holder, int position) {
            holder.nameView.setText(exercises.get(position).getName());
            holder.imageView.setImageDrawable(
                    ResourcesCompat.getDrawable(holder.imageView.getResources(),
                            exercises.get(position).getImage(),
                            null));
        }

        @Override
        public int getItemCount() {
            return (exercises != null ? exercises.size() : 0);
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setExercises(List<Exercise> exercises) {
            this.exercises = exercises;
            notifyDataSetChanged();
        }

        public Exercise getItem(int position) {
            return exercises.get(position);
        }
    }

    private static class ExercisesViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageView;
        private final TextView nameView;

        public ExercisesViewHolder(View itemView, ExercisesRecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view_item_exercises);
            nameView = itemView.findViewById(R.id.text_view_item_exercises);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recyclerViewInterface != null) {
                        int position = getBindingAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) {
                            recyclerViewInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
