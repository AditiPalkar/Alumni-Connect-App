package com.example.alumniconnectingapp;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class JobsAlumni extends Fragment {

    // API Credentials
    private static final String APP_ID = "";
    private static final String APP_KEY = "";

    // UI Components
    private LinearLayout jobsContainer;
    private TextView chipAll, chipInternship, chipFullTime, chipPartTime, chipRemote;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_jobs, container, false);

        // 1. Initialize Main Container
        jobsContainer = view.findViewById(R.id.jobsContainer);

        // 2. Initialize Chips
        chipAll = view.findViewById(R.id.chipAll);
        chipInternship = view.findViewById(R.id.chipInternship);
        chipFullTime = view.findViewById(R.id.chipFullTime);
        chipPartTime = view.findViewById(R.id.chipPartTime);
        chipRemote = view.findViewById(R.id.chipRemote);

        // 3. Set Click Listeners
        setupChipListener(chipAll, "");
        setupChipListener(chipInternship, "internship");
        setupChipListener(chipFullTime, "full time");
        setupChipListener(chipPartTime, "part time");
        setupChipListener(chipRemote, "remote");

        // 4. Load initial data
        jobsContainer.removeAllViews();
        fetchJobs("");

        return view;
    }

    private void setupChipListener(TextView chip, String category) {
        chip.setOnClickListener(v -> {
            resetChipStyles();
            chip.setBackgroundResource(R.drawable.bg_chip_lime);
            chip.setTextColor(Color.parseColor("#2D2D2D"));

            jobsContainer.removeAllViews();
            fetchJobs(category);
        });
    }

    private void resetChipStyles() {
        TextView[] allChips = {chipAll, chipInternship, chipFullTime, chipPartTime, chipRemote};
        for (TextView c : allChips) {
            c.setBackgroundResource(R.drawable.bg_chip_transparent);
            c.setTextColor(Color.WHITE);
        }
    }

    private void fetchJobs(String categoryFilter) {
        new Thread(() -> {
            try {
                String searchTerm = "software developer";

                if (!categoryFilter.isEmpty()) {
                    searchTerm += " " + categoryFilter;
                }

                String encodedQuery = URLEncoder.encode(searchTerm, "UTF-8");

                String apiUrl = "https://api.adzuna.com/v1/api/jobs/in/search/1" +
                        "?app_id=" + APP_ID +
                        "&app_key=" + APP_KEY +
                        "&what=" + encodedQuery +
                        "&where=india";

                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) json.append(line);
                reader.close();

                parseJobs(json.toString());

            } catch (Exception e) {
                e.printStackTrace();
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getActivity(), "Failed to load jobs", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        }).start();
    }

    private void parseJobs(String response) {
        try {
            JSONObject root = new JSONObject(response);
            JSONArray results = root.getJSONArray("results");

            if (getActivity() == null) return;

            getActivity().runOnUiThread(() -> {
                if (results.length() == 0) {
                    Toast.makeText(getContext(), "No jobs found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Loop through results and pass index 'i' for alternating colors
                for (int i = 0; i < results.length(); i++) {
                    try {
                        JSONObject job = results.getJSONObject(i);
                        String title = job.getString("title");
                        String company = job.getJSONObject("company").getString("display_name");
                        String location = job.getJSONObject("location").getString("display_name");

                        addJobCard(title, company, location, i);

                    } catch (Exception ignored) {}
                }
            });

        } catch (Exception ignored) {}
    }

    private void addJobCard(String title, String company, String location, int index) {
        if (getContext() == null) return;

        // Inflate layout
        View cardView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_job_card_dynamic, jobsContainer, false);

        // Find Views
        CardView card = (CardView) cardView;
        View logoContainer = cardView.findViewById(R.id.logoContainer);
        TextView tvTitle = cardView.findViewById(R.id.tvTitle);
        TextView tvCompany = cardView.findViewById(R.id.tvCompany);
        TextView tvLocation = cardView.findViewById(R.id.tvLocation);
        TextView tvInitial = cardView.findViewById(R.id.tvCompanyInitial);

        if (index % 2 == 0) {

            card.setCardBackgroundColor(Color.WHITE);
            logoContainer.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#1565C0")));
            tvInitial.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            // ODD: Soft Indigo Card, White Logo Box
            card.setCardBackgroundColor(Color.parseColor("#EEF2FF"));
            logoContainer.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            tvInitial.setTextColor(Color.parseColor("#5C6BC0"));
        }

        // Set Data
        tvTitle.setText(title);
        tvCompany.setText(company);

        if (location.contains(",")) {
            tvLocation.setText(location.split(",")[0].trim());
        } else {
            tvLocation.setText(location);
        }

        if (company != null && !company.isEmpty()) {
            tvInitial.setText(String.valueOf(company.charAt(0)).toUpperCase());
        }

        jobsContainer.addView(cardView);
    }
}