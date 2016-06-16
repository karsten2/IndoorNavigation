package com.googlecode.android_scripting.facade;

import android.content.Intent;
import android.util.Log;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcMinSdk;
import com.googlecode.android_scripting.rpc.RpcParameter;
import com.indoornavigation.Controller.PythonDataController;
import com.python.python27.config.GlobalConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@RpcMinSdk(4)
public class CustomDataFacade extends RpcReceiver {

    private final CountDownLatch mOnInitLock;

    public CustomDataFacade(FacadeManager manager) {
        super(manager);
        mOnInitLock = new CountDownLatch(1);
        mOnInitLock.countDown();
    }

    @Override
    public void shutdown() { }

    @Rpc(description = "Current value of the state variable.")
    public int getState() {
        return PythonDataController.getState();
    }

    @Rpc(description = "Function sets the state to the idle value")
    public void resetState() {
        PythonDataController.setState(1);
    }

    @Rpc(description = "sends data for moving average py script",
            returns = "key value pair with paramenters")
    public Map<String, Object> getMovingAverageParameter() {
        Map ret = new HashMap<>();
        ret.put("data", PythonDataController.MovingAverage.getMovingAvgData());
        ret.put("windowSize", PythonDataController.MovingAverage.getMovingAvgWindowSize());
        return ret;
    }

    @Rpc(description = "Receives value from python.")
    public void setMovingAverage(@RpcParameter(name = "value") Double value) {
        PythonDataController.MovingAverage.setResult(value);
    }

    @Rpc(description = "Write a message to logcat")
    public void sendMessage(@RpcParameter(name = "message") String message) {
        Log.d("CustomDataFacade", message);
    }



}