package com.triplet.yellapp.utils;

import com.triplet.yellapp.models.TokenPair;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

public class Interceptors {
    static SessionManager sessionManager;
    Interceptors(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    public static class AccessTokenBinder implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            Request.Builder builder = request.newBuilder();

            if (sessionManager.getToken().getAccessToken() != null) {
                builder.addHeader("Authorization", "Bearer " + sessionManager.getToken().getAccessToken());
            }
            request = builder.build();
            return chain.proceed(request);
        }
    }

    public static class TokenMaintainer implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            Response response = chain.proceed(request);

            if (response.code() == 401) {

                if (responseCount(response) >= 3) {
                    sessionManager.deleteToken();
                    response.close();
                    return response;
                }

                response.close();

                TokenPair token = sessionManager.getToken();

                ApiService service = Client.createService(ApiService.class);
                Call<TokenPair> call = service.refresh("Bearer " + token.getRefreshToken());
                retrofit2.Response<TokenPair> res = call.execute();

                int resCode = res.code();

                if (resCode == 200) {
                    TokenPair newToken = res.body();
                    sessionManager.saveToken(newToken);
                    request = chain.request().newBuilder().header("Authorization", "Bearer " + newToken.getAccessToken()).build();
                    return chain.proceed(request);
                }
                // invalid session
                else if (resCode == 403) {
                    sessionManager.deleteToken();
                    return response;
                }
            }

            return response;
        }
    }

    private static int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }
}
