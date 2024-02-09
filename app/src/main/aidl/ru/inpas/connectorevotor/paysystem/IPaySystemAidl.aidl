// IPaySystemAidl.aidl
package ru.inpas.connectorevotor.paysystem;

import ru.inpas.connectorevotor.paysystem.IPaySystemListener;

interface IPaySystemAidl {
    	void startPayment(String amount, String json);
    	void startRefund(String amount, String json);
    	void startReversal(String amount, String json);
    	void startReconciliation();
    	void doSomething(String json);
    	void registerCallback(IPaySystemListener callback);
        void unregisterCallback();
}