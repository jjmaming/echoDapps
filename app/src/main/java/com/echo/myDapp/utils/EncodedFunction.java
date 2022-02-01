package com.echo.myDapp.utils;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

public class EncodedFunction {

    public static String transfer(String addressTo, BigInteger amount) {
        return FunctionEncoder.encode(new Function("transfer",
                Arrays.asList(new Address(addressTo), new Uint256(amount)),
                Collections.emptyList()));
    }

    public static String balanceOf(Credentials credentials) {
        return FunctionEncoder.encode(new Function("balanceOf",
                Collections.singletonList(new Address(credentials.getAddress())),
                Collections.singletonList(new TypeReference<Uint256>() {
                })));
    }
}
