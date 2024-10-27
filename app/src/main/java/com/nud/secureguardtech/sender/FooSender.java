package com.nud.secureguardtech.sender;

public class FooSender extends Sender {

    public final static String TYPE = "FOO";

    public FooSender() {
        super("", TYPE);
    }

    @Override
    protected void sendMessage(String destination, String msg) {
    }
}
