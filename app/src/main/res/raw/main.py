import android, time
import mymovingaverage
import knn

droid = android.Android()
droid.sendMessage("Main script successfully started")
state = droid.getState().result

while state:
    if state == 2:
        params = droid.getMovingAverageParameter().result
        result = mymovingaverage.getMovingaverage(params['data'], params['windowSize'])
        droid.setMovingAverage(result)
        droid.resetState()
    if state == 3:
        droid.sendMessage('run knn main')
        knn.main()

        droid.resetState()

    state = droid.getState().result
    time.sleep(5)