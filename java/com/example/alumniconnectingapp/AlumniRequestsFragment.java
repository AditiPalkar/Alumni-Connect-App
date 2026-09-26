package com.example.alumniconnectingapp;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.firestore.*;

import java.util.*;

public class AlumniRequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FirebaseFirestore firestore;
    private DatabaseReference realtimeDb;
    private String currentUserId;

    private List<DocumentSnapshot> requestList = new ArrayList<>();

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_alumni_requests, container, false);

        recyclerView = view.findViewById(R.id.recyclerRequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firestore = FirebaseFirestore.getInstance();
        realtimeDb = FirebaseDatabase.getInstance().getReference();
        currentUserId = FirebaseAuth.getInstance().getUid();

        loadRequests();

        return view;
    }

    private void loadRequests() {

        firestore.collection("connectionRequests")
                .whereEqualTo("receiverId", currentUserId)
                .whereEqualTo("status", "pending")
                .addSnapshotListener((value, error) -> {

                    requestList.clear();
                    if (value != null) {
                        requestList.addAll(value.getDocuments());
                    }

                    recyclerView.setAdapter(new RequestsAdapter());
                });
    }

    class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_connection_request, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int position) {

            DocumentSnapshot doc = requestList.get(position);
            String studentId = doc.getString("senderId");

            h.btnAccept.setOnClickListener(v -> acceptRequest(doc, studentId));
            h.btnReject.setOnClickListener(v -> doc.getReference().delete());
        }

        @Override
        public int getItemCount() {
            return requestList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView btnAccept, btnReject;

            ViewHolder(View itemView) {
                super(itemView);
                btnAccept = itemView.findViewById(R.id.btnAccept);
                btnReject = itemView.findViewById(R.id.btnReject);
            }
        }
    }

    private void acceptRequest(DocumentSnapshot doc, String studentId) {

        // Add connection both sides in Realtime DB
        realtimeDb.child("connections")
                .child(currentUserId)
                .child(studentId)
                .setValue(true);

        realtimeDb.child("connections")
                .child(studentId)
                .child(currentUserId)
                .setValue(true);

        doc.getReference().update("status", "accepted");
    }
}