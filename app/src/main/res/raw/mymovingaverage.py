from movingaverage import movingaverage

def getMovingaverage(data, subset_siz, data_is_list = None, avoid_fp_drift = True):
    return list(movingaverage(data, subset_siz, data_is_list, avoid_fp_drift))