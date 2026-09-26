package com.example.alumniconnectingapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboard extends AppCompatActivity {

    private RecyclerView rvPendingAlumni;
    private TextView tvEmptyState;

    private FirebaseFirestore db;
    private PendingAlumniAdapter adapter;
    private List<PendingAlumni> alumniList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        rvPendingAlumni = findViewById(R.id.rvPendingAlumni);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        db = FirebaseFirestore.getInstance();
        alumniList = new ArrayList<>();

        adapter = new PendingAlumniAdapter(alumniList);
        rvPendingAlumni.setLayoutManager(new LinearLayoutManager(this));
        rvPendingAlumni.setAdapter(adapter);

        loadPendingAlumni();
    }

    // 🔹 Fetch only pending alumni
    private void loadPendingAlumni() {

        db.collection("users")
                .whereEqualTo("role", "alumni")
                .whereEqualTo("status", "pending")
                .get()
                .addOnSuccessListener(query -> {

                    alumniList.clear();

                    for (QueryDocumentSnapshot doc : query) {

                        alumniList.add(new PendingAlumni(
                                doc.getId(),
                                doc.getString("name"),
                                doc.getString("email"),
                                doc.getString("institute"),
                                doc.getString("passoutYear")
                        ));
                    }

                    adapter.notifyDataSetChanged();
                    tvEmptyState.setVisibility(
                            alumniList.isEmpty() ? View.VISIBLE : View.GONE
                    );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load alumni",
                                Toast.LENGTH_SHORT).show()
                );
    }

    // 🔹 Approve alumni
    private void approveAlumni(String uid) {
        db.collection("users")
                .document(uid)
                .update("status", "approved")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Alumni approved",
                            Toast.LENGTH_SHORT).show();
                    loadPendingAlumni();
                });
    }

    // 🔹 Reject alumni
    private void rejectAlumni(String uid) {
        db.collection("users")
                .document(uid)
                .update("status", "rejected")
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this,
                            "Alumni rejected",
                            Toast.LENGTH_SHORT).show();
                    loadPendingAlumni();
                });
    }

    static class PendingAlumni {
        String uid, name, email, institute, passoutYear;

        PendingAlumni(String uid, String name, String email,
                      String institute, String passoutYear) {
            this.uid = uid;
            this.name = name;
            this.email = email;
            this.institute = institute;
            this.passoutYear = passoutYear;
        }
    }

    class PendingAlumniAdapter
            extends RecyclerView.Adapter<PendingAlumniAdapter.ViewHolder> {

        private final List<PendingAlumni> list;

        PendingAlumniAdapter(List<PendingAlumni> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pending_alumni, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder h, int position) {
            PendingAlumni a = list.get(position);

            h.tvName.setText(a.name);
            h.tvEmail.setText(a.email);
            h.tvInstitute.setText(a.institute + " • " + a.passoutYear);

            h.btnApprove.setOnClickListener(v -> approveAlumni(a.uid));
            h.btnReject.setOnClickListener(v -> rejectAlumni(a.uid));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView tvName, tvEmail, tvInstitute;
            MaterialButton btnApprove, btnReject;

            ViewHolder(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvName);
                tvEmail = v.findViewById(R.id.tvEmail);
                tvInstitute = v.findViewById(R.id.tvInstitute);
                btnApprove = v.findViewById(R.id.btnApprove);
                btnReject = v.findViewById(R.id.btnReject);
            }
        }
    }
}
