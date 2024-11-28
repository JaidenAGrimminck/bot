# Installing the LIDAR

> [!IMPORTANT]
> This is for the YDLidar module, for installing the correct packages since the documentation isn't the best.

## Prerequesites

- Raspberry Pi with a Ubuntu distro installed (I used Jammy, 22.04)
- ROS2 Installed on the Pi (I installed [Humble](https://docs.ros.org/en/humble/Installation/Ubuntu-Install-Debs.html))

## Steps

First, you want to clone [this](https://github.com/YDLIDAR/YDLidar-SDK) repository onto your Pi.

Then, once installed, run the following:
```bash
sudo apt install cmake pkg-config
sudo apt-get install swig
```

> [!WARNING]
> This is assuming that Python is already installed! I used Python `3.10.12` for this.

Then, once that's installed, open the `YDLidar-sdk` directory. Run:

```bash
python3 -m pip install .
```

> [!NOTE]
> This will take a bit for it to install.

Then, you'll want to run:

```bash
cd build
cmake ..
make
cpack
```

Once installed, you can test it with:

```bash
sudo ./tri_test
```

> [!IMPORTANT]
> It seems like if you're not a sudo-user, you have to give it sudo access so it can open the comm port.
> Make sure to have it plugged in at this time.
> I selected `128000` for the baud rate, and `/dev/ttyUSB0` for the comm port.
> I also selected only one way communication.
  
This should output some data about the lidar.
  
> [!WARNING]
> I got it to work, but then suddenly when I re-ran the program, I couldn't select `128000` any more.
> I restarted my Pi to try and fix this.
> *30 mins later*: Ok, after a lot of tinkering, I basically couldn't fix it. But, I got the Python to work.

For the Python, open the `python/examples` directory in the base `YDLidar-sdk` directory.

Then, open the `tof_test.py` file with either vim or something else.

> [!NOTE]
> You may have to go back to the base directory and run
> ```bash
> sudo python3 -m pip install .
> ```
> if the `ydlidar` library isn't found.

Then, make sure to change `LidarPropSingleChannel` to `True` and `LidarPropSerialBaudrate` to `128000`.

You may also want to set the port manually.

Also, you'll have to add into the for-loop, after the `if r:`

```python
scan_time = scan.config.scan_time
if scan_time == 0:
    scan_time = 1.0
print("Scan received [",scan.stamp,"]: ",scan.points.size(),"ranges is [",1.0/scan_time,"]Hz")
```

On the first message, the `scan_time` seems to return zero.

Then, to run the file, run

```bash
sudo python3 tof_test.py
```

That got it to work for me.
