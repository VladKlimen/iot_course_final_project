import numpy as np
from scipy.signal import find_peaks, butter, lfilter


def calculate_steps(gyroscope_magnitude,sampling_rate, height, threshold, prominence):
    # Low-pass filtering (optional, if required)
    cutoff_freq = 2  # Adjust the cutoff frequency based on your gyroscope data (Nyquist frequency = sampling_rate / 2)
    normalized_cutoff_freq = cutoff_freq / (sampling_rate / 2)
    b, a = butter(4, normalized_cutoff_freq, btype='low', analog=False)
    gyroscope_magnitude = lfilter(b, a, gyroscope_magnitude)

    # Detect peaks
    peaks, _ = find_peaks(gyroscope_magnitude, distance=sampling_rate * 0.25,
                          height=height,
                          threshold=threshold,
                          prominence=prominence)

    # Indicate peak/non-peak elements in the data variable
    data_indicator = np.zeros_like(gyroscope_magnitude)
    data_indicator[peaks] = 1

    # Get list of peak indices
    peak_indices = np.where(data_indicator == 1)[0]

    num_steps = len(peak_indices)

    return num_steps

heights = range(6,17)
thresholds = [0.0001, 0.001, 0.0012, 0.0014,0.1,1]
prominences = range(7)

def main(magnitude_data, actual_repetitions):
    actual_num_steps = actual_repetitions
    best_error = np.inf
    best_height = None
    best_threshold = None
    best_prominence = None
    sampling_rate = 1.0 / 0.086	# this is defined in our adruino code, always the same


    for height in heights:
        for threshold in thresholds:
            for prominence in prominences:
                num_steps = calculate_steps(magnitude_data,sampling_rate, height, threshold, prominence)
                error = np.absolute(num_steps - actual_num_steps) / actual_num_steps * 100
                if error <= best_error:
                    best_error = error
                    best_height = height
                    best_threshold = threshold
                    best_prominence = prominence

	
    return best_height, best_threshold, best_prominence
