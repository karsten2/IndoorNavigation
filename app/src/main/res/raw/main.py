import android, time
import mymovingaverage

droid = android.Android()
droid.sendMessage("Main script successfully started")
state = droid.getState().result

while state:
    if state == 2:
        params = droid.getMovingAverageParameter().result
        result = mymovingaverage.getMovingaverage(params['data'], params['windowSize'])
        droid.sendMessage('Result' + str(result))
        droid.setMovingAverage(result)
        droid.makeToast("Moving Average: " + str(result))
        droid.resetState()

    state = droid.getState().result
    time.sleep(5)