package com.bitcoin.merchant.app

interface Action {
    companion object {
        const val PACKAGE = MainActivity.APP_PACKAGE
        const val SET_PAYMENT_TARGET = PACKAGE + "Action.SET_PAYMENT_TARGET"
        const val PARAM_PAYMENT_TARGET = PACKAGE + "PAYMENT_TARGET"
        const val SUBSCRIBE_TO_ADDRESS = PACKAGE + "SUBSCRIBE_TO_ADDRESS"
    }
}