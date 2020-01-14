package org.bitcoindotcom.bchprocessor.bip70.model;

import com.bitcoin.merchant.app.util.AppUtil;

import java.util.Arrays;

public class InvoiceStatus {
    public String paymentUrl;
    public String paymentId;
    public String memo;
    public String time; // 2019-10-05T21:44:34.009Z
    public String expires; // 2019-10-05T21:59:34.009Z
    public String status;
    public String fiatSymbol; // "USD"
    public double fiatRate; // 222.67
    public String txId;
    public String merchantId;
    public InvoiceStatusOutput[] outputs = new InvoiceStatusOutput[0];

    public static InvoiceStatus fromJson(String message) {
        return AppUtil.GSON.fromJson(message, InvoiceStatus.class);
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
