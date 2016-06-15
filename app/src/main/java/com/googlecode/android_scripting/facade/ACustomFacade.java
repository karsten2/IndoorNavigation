package com.googlecode.android_scripting.facade;

import android.util.Log;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcMinSdk;
import com.googlecode.android_scripting.rpc.RpcParameter;
import com.python.python27.config.GlobalConstants;

import java.util.concurrent.CountDownLatch;

@RpcMinSdk(4)
public class ACustomFacade extends RpcReceiver {

    private final CountDownLatch mOnInitLock;

    public ACustomFacade(FacadeManager manager) {
        super(manager);
        mOnInitLock = new CountDownLatch(1);
        mOnInitLock.countDown();
    }

    public void shutdown() {
    }

    // Usage example from Python code:
    //
    // import android
    // droid = android.Android()
    // droid.aHelloFunction("hello rpc")
    @Rpc(description = "Print hello in logcat")
    public void aHelloFunction(@RpcParameter(name = "message") String message) throws InterruptedException {
        mOnInitLock.await();
        Log.i(GlobalConstants.LOG_TAG, "ACustomFacade received: " + message);
    }

    @Rpc(description = "sends string",
            returns = "a message")
    public String getCustomMessage() {
        return "custom facade message";
    }
}