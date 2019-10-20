package com.sse.iamhere.Server;

import android.text.TextUtils;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGen {
    private static HttpLoggingInterceptor.Level loggingLevel = HttpLoggingInterceptor.Level.BASIC;

    public static <S> S createService(Class<S> serviceClass, String baseURL) {
        return createService(serviceClass, baseURL, null, null);
    }

    public static <S> S createService(Class<S> serviceClass, String baseURL, String username, String password) {
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            String authToken = Credentials.basic(username, password);
            return createService(serviceClass, baseURL, authToken);
        }

        return createService(serviceClass, baseURL, null);
    }

    private static <S> S createService(Class<S> serviceClass, String baseURL, final String authToken) {
        Retrofit.Builder builder = new Retrofit.Builder()
                                    .baseUrl(baseURL)
                                    .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        if (!httpClient.interceptors().contains(logging)) {
            logging.setLevel(loggingLevel);

            httpClient.addInterceptor(logging);
            builder.client(httpClient.build());
            retrofit = builder.build();
        }

        if (!TextUtils.isEmpty(authToken)) {
            AuthenticationInterceptor interceptor = new AuthenticationInterceptor(authToken);
            if (!httpClient.interceptors().contains(interceptor)) {
                httpClient.addInterceptor(interceptor);

                builder.client(httpClient.build());
                retrofit = builder.build();
            }
        }

        return retrofit.create(serviceClass);
    }
}