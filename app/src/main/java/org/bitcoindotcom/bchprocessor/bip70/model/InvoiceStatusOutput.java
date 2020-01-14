package org.bitcoindotcom.bchprocessor.bip70.model;

public class InvoiceStatusOutput {
    public String script;
    public long amount;
    public String address;
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
