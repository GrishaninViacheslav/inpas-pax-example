// IPaySystemListener.aidl
package ru.inpas.connectorevotor.paysystem;

import ru.inpas.connectorevotor.paysystem.IPaySystemListener;

interface IPaySystemListener {

       void onTransactionResponse(int transactionCode, String response);
}