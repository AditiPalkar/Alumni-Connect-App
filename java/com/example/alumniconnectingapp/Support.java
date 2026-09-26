//package com.example.alumniconnectingapp;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.database.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class Support extends Fragment {
//
//    // Header views
//    private TextView tvConnectionsCount, tvFollowersCount;
//
//    // RecyclerView
//    private RecyclerView rvAlumniSuggestions;
//    private AlumniAdapter alumniAdapter;
//    private final List<UserModel> alumniList = new ArrayList<>();
//
//    // Firebase
//    private DatabaseReference rootRef;
//    private String currentUserId;
//
//    public Support() {}
//
//    @Nullable
//    @Override
//    public View onCreateView(
//            @NonNull LayoutInflater inflater,
//            @Nullable ViewGroup container,
//            @Nullable Bundle savedInstanceState) {
//
//        View view = inflater.inflate(R.layout.fragment_support, container, false);
//
//        // Firebase
//        currentUserId = FirebaseAuth.getInstance().getUid();
//        rootRef = FirebaseDatabase.getInstance().getReference();
//
//        // Bind views
//        tvConnectionsCount = view.findViewById(R.id.tvConnectionsCount);
//        tvFollowersCount   = view.findViewById(R.id.tvFollowersCount);
//        rvAlumniSuggestions = view.findViewById(R.id.rvAlumniSuggestions);
//
//        // RecyclerView setup
//        rvAlumniSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));
//        alumniAdapter = new AlumniAdapter();
//        rvAlumniSuggestions.setAdapter(alumniAdapter);
//
//        // Load data
//        observeHeaderCounts();
//        loadApprovedAlumni();
//
//        return view;
//    }
//
//    private void loadApprovedAlumni() {
//
//        rootRef.child("users")
//                .addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                        alumniList.clear();
//
//                        for (DataSnapshot ds : snapshot.getChildren()) {
//
//                            String role = ds.child("role").getValue(String.class);
//                            Boolean approved = ds.child("approved").getValue(Boolean.class);
//
//                            if ("alumni".equals(role)
//                                    && Boolean.TRUE.equals(approved)
//                                    && !ds.getKey().equals(currentUserId)) {
//
//                                UserModel alumni = ds.getValue(UserModel.class);
//                                if (alumni != null) {
//                                    alumni.uid = ds.getKey();
//                                    alumniList.add(alumni);
//                                }
//                            }
//                        }
//                        alumniAdapter.notifyDataSetChanged();
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {}
//                });
//    }
//
//    private void observeHeaderCounts() {
//
//        rootRef.child("connections")
//                .child(currentUserId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        tvConnectionsCount.setText(
//                                String.valueOf(snapshot.getChildrenCount()));
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {}
//                });
//
//        rootRef.child("followers")
//                .child(currentUserId)
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        tvFollowersCount.setText(
//                                String.valueOf(snapshot.getChildrenCount()));
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {}
//                });
//    }
//
//    private class AlumniAdapter
//            extends RecyclerView.Adapter<AlumniAdapter.ViewHolder> {
//
//        @NonNull
//        @Override
//        public ViewHolder onCreateViewHolder(
//                @NonNull ViewGroup parent, int viewType) {
//
//            View view = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_alumni_connection_card, parent, false);
//
//            return new ViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull ViewHolder h, int position) {
//
//            UserModel alumni = alumniList.get(position);
//
//            h.tvName.setText(alumni.name);
//            h.tvRole.setText(alumni.designation);
//            h.tvLocation.setText(" " + alumni.company + " • " + alumni.city);
//            h.tvAvatar.setText(getInitials(alumni.name));
//
//            observeConnectionState(alumni.uid, h.btnConnect);
//
//            h.btnConnect.setOnClickListener(v ->
//                    sendConnectionRequest(alumni.uid));
//        }
//
//        @Override
//        public int getItemCount() {
//            return alumniList.size();
//        }
//
//        class ViewHolder extends RecyclerView.ViewHolder {
//
//            TextView tvAvatar, tvName, tvRole, tvLocation, btnConnect;
//
//            ViewHolder(@NonNull View itemView) {
//                super(itemView);
//                tvAvatar   = itemView.findViewById(R.id.tvAvatar);
//                tvName     = itemView.findViewById(R.id.tvName);
//                tvRole     = itemView.findViewById(R.id.tvRole);
//                tvLocation = itemView.findViewById(R.id.tvLocation);
//                btnConnect = itemView.findViewById(R.id.btnConnect);
//            }
//        }
//    }
//
//    private void observeConnectionState(String alumniId, TextView btn) {
//
//        rootRef.child("connectionRequests")
//                .child(alumniId)
//                .child(currentUserId)
//                .child("status")
//                .addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                        String status = snapshot.getValue(String.class);
//
//                        if ("accepted".equals(status)) {
//                            btn.setText("✓ Connected");
//                            btn.setEnabled(false);
//                        } else if ("pending".equals(status)) {
//                            btn.setText("Pending");
//                            btn.setEnabled(false);
//                        } else {
//                            btn.setText("+ Connect");
//                            btn.setEnabled(true);
//                        }
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {}
//                });
//    }
//
//    private void sendConnectionRequest(String alumniId) {
//
//        DatabaseReference reqRef = rootRef
//                .child("connectionRequests")
//                .child(alumniId)
//                .child(currentUserId);
//
//        reqRef.child("status").setValue("pending");
//        reqRef.child("timestamp").setValue(System.currentTimeMillis());
//
//        rootRef.child("followers")
//                .child(alumniId)
//                .child(currentUserId)
//                .setValue(true);
//    }
//
//
//    static class UserModel {
//        public String uid;
//        public String name;
//        public String designation;
//        public String company;
//        public String city;
//        public String role;
//        public boolean approved;
//
//        public UserModel() {}
//    }
//
//
//    private String getInitials(String name) {
//        if (name == null || name.isEmpty()) return "";
//        String[] parts = name.split(" ");
//        return parts.length >= 2
//                ? ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase()
//                : ("" + parts[0].charAt(0)).toUpperCase();
//    }
//}
package com.example.alumniconnectingapp;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;

import java.util.*;

public class Support extends Fragment {

    private RecyclerView rvAlumniSuggestions;
    private AlumniAdapter alumniAdapter;
    private List<UserModel> alumniList = new ArrayList<>();

    private FirebaseFirestore firestore;
    private DatabaseReference realtimeDb;

    private String currentUserId;

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_support, container, false);

        rvAlumniSuggestions = view.findViewById(R.id.rvAlumniSuggestions);
        rvAlumniSuggestions.setLayoutManager(new LinearLayoutManager(getContext()));

        firestore = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance().getReference();

        currentUserId = FirebaseAuth.getInstance().getUid();

        alumniAdapter = new AlumniAdapter();
        rvAlumniSuggestions.setAdapter(alumniAdapter);

        loadAlumni();

        return view;
    }

    private void loadAlumni() {

        firestore.collection("users")
                .whereEqualTo("role", "alumni")
                .whereEqualTo("status", "approved")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    alumniList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        if (!doc.getId().equals(currentUserId)) {

                            UserModel user = doc.toObject(UserModel.class);
                            user.uid = doc.getId();
                            alumniList.add(user);
                        }
                    }

                    alumniAdapter.notifyDataSetChanged();
                });
    }

    class AlumniAdapter extends RecyclerView.Adapter<AlumniAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_alumni_connection_card, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int position) {

            UserModel user = alumniList.get(position);

            h.tvName.setText(user.name);
            h.tvRole.setText(user.designation);
            h.tvLocation.setText(user.company + " • " + user.city);

            observeConnection(user.uid, h.btnConnect);

            h.btnConnect.setOnClickListener(v -> sendRequest(user.uid));
        }

        @Override
        public int getItemCount() {
            return alumniList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvName, tvRole, tvLocation, btnConnect;

            ViewHolder(View itemView) {
                super(itemView);

                tvName = itemView.findViewById(R.id.tvName);
                tvRole = itemView.findViewById(R.id.tvRole);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                btnConnect = itemView.findViewById(R.id.btnConnect);
            }
        }
    }

    private void sendRequest(String alumniId) {

        Map<String, Object> request = new HashMap<>();
        request.put("senderId", currentUserId);
        request.put("receiverId", alumniId);
        request.put("status", "pending");
        request.put("timestamp", System.currentTimeMillis());

        firestore.collection("connectionRequests").add(request);
    }

    private void observeConnection(String alumniId, TextView button) {

        realtimeDb.child("connections")
                .child(currentUserId)
                .child(alumniId)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            button.setText("✓ Connected");
                            button.setEnabled(false);
                        } else {
                            button.setText("+ Connect");
                            button.setEnabled(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    public static class UserModel {
        public String uid;
        public String name;
        public String designation;
        public String company;
        public String city;

        public UserModel() {}
    }
}