package com.smsindia.app.ui; // ✅ FIXED PACKAGE

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// ✅ FIXED IMPORTS (No more "com.yourpackage")
import com.smsindia.app.R;
import com.smsindia.app.adapter.WithdrawalAdapter;
import com.smsindia.app.model.WithdrawalModel;
import com.smsindia.app.network.SupabaseAdminApi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AdminPaymentActivity extends AppCompatActivity {

    // 🔴 TODO: PUT YOUR REAL URL AND KEY HERE
    private static final String SUPABASE_URL = "https://YOUR_PROJECT.supabase.co";
    private static final String SUPABASE_KEY = "YOUR_SERVICE_ROLE_KEY"; 

    final int UPI_PAYMENT_REQ_CODE = 123;
    RecyclerView recyclerView;
    SupabaseAdminApi api;
    String currentPayingId = ""; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_payment); 

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SUPABASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(SupabaseAdminApi.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fetchWithdrawals();
    }

    private void fetchWithdrawals() {
        api.getPendingWithdrawals(SUPABASE_KEY, "Bearer " + SUPABASE_KEY).enqueue(new Callback<List<WithdrawalModel>>() {
            @Override
            public void onResponse(Call<List<WithdrawalModel>> call, Response<List<WithdrawalModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WithdrawalAdapter adapter = new WithdrawalAdapter(response.body(), request -> {
                        launchUpi(request.upi_id, request.amount, request.id);
                    });
                    recyclerView.setAdapter(adapter);
                }
            }
            @Override public void onFailure(Call<List<WithdrawalModel>> call, Throwable t) { }
        });
    }

    private void launchUpi(String upiId, String amount, String dbId) {
        this.currentPayingId = dbId;
        Uri uri = Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", "SMS Admin")
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        Intent chooser = Intent.createChooser(intent, "Pay via");
        if (chooser.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(chooser, UPI_PAYMENT_REQ_CODE);
        } else {
            Toast.makeText(this, "No UPI App Installed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPI_PAYMENT_REQ_CODE) {
            if ((RESULT_OK == resultCode) || (resultCode == 11)) {
                if (data != null) {
                    String response = data.getStringExtra("response");
                    checkPaymentStatus(response);
                } else {
                    checkPaymentStatus("nothing");
                }
            } else {
                checkPaymentStatus("nothing");
            }
        }
    }

    private void checkPaymentStatus(String response) {
        String status = "";
        String txnRef = "";
        if (response == null) response = "discard";
        String[] parts = response.split("&");
        for (String part : parts) {
            String[] pair = part.split("=");
            if (pair.length >= 2) {
                if (pair[0].equalsIgnoreCase("Status")) {
                    status = pair[1].toLowerCase();
                } else if (pair[0].equalsIgnoreCase("txnRef") || pair[0].equalsIgnoreCase("approvalRefNo")) {
                    txnRef = pair[1];
                }
            }
        }
        if (status.equals("success")) {
            Toast.makeText(this, "Payment Successful!", Toast.LENGTH_LONG).show();
            markRequestAsPaid(currentPayingId, txnRef);
        } else {
            Toast.makeText(this, "Payment Failed or Cancelled", Toast.LENGTH_LONG).show();
        }
    }

    private void markRequestAsPaid(String id, String refNo) {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("status", "PAID");
        updateMap.put("txn_ref", refNo);
        api.markAsPaid(SUPABASE_KEY, "Bearer " + SUPABASE_KEY, "eq." + id, updateMap).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(AdminPaymentActivity.this, "Database Updated!", Toast.LENGTH_SHORT).show();
                fetchWithdrawals(); 
            }
            @Override public void onFailure(Call<Void> call, Throwable t) { }
        });
    }
}
