package org.bitcoindotcom.bchprocessor.bip70.model;

import com.bitcoin.merchant.app.util.GsonUtil;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.Date;

public class InvoiceStatus {
    @SerializedName("paymentUrl")
    public String paymentUrl;
    @SerializedName("paymentId")
    public String paymentId;
    @SerializedName("memo")
    public String memo;
    @SerializedName("time")
    public Date time; // 2019-10-05T21:44:34.009Z
    @SerializedName("expires")
    public Date expires; // 2019-10-05T21:59:34.009Z
    @SerializedName("status")
    public String status;
    @SerializedName("fiatSymbol")
    public String fiatSymbol; // "USD"
    @SerializedName("fiatRate")
    public double fiatRate; // 222.67
    @SerializedName("txId")
    public String txId;
    @SerializedName("merchantId")
    public String merchantId;
    @SerializedName("outputs")
    public InvoiceStatusOutput[] outputs = new InvoiceStatusOutput[0];

    public static InvoiceStatus fromJson(String message) {
        return GsonUtil.INSTANCE.getGson().fromJson(message, InvoiceStatus.class);
    }

    public String getFirstAddress() {
        return outputs.length > 0 ? outputs[0].address : null;
    }

    public long getTotalBchAmount() {
        long amount = 0;
        for (InvoiceStatusOutput output : outputs) {
            amount += output.amount;
        }
        return amount;
    }

    public boolean isPaid() {
        return Status.paid.name().equals(status);
    }

    public boolean isOpen() {
        return Status.open.name().equals(status);
    }

    public boolean isExpired() {
        return Status.expired.name().equals(status);
    }

    public String getWalletUri() {
        return "bitcoincash:?r=" + paymentUrl;
    }

    @Override
    public String toString() {
        return "InvoiceStatus{" +
                "paymentUrl='" + paymentUrl + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", memo='" + memo + '\'' +
                ", time='" + time + '\'' +
                ", expires='" + expires + '\'' +
                ", status='" + status + '\'' +
                ", txId='" + txId + '\'' +
                ", outputs=" + Arrays.toString(outputs) +
                '}';
    }

    enum Status {
        open, paid, expired
    }
}
