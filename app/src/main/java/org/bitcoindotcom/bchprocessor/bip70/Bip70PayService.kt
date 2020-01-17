package org.bitcoindotcom.bchprocessor.bip70

import com.bitcoin.merchant.app.util.GsonUtil
import org.bitcoindotcom.bchprocessor.bip70.model.InvoiceRequest
import org.bitcoindotcom.bchprocessor.bip70.model.InvoiceStatus
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface Bip70PayService {
    @POST("create_invoice")
    fun createInvoice(@Body r: InvoiceRequest): Call<InvoiceStatus?>

    companion object {
        fun create(baseUrl: String): Bip70PayService {
            return Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create(GsonUtil.gson))
                    .build()
                    .create(Bip70PayService::class.java)
        }
    }
}