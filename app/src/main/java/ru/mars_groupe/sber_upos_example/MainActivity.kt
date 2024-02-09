package ru.mars_groupe.sber_upos_example

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.RemoteException
import android.util.Log
import ru.evotor.framework.core.IntegrationActivity
import ru.inpas.connectorevotor.paysystem.IPaySystemAidl
import ru.inpas.connectorevotor.paysystem.IPaySystemListener
import ru.mars_groupe.sber_upos_example.databinding.ActivityMainBinding

class MainActivity : IntegrationActivity() {
    companion object {
        const val packageNameUniversal = "ru.inpas.universaldriverinpas" // - наименование пакета.
        const val packageNameVerifone = "ru.inpas.posdriver.verifone" // - наименование пакета.
        const val packageNamePax = "ru.inpas.posdriver.pax" // - наименование пакета.

        const val patchService =
            "ru.inpas.connectorevotor.POSService" // - путь до сервиса (полное наименование сервиса).
        const val ACTION_AIDL = "ru.inpas.connectorevotor.PaySystemUPOS" // -  флаг сервиса;
    }

    private lateinit var binding: ActivityMainBinding

    private var isAdapterRegistered = false
    private var isServiceBound = false
    private var isServiceFound = false

    private var posCoreClientAidlInterface: IPaySystemAidl? = null // – интерфейс сервиса.
    private val posCoreInterface: IPaySystemAidl
        get() = posCoreClientAidlInterface!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViews()
    }

    private fun initViews() = with(binding) {
        lunchUpos.setOnClickListener {
            lunchUpos()
        }
    }

    private fun lunchUpos() {
        if (!isServiceBound) {
            bindUposService()
            if (isServiceFound) {
                showMessage("showLoadingScreen")
            } else {
                showMessage(getString(R.string.error_connecting_upos))
            }
        } else {
            showUposScreen()
        }
    }


    private fun bindUposService() {
        showMessage("bindUposService()")
        isAdapterRegistered = false
        val intent = Intent( ACTION_AIDL)
        try {
            intent.component = ComponentName(
                packageNamePax,
                patchService
            )
            isServiceFound = bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            showMessage("bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE): $isServiceFound")
            if (!isServiceFound) {
                showMessage(getString(R.string.driver_not_found))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showMessage("Ошибка bindUposService $e")
        }
    }

    private var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            showMessage("onServiceConnected(name = $name, service = $service)")
            posCoreClientAidlInterface = IPaySystemAidl.Stub.asInterface(service)
            isServiceBound = true
            showUposScreen()
        }

        override fun onServiceDisconnected(name: ComponentName) {
            showMessage("onServiceDisconnected(name = $name)")
            isServiceBound = false
            posCoreClientAidlInterface = null
        }
    }

    private fun showUposScreen() {
        val uposInterface = posCoreClientAidlInterface
        if (uposInterface == null) {
            showMessage(getString(R.string.error_connecting_upos))
            return
        }

        if (!isAdapterRegistered) {
            registerAdapter(uposInterface)
        }

        try {
            posCoreInterface.doSomething("{\"OPERATION\":\"20\"}")
            showMessage("{\"OPERATION\":\"20\"}")
        } catch (e: Throwable) {
            showMessage("Error while register posCoreInterface.doSomething(${"{\"OPERATION\":\"20\"}"}): \n$e")
            e.printStackTrace()
        }
    }

    private fun registerAdapter(uposInterface: IPaySystemAidl) {
        showMessage("registerAdapter(uposInterface: IPaySystemAidl)")
        try {
            uposInterface.registerCallback(object :
                IPaySystemListener.Stub() {
                override fun onTransactionResponse(transactionCode: Int, response: String) {
                    showMessage("onTransactionResponse(transactionCode = $transactionCode)")
                }
            })
            isAdapterRegistered = true
        } catch (e: RemoteException) {
            showMessage("Error while register IPaySystemListener: $e")
        }
    }

    private fun showMessage(message: String) {
        Handler(Looper.getMainLooper()).post {
            Log.d("InpasExample", message)
            // binding.message.setText("${binding.message.text}\n\n$message")
            // Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}