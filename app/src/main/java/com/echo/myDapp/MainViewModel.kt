package com.echo.myDapp

import android.app.Application
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.echo.myDapp.utils.Constant
import com.echo.myDapp.utils.EncodedFunction
import org.web3j.crypto.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.tx.FastRawTransactionManager
import org.web3j.tx.Transfer
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger


class MainViewModel(private val context: Application) : AndroidViewModel(context) {
    private var web3: Web3j? = null
    private var credentials: Credentials? = null

    var balanceLiveData = MutableLiveData(BigDecimal.valueOf(0))
    var publicAddressLiveData = MutableLiveData("")
    var gasFeeMutableLiveData = MutableLiveData("")

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
        checkIfNetworkConnected { web3j ->
            try {
                val receipt = Transfer.sendFunds(
                    web3j,
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

    fun sendCustomToken(
        amountToken: Long = 0.1.toLong(),
        contractTokenAddress: String = Constant.BCOIN_CONTRACT_ADDRESS
    ) {
        checkIfNetworkConnected { web3j ->
            try {
                val amount = BigInteger.valueOf(amountToken)
                val gasPrice = DefaultGasProvider.GAS_PRICE
                val maxGasPrice = DefaultGasProvider.GAS_LIMIT
                val transactionManager = FastRawTransactionManager(web3j, getAccountCredential())

                Log.d("echo", "gas price : $gasPrice")
                Log.d("echo", "gas limit $maxGasPrice")
                val transactionHash = transactionManager.sendTransaction(
                    gasPrice,
                    maxGasPrice, contractTokenAddress,
                    EncodedFunction.transfer(Constant.SEND_ADDRESS, amount),
                    BigInteger.ZERO
                ).transactionHash

                val transactionReceipt =
                    web3j.ethGetTransactionReceipt(transactionHash).send().transactionReceipt
                if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        transactionReceipt.isPresent
                    } else {
                        false
                    }
                ) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Log.d("echo", transactionReceipt.get().toString())
                        showToast("transaction success")
                        Log.d("echo", "success")
                    } else {
                        showToast("transaction failed null")
                    }
                } else {
                    showToast("transaction failed")
                    Log.d("echo", "transaction failed")
                }

            } catch (e: java.lang.Exception) {
                showToast(e.message.toString())
                Log.d("echo", e.message.toString())
            }
        }
    }


//    fun sendERCToken(weiAmount: Long) {
//        checkIfNetworkConnected { web3j ->
//
//            // Get the latest nonce of current account
//            val ethGetTransactionCount = web3j
//                .ethGetTransactionCount(
//                    getAccountCredential().address,
//                    DefaultBlockParameterName.LATEST
//                )
//                .send()
//            val nonce = ethGetTransactionCount.transactionCount
//
//            // Gas Parameter
//            val gasLimit = BigInteger.valueOf(21000)
//            val gasPrice =
//                BigInteger.valueOf(5000)
//
//            try {
//                val transaction =
//                    Transaction(
//                        getAccountCredential().address,
//                        nonce,
//                        gasPrice,
//                        gasLimit,
//                        Constant.SEND_ADDRESS,
//                        BigInteger.valueOf(weiAmount),
//                        Constant.BCOIN_CONTRACT_ADDRESS,
//                        ChainIdLong.ETHEREUM_CLASSIC_MAINNET,
//                        null,
//                        null
//                    )
//                val receipt = web3j.ethSendTransaction(transaction).send()
//                showToast("Transaction successful: " + receipt.transactionHash)
//            } catch (e: java.lang.Exception) {
//                showToast(e.message.toString())
//            }
//        }
//    }

    fun checkCustomBalance(contractTokenAddress: String = Constant.BCOIN_CONTRACT_ADDRESS) {
        checkIfNetworkConnected { web3j ->
            val transaction = Transaction.createEthCallTransaction(
                getAccountCredential().address,
                contractTokenAddress,
                EncodedFunction.balanceOf(getAccountCredential())
            )
            val response = web3j.ethCall(transaction, DefaultBlockParameterName.LATEST)?.send()

            val balance = Numeric.toBigInt(response?.value)
            val weiBalance = Convert.fromWei(balance.toString(), Convert.Unit.ETHER)
            balanceLiveData.value = weiBalance
        }
    }

    fun checkBnbBalance() {
        checkIfNetworkConnected { web3j ->
            try {
                val balanceWei = web3j.ethGetBalance(
                    getAccountCredential().address,
                    DefaultBlockParameterName.LATEST
                )?.sendAsync()?.get()
                balanceLiveData.value =
                    BigDecimal.valueOf(balanceWei?.balance.toString().toLong())
            } catch (e: java.lang.Exception) {
                showToast(e.message.toString())
            }
        }
    }

    private fun estimateGasFee() {
        checkIfNetworkConnected { web3j ->
            val transaction = Transaction.createContractTransaction(
                Constant.BCOIN_CONTRACT_ADDRESS,
                null,
                null,
                null
            )
            val amount = web3j.ethEstimateGas(transaction).sendAsync().get().amountUsed
            gasFeeMutableLiveData.value =
                Convert.fromWei(BigDecimal(amount), Convert.Unit.ETHER).toString()
        }
    }

    private fun getAccountCredential(): Credentials {
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

    private fun checkIfNetworkConnected(unit: (web3j: Web3j) -> Unit) {
        web3?.let {
            unit.invoke(it)
            return
        }
        connectToNetwork {
            unit.invoke(web3!!)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}