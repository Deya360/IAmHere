package com.sse.iamhere.Server;

import android.text.TextUtils;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.sse.iamhere.Utils.Constants.TOKEN_ACCESS;
import static com.sse.iamhere.Utils.Constants.TOKEN_NONE;
import static com.sse.iamhere.Utils.Constants.TOKEN_REFRESH;

public class ServiceGen {
    private static Retrofit retrofit = null;
    private static Retrofit retrofitAccess = null;
    private static Retrofit retrofitRefresh = null;
    private static HttpLoggingInterceptor.Level loggingLevel = HttpLoggingInterceptor.Level.BASIC; //TODO: change to level.NONE for release

    public static void resetCachedServices() {
        retrofit=null;
        retrofitAccess=null;
        retrofitRefresh=null;
    }

    static <S> S createService(Class<S> serviceClass, String baseURL, String token, int tokenType) {
        switch (tokenType) {
            case TOKEN_ACCESS:
                return createServiceAccess(serviceClass, baseURL, token);

            case TOKEN_REFRESH:
                return createServiceRefresh(serviceClass, baseURL, token);

            default:
            case TOKEN_NONE:
                return createService(serviceClass, baseURL);
        }
    }


    // Service without any tokens
    private static <S> S createService(Class<S> serviceClass, String baseURL) {
        if (retrofit==null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            if (!httpClient.interceptors().contains(loggingInterceptor)) {
                loggingInterceptor.setLevel(loggingLevel);

                httpClient.addInterceptor(loggingInterceptor);
            }

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }

        return retrofit.create(serviceClass);
    }

    private static <S> S createServiceAccess(Class<S> serviceClass, String baseURL, String accessToken) {
        if (retrofitAccess==null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            if (!httpClient.interceptors().contains(loggingInterceptor)) {
                loggingInterceptor.setLevel(loggingLevel);

                httpClient.addInterceptor(loggingInterceptor);
            }

            if (!TextUtils.isEmpty(accessToken)) {
                Interceptor authInterceptor = chain -> {
                    Request request = chain.request()
                            .newBuilder()
                            .addHeader("access_token", accessToken)
                            .build();
                    return chain.proceed(request);
                };

                if (!httpClient.interceptors().contains(authInterceptor)) {
                    httpClient.addInterceptor(authInterceptor);
                }
            }

            retrofitAccess = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }

        return retrofitAccess.create(serviceClass);
    }

    private static <S> S createServiceRefresh(Class<S> serviceClass, String baseURL, String refreshToken) {
        if (retrofitRefresh==null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            if (!httpClient.interceptors().contains(loggingInterceptor)) {
                loggingInterceptor.setLevel(loggingLevel);

                httpClient.addInterceptor(loggingInterceptor);
            }

            if (!TextUtils.isEmpty(refreshToken)) {
                Interceptor authInterceptor = chain -> {
                    Request request = chain.request()
                            .newBuilder()
                            .addHeader("refresh_token", refreshToken)
                            .build();
                    return chain.proceed(request);
                };

                if (!httpClient.interceptors().contains(authInterceptor)) {
                    httpClient.addInterceptor(authInterceptor);
                }
            }

            retrofitRefresh = new Retrofit.Builder()
                    .baseUrl(baseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }

        return retrofitRefresh.create(serviceClass);
    }
}