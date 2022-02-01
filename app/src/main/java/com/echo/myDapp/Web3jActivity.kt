package com.echo.myDapp

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.blockchain_layout.*
import java.math.BigDecimal


class Web3jActivity : BaseActivity() {

    private var tokenBalance = BigDecimal("0")
    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.blockchain_layout)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        val policy = ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        initView()
        initObserver()
    }


    private fun initView() {
        btn_check_balance.setOnClickListener {
//            viewModel.checkBnbBalance()
            viewModel.checkCustomBalance()
        }
        btn_connect.setOnClickListener {
            viewModel.setNewCredentials()
        }

        btn_send.setOnClickListener {
//            viewModel.sendToken(0.01.toLong())
            viewModel.sendCustomToken()
        }
    }

    private fun initObserver() {
        viewModel.balanceLiveData.observe(this) {
            tokenBalance = it
            tv_balance.text =
                getString(R.string.balance, it)
        }

        viewModel.publicAddressLiveData.observe(this) {
            tv_address.text = getString(R.string.your_address_will_be_shown_here, it)
        }

        viewModel.gasFeeMutableLiveData.observe(this) {
            tv_gas_fee.text = getString(R.string.gasFee, it)
        }
    }

}