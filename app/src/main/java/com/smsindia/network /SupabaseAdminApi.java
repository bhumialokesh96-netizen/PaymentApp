package com.smsindia.network;

import com.smsindia.model.WithdrawalModel;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.Query;

public interface SupabaseAdminApi {

    // 1. Fetch only PENDING requests
    @GET("/rest/v1/withdrawals?select=*&status=eq.PENDING")
    Call<List<WithdrawalModel>> getPendingWithdrawals(
        @Header("apikey") String apiKey,
        @Header("Authorization") String auth
    );

    // 2. Mark as PAID
    @PATCH("/rest/v1/withdrawals")
    Call<Void> markAsPaid(
        @Header("apikey") String apiKey,
        @Header("Authorization") String auth,
        @Query("id") String idQuery, // usage: "eq.UUID"
        @Body Map<String, Object> body
    );
}
