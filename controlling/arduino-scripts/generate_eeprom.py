
eeprom_sim = [
    "byte EEPROM_SIM[] = {",
    "//general",
    "I2C_ADDRESS, // i2c address"
    "$N_DEVICES$, // # of devices"
    "0, 0, 0, 0, 0, 0, 0, 0, 0,",
    "//devices",
]

n_devices = input("How many devices do you want?")

eeprom_sim[2] = eeprom_sim[2].replace("$N_DEVICES$", n_devices)

for i in range(int(n_devices)):
    device_type = prompt("What type of device do you want to use? (pin, pwm)")

    str = ""

    if device_type == "pin":
        str += "0x01, "

        pin = input("What pin do you want to use?")
        str += f"{pin}, "

        out_or_in = input("Is this an output or input? (out, in)")
        if out_or_in == "out":
            str += "0x01, "
        elif out_or_in == "in":
            str += "0x00, "

    elif device_type == "pwm":
        str += "0x02, "

        pin = input("What pin do you want to use?")
        str += f"{pin}, "

    # todo: add # of bytes in hex, then add values


# remove last comma
eeprom_sim[-1] = eeprom_sim[-1].replace(",", "")

eeprom_sim.append("};")

print("-- copy and paste the following --\n")

for line in eeprom_sim:
    print(line)