package com.triplet.yellapp.utils;

import com.squareup.moshi.Moshi;
import com.triplet.yellapp.models.TokenPair;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class Client {
    private final static String API_URL = "https://yell-backend-dev.herokuapp.com/api/";
    private final static OkHttpClient client = buildClient();
    private final static Retrofit retrofit = buildRetrofit(client);

    private static OkHttpClient buildClient(){
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();

                        Request.Builder builder = request.newBuilder()
                                .addHeader("Accept", "application/json")
                                .addHeader("Connection", "close");

                        request = builder.build();

                        return chain.proceed(request);

                    }
                });

        return builder.build();

    }

    private static Retrofit buildRetrofit(OkHttpClient client){
        Moshi moshi = new Moshi.Builder()
                .add(new RealmListJsonAdapterFactory())
                .build();
        return new Retrofit.Builder()
                .baseUrl(API_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build();
    }

    public static <T> T createService(Class<T> service){
        return retrofit.create(service);
    }

    public static <T> T createServiceWithAuth(Class<T> service, final SessionManager sessionManager){

        Interceptors.sessionManager = sessionManager;

        OkHttpClient newClient = client.newBuilder()
                .addInterceptor(new Interceptors.AccessTokenBinder())
                .addInterceptor(new Interceptors.TokenMaintainer())
                .build();

        Retrofit newRetrofit = retrofit.newBuilder().client(newClient).build();
        return newRetrofit.create(service);

    }

    public static Retrofit getInstance() {
        return retrofit;
    }


}
