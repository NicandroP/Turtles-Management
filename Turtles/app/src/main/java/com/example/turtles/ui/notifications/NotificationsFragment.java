package com.example.turtles.ui.notifications;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.turtles.MainActivity;
import com.example.turtles.R;
import com.example.turtles.StartActivity;
import com.example.turtles.databinding.FragmentNotificationsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class NotificationsFragment extends Fragment {
    private NotificationsViewModel notificationsViewModel;
    private FirebaseUser user;

    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);


        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        final TextView name =binding.userNameTextView;
        // Inizializza Firebase Authentication
        user=FirebaseAuth.getInstance().getCurrentUser();
        String displayName="Guest";
        if (user != null) {
            displayName = user.getEmail();
        }
        name.setText("Hello, " + displayName + "!");

        final Button logout =binding.logout;

        final Activity activity = getActivity();
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                if (activity != null) {
                    Toast.makeText(activity,"logged out", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(activity, StartActivity.class));
                    activity.finish(); // Optional: Close the current activity if needed
                }
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}