package com.triplet.yellapp.utils;

import com.triplet.yellapp.models.DashboardCard;
import com.triplet.yellapp.models.InfoMessage;
import com.triplet.yellapp.models.Notification;
import com.triplet.yellapp.models.UserAccountFull;
import com.triplet.yellapp.models.YellTask;
import com.triplet.yellapp.models.TokenPair;
import com.triplet.yellapp.models.UserAccount;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {

    @POST("users")
    @Headers("Content-Type: application/json")
    Call<InfoMessage> register(@Body RequestBody body);

    @POST("auth")
    @Headers("Content-Type: application/json")
    Call<TokenPair> login(@Body RequestBody body);

    @GET("auth")
    Call<TokenPair> refresh(@Header("Authorization") String refreshToken);

    @GET("users")
    Call<UserAccount> getUserProfile(@Query("fetch") String fetch);

    @GET("users")
    Call<UserAccountFull> getUserFull(@Query("fetch") String fetch);

    @DELETE("auth")
    Call<InfoMessage> logout();

    @POST("users/verify")
    @Headers("Content-Type: application/json")
    Call<InfoMessage> verify(@Body RequestBody body);

    @POST("users/verify/resend")
    @Headers("Content-Type: application/json")
    Call<InfoMessage> resendVerification(@Body RequestBody body);

    @POST("dashboards")
    @Headers("Content-Type: application/json")
    Call<DashboardCard> addDashboard(@Body RequestBody body);

    @GET("dashboards")
    Call<DashboardCard> getDashboard(@Query("dashboard_id") String dashboardId, @Query("fetch") String fetch);

    @PATCH("dashboards")
    @Headers("Content-Type: application/json")
    Call<InfoMessage> editDashboard(@Body RequestBody body);

    @HTTP(method = "DELETE", path = "dashboards", hasBody = true)
    @Headers("Content-Type: application/json")
    Call<InfoMessage> deleteDashboard(@Body RequestBody body);

    @POST("dashboards/permission")
    @Headers("Content-Type: application/json")
    Call<InfoMessage> inviteSoToDashboard(@Body RequestBody body);

    @GET("notifications")
    Call<List<Notification>> getNotification(@Query("limit") String limit);

    @POST("notifications/confirm")
    @Headers("Content-Type: application/json")
    Call<InfoMessage> confirmInvited(@Body RequestBody body);

    @HTTP(method = "DELETE", path = "notifications/confirm", hasBody = true)
    @Headers("Content-Type: application/json")
    Call<InfoMessage> rejectInvited(@Body RequestBody body);

    @PATCH("notification/read")
    @Headers("Content-Type: application/json")
    Call<InfoMessage> read(@Body RequestBody body);

    @PATCH("notification/unread")
    @Headers("Content-Type: application/json")
    Call<InfoMessage> unread(@Body RequestBody body);

    @GET("tasks")
    Call<YellTask> getTask(@Query("task_id") String taskId);

    @Multipart
    @POST("tasks")
    Call<YellTask> addTask(@Part MultipartBody.Part file, @Part("data") RequestBody body);

    @Multipart
    @PATCH("tasks")
    Call<InfoMessage> editTask(@Part MultipartBody.Part file, @Part("data") RequestBody body);

    @HTTP(method = "DELETE", path = "tasks", hasBody = true)
    @Headers("Content-Type: application/json")
    Call<InfoMessage> deleteTask(@Body RequestBody body);

    /*@POST("budgets")
    @Headers("Content-Type: application/json")
    Call<BudgetCard> createBudget(@Body RequestBody body);

    @GET("budgets")
    Call<BudgetCard> getBudget(@Query("budget_id") String budgetID, @Query("fetch") String fetch);


    @PATCH("budgets")
    @Headers("Content-Type: application/json")
    Call<InfoMessage> editBudgets(@Body RequestBody body);

    @HTTP(method = "DELETE", path = "budgets", hasBody = true)
    @Headers("Content-Type: application/json")
    Call<InfoMessage> deleteBudgets(@Body RequestBody body);


    @GET("transactions")
    Call<TransactionCard> getTransaction(@Query("transaction_id") String transactionID);

    @POST("transactions")
    @Headers("Content-Type: application/json")
    Call<TransactionCard> addTransaction(@Body RequestBody body);

    @PATCH("transactions")
    Call<InfoMessage> editTransaction(@Body RequestBody body);

    @HTTP(method = "DELETE", path = "transactions", hasBody = true)
    @Headers("Content-Type: application/json")
    Call<InfoMessage> deleteTransaction(@Body RequestBody body);*/

}