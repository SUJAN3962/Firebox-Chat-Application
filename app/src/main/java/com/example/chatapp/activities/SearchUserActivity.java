package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.adapters.UsersAdapter;
import com.example.chatapp.databinding.ActivitySearchUserBinding;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.User;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SearchUserActivity extends AppCompatActivity implements UserListener {

    private ActivitySearchUserBinding binding;
    private PreferenceManager preferenceManager;
    public static ArrayList<User> searchList;
    private List<User> userList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.usersRecyclerView.setHasFixedSize(true);
        binding.usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        preferenceManager = new PreferenceManager((getApplicationContext()));
        getUsers();
        searchUsers();
    }

    private void searchUsers() {
        binding.searchQuery.setOnClickListener(v-> {
            binding.imageEnter.setVisibility(View.VISIBLE);
            binding.imageCancel.setVisibility(View.VISIBLE);
        });
        binding.imageEnter.setOnClickListener(v -> {
            filterList(binding.searchQuery.getText().toString().trim());
            binding.searchQuery.clearFocus();
        });
        binding.imageCancel.setOnClickListener(v -> {
            if (binding.searchQuery.getText().toString().isEmpty()) {
                startActivity(new Intent(getApplicationContext(), SearchUserActivity.class));
                finish();
            }
            else {
                binding.searchQuery.setText("");
            }
        });
    }

    private void getUsers() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if(currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                            userList.add(user);
                        }
                        Collections.sort(users, new Comparator<User>() {
                            @Override
                            public int compare(User user1, User user2) {
                                return user1.name.compareToIgnoreCase(user2.name);
                            }
                        });
                        if(users.size() > 0) {
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(usersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        }
                        else {
                            showNotFoundMessage();
                        }
                    }
                    else {
                        showNotFoundMessage();
                    }
                });
    }

    private void filterList(String s) {
        if (s.length() > 0) {
            searchList = new ArrayList<>();
            for(int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getName().toUpperCase().contains(s.toUpperCase())) {
                    User user = new User();
                    user.setName(userList.get(i).getName());
                    user.setEmail(userList.get(i).getEmail());
                    user.setImage(userList.get(i).getImage());
                    user.setToken(userList.get(i).getToken());
                    user.setId(userList.get(i).getId());
                    searchList.add(user);
                }
            }
            Collections.sort(searchList, new Comparator<User>() {
                @Override
                public int compare(User user1, User user2) {
                    return user1.name.compareToIgnoreCase(user2.name);
                }
            });
            if(searchList.size() > 0) {
                UsersAdapter usersAdapter = new UsersAdapter(searchList, this);
                binding.usersRecyclerView.setAdapter(usersAdapter);
                binding.usersRecyclerView.setVisibility(View.VISIBLE);
            }
            else
                showNotFoundMessage();
        }
    }

    private void showNotFoundMessage() {
        Toast.makeText(this, "No such user found!", Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        finish();
    }
}