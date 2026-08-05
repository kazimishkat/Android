package com.example.pharmacymanagement.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // Emulator
    private static final String BASE_URL = "http://10.0.2.2:8085/";

    // Real Device
    // private static final String BASE_URL = "http://192.168.88.250:8085/";
    public static final String IMAGE_URL = BASE_URL + "uploads";

    private static Retrofit retrofit;

    public static ApiService getClient(Context context) {

        if (retrofit == null) {

            HttpLoggingInterceptor logging =
                    new HttpLoggingInterceptor();

            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(new AuthInterceptor(context))
                    .addInterceptor(logging)
                    .build();

            // NEWLY ADDED
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(java.time.LocalDateTime.class, new com.google.gson.JsonDeserializer<java.time.LocalDateTime>() {
                        @Override
                        public java.time.LocalDateTime deserialize(com.google.gson.JsonElement json, java.lang.reflect.Type typeOfT, com.google.gson.JsonDeserializationContext context) throws com.google.gson.JsonParseException {
                            String s = json.getAsString();
                            try {
                                return java.time.LocalDateTime.parse(s, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
                            } catch (Exception e) {
                                try {
                                    return java.time.ZonedDateTime.parse(s).toLocalDateTime();
                                } catch (Exception e2) {
                                    return java.time.OffsetDateTime.parse(s).toLocalDateTime();
                                }
                            }
                        }
                    })
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit.create(ApiService.class);
    }
}
