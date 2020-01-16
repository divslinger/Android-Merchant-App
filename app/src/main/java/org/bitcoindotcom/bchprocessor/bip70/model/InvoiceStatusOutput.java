package org.bitcoindotcom.bchprocessor.bip70.model;

import com.google.gson.annotations.SerializedName;

public class InvoiceStatusOutput {
    @SerializedName("script")
    public String script;
    @SerializedName("amount")
    public long amount;
    @SerializedName("address")
    public String address;
    @SerializedName("type")
    public String type;

    @Override
    public String toString() {
        return "InvoiceStatusOutput{" +
                "script='" + script + '\'' +
                ", amount=" + amount +
                ", address='" + address + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
