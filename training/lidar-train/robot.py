

def arcade_drive(forward, horizontal):
    # Compute raw motor outputs from arcade drive inputs.
    left_motor = forward + horizontal
    right_motor = forward - horizontal

    # Normalize the outputs to ensure they're within [-1, 1]
    max_val = max(abs(left_motor), abs(right_motor))
    if max_val > 1:
        left_motor /= max_val
        right_motor /= max_val

    return left_motor, right_motor