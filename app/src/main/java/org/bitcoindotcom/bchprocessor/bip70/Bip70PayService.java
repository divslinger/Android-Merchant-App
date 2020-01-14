package org.bitcoindotcom.bchprocessor.bip70;

import org.bitcoindotcom.bchprocessor.bip70.model.InvoiceCreation;
import org.bitcoindotcom.bchprocessor.bip70.model.InvoiceStatus;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface Bip70PayService {
    static Bip70PayService create(String baseUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return retrofit.create(Bip70PayService.class);
    }

    @POST("create_invoice")
    Call<InvoiceStatus> createInvoice(@Body InvoiceCreation r);
}
