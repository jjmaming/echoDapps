package com.echo.myDapp

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.echo.myDapp.utils.Constant
import org.web3j.crypto.Credentials
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.tx.*
import org.web3j.utils.Convert
import java.io.File
import java.math.BigDecimal


class MainViewModel(private val context: Application) : AndroidViewModel(context) {
    private var web3: Web3j? = null
    private var credentials: Credentials? = null

    var balanceLiveData = MutableLiveData<BigDecimal>()
    var publicAddressLiveData = MutableLiveData<String>()
    var gasFeeMutableLiveData = MutableLiveData<String>()

    init {
        connectToNetwork()
    }

    private fun connectToNetwork(unit: (() -> Unit?)? = null) {
        try {
            web3 =
                Web3j.build(HttpService(Constant.BLOCK_CHAIN_URL))
            estimateGasFee()
            showToast(context.getString(R.string.lbl_connecting_to_smart_chain))

            //if the client version has an error the user will not gain access if successful the user will get connected
            val clientVersion = web3?.web3ClientVersion()?.sendAsync()?.get()
            clientVersion?.let {
                if (!it.hasError()) {
                    unit?.invoke()
                    showToast(context.getString(R.string.lbl_connected))
                } else {
                    showToast(it.error.message)
                }
            }
        } catch (e: Exception) {
            showToast(e.message.toString())
        }
    }

    fun sendToken(
        amout: Long,
        receiverAddress: String = Constant.SEND_ADDRESS,
        unit: Convert.Unit = Convert.Unit.ETHER
    ) {
        checkIfNetworkConnected {
            try {
                val receipt = Transfer.sendFunds(
                    web3,
                    getAccountCredential(),
                    receiverAddress,
                    BigDecimal.valueOf(amout),
                    unit
                ).send()
                showToast("Transaction successful: " + receipt.transactionHash)
            } catch (e: java.lang.Exception) {
                showToast(e.message.toString())
            }
        }
    }

    private fun estimateGasFee() {
        checkIfNetworkConnected {
            val transaction = Transaction.createContractTransaction(
                Constant.BCOIN_CONTRACT_ADDRESS,
                null,
                null,
                null
            )
            val amount = web3?.ethEstimateGas(transaction)?.sendAsync()?.get()?.amountUsed
            gasFeeMutableLiveData.value =
                Convert.fromWei(BigDecimal(amount), Convert.Unit.ETHER).toString()

        }
    }

//    fun checkBalanceByToken(){
//        checkIfNetworkConnected {
//            val transactionManager: TransactionManager = RawTransactionManager(
//                web3, credentials, ChainIdLong.MAINNET
//            )
//
//        }
//    }

    fun checkBalance() {
        checkIfNetworkConnected {
            try {
                val balanceWei = web3?.ethGetBalance(
                    getAccountCredential().address,
                    DefaultBlockParameterName.LATEST
                )?.sendAsync()?.get()
                balanceLiveData.value =
                    Convert.fromWei(balanceWei?.balance.toString(), Convert.Unit.ETHER)
            } catch (e: java.lang.Exception) {
                showToast(e.message.toString())
            }
        }
    }

    fun getAccountCredential(): Credentials {
        credentials = credentials ?: Credentials.create(Constant.ACCOUNT_PRIVATE_KEY)
        publicAddressLiveData.value = credentials?.address
        return credentials!!
    }


    fun setNewCredentials(privateKey: String = Constant.ACCOUNT_PRIVATE_KEY) {
        credentials = Credentials.create(privateKey)
        publicAddressLiveData.value = credentials?.address
    }

    private fun createWallet(password: String, file: File) {
        try {
            // generating the etherium wallet
            val walletName = WalletUtils.generateLightNewWalletFile(password, file)
            credentials = WalletUtils.loadCredentials(password, "$file/$walletName")
        } catch (e: java.lang.Exception) {
            showToast(e.message.toString())
        }
    }

    private fun checkIfNetworkConnected(unit: () -> Unit) {
        web3?.let {
            unit.invoke()
            return
        }
        connectToNetwork {
            unit.invoke()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}