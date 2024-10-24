Controlling Getting Started
===========================

.. _controlling_get_started:

Getting Started
---------------

If you haven't done it, yet you should check out the :ref:`installation` section to get the project installed and ready to go.

This means running the following commands:

.. code-block:: console

    (~) $ git clone
    (~) $ cd bot
    (~/bot) $ cd controlling
    (~/bot/controlling) $ ./gradlew clean
    (~/bot/controlling) $ ./gradlew build


Once you have done that, you can start the controlling code with:

.. code-block:: console

    (~/bot/controlling) $ ./gradlew run

Awesome! It may throw an error or two, but that's okay, since we'll get to customizing it in the next section.

If you want a reference on how to use the controlling code, check out the JavaDocs `here <https://jaidenagrimminck.github.io/bot/controlling/docs/javadoc/index.html>`_!


The following guide goes over making a light turn on and off with the controlling code, running on a Raspberry Pi connected 
via a level shifter to an Arduino microcontroller.


Step 1: Creating Our Own Robot
------------------------------

We'll start off simple and easy, by creating a new robot. This robot will be a simple one, with just a light on it!

First, open up the project in your favorite IDE. I recommend IntelliJ IDEA, but you can use whatever you want.

This will work best if the project is opened up to the controlling module directly rather than the encompassing project.

Once you have the project open, navigate to the `src/main/java` directory. We can head over to the `me.autobot.code` package, delete all files except for `Main.java` and create a new class.

We'll call the class, `SimpleBot.java`.

We want to make sure that the class extends `Robot`, so the code should look like this:

.. code-block:: java

    package me.autobot.code;

    import me.autobot.Robot;

    public class SimpleBot extends Robot {

    }

We'll then want to go over to the `Main.java` class and replace the code in the `main` method with the following:

.. code-block:: java

    public static void main(String[] args) {
        new SimpleBot();
    }

Awesome! Now, we have a simple robot that does nothing. Let's add a light to it!

Step 2: Adding a Light
----------------------

To add a light to our robot, we'll need to create a new class. I would create a new folder called `components` in the `me.autobot.code` package.

In this folder, create a new class called `Light.java`.

We'll then want to make sure that the class extends `Device`, so the code should look like this:

.. code-block:: java

    package me.autobot.code.components;

    import me.autobot.Device;

    public class Light extends Device {

    }

Great, now we have an empty light class. Let's add some functionality to it!

Step 3: Adding Functionality
---------------------------

What the Raspberry Pi and the Arduino do is that they communicate with each other via I2C. 
This means that we can send messages between the two devices, allowing us to control the light from the Raspberry Pi.

The I2C devices have a relationship between each other, where there's a *controller* and a *target*.
The controller is the device that sends the message, and the target is the device that receives the message.
Think like the controller as a bossy person, and the target as the person who does the work.

The target can talk back though to the controller... only if the controller asks it to! But, we don't need to talk back in this case.

Here, we want our light, or our target, to turn on and off. So, we'll need to create a method that will send a message to the Raspberry Pi to turn the light on and off.

We first need to use the full functionality of the `Device` class, so we'll start off by making a constructor that takes two `int` parameters, called bus and address, and save them to a field.

.. code-block:: java

    private int bus;

    private int address;

    public Light(int bus, int address) {
        super();

        this.bus = bus;
        this.address = address;
    }

There can be multiple "buses" on the Raspberry Pi, so we need to specify which bus we're using. (Usually, it's 1!)

We then want to make a method called `connectToI2C` that will connect to the I2C bus. We'll let it take in the pin of the light, so we want an `int` parameter called `pin`.

.. code-block:: java

    public void connectToI2C(int pin) {
        // Connect to the I2C bus
    }

In Simulation, many robots can be made at once, all with the same sensor ids and addresses. 
That's why devices can be assigned "parent" robots to make sure that they don't interfere with each other.
Even though we aren't using Simulation here, we want to make sure that we're following the same rules.

In this, we'll use the `SensorHubI2CConnection` class to connect to the I2C bus.
This is a class that's really helpful for communicating with the Arduino code from this library.

We can start off by:
1. Checking if there is a parent attached to the robot.
2. Check if we are not in simulation.
3. Assign a "i2cRef" (I2C referral) for the I2C SensorHub connection.
4. Save the pin to a field.

.. warning::

    If you are in simulation, you should not be using the I2C bus! This is because the simulation may be running on a device without access to the I2C bus!

.. code-block:: java

    private int pin;
    private SensorHubI2CConnection i2cRef;

    public void connectToI2C(int pin) {
        if (this.getParent() == null) {
            throw new IllegalStateException("Device must have a parent!");
        }

        if (this.inSimulation()) {
            //ignore due to simulation
            return;
        }

        i2cRef = SensorHubI2CConnection.getOrCreateConnection(
            SensorHubI2CConnection.generateId(bus, address), bus, address
        );

        this.pin = pin;
    }

What the `getOrCreateConnection` does is look at all of our previous connections and see if there is one that matches the bus and address.
If there is, it returns that connection. If there isn't, it creates a new connection and returns that. It's so we don't have multiple connections to the same bus and address.

We then want to make a method called `turnOn` that will turn the light on, and a respective `turnOff` method.

::
    
    public void turnOn() {
        if (this.inSimulation()) {
            //ignore due to simulation
            return;
        }

        i2cRef.writeToPin(pin, SensorHubI2CConnection.HIGH);
    }

    public void turnOff() {
        if (this.inSimulation()) {
            //ignore due to simulation
            return;
        }

        i2cRef.write(pin, SensorHubI2CConnection.LOW);
    }


The `writeToPin` method writes a value to a specific pin on the Arduino, and the `HIGH` and `LOW` values are constants that represent the HIGH value in Ardunio and the LOW value in Arduino.
We can then use these values to turn the light on and off!

Step 4: Implementing the Light
------------------------------

Now that we have the functionality for the light, we can implement it in the `SimpleBot` class.

We can start off by creating a new instance of the `Light` class in the `SimpleBot` class.

.. code-block:: java

    private Light light;

    public SimpleBot() {
        super();
    }

    /**
    * This method is called once when the robot turns on.
    * This is where you should set up all of your devices.
    */
    @Override
    protected void setup() {
        // Create a new light on bus 1, with the SensorHub I2C address at 0x12
        light = new Light(1, 0x12);
        
        //Here, we're setting the parent of every single device on the robot to the robot itself.
        getDevices().forEach(device -> {
            device.setParent(this);
        });

        //Finally, we're connecting the light to some pin (we'll say pin 12) here:
        light.connectToI2C(12);
    }

And, we can use the built-in clock class to turn the light on and off every second.
This is through the `clock()#elapsedSince` method, which returns if the given time has elapsed since the last time the method was activated.
(If 1000 milliseconds has elapsed since that method returned true (if you pass in `1000`), then the method will return true again, then the method will return true again in another 1000 milliseconds.)

.. code-block:: java

    private boolean currentlyOn = false;

    /**
    * This method is called continously while the robot is on.
    * This is where you should put your main code.
    */
    @Override
    protected void loop() {
        //This if-statement will become true/will run every 1000 milliseconds
        if (clock().elapsedSince(1000)) {
            //Check if the light is currently on, and turn it off if it is, and vice versa
            if (currentlyOn) {
                light.turnOff();
            } else {
                light.turnOn();
            }
            
            //Set the currentlyOn variable to the opposite of what it currently is
            currentlyOn = !currentlyOn;
        }
    }


Step 5: Uploading the Arduino Code
----------------------------------

To upload the Arduino code, we want to take the code found `here <https://github.com/JaidenAGrimminck/bot/blob/main/controlling/arduino-scripts/readwriter/readwriter.ino>`_ and open it up in the Arduino IDE.

We'll want to change a few things before we upload it, though.

We want to set `NUM_DEVICES` to `1`, `NUM_SERVOS` to `0`, and `PING_DEVICES` to `0`.

We want to make sure `I2C_ADDRESS` is the same as the one we set in the code (`0x12`), and make sure `PRIORITIZE_EEPROM_I2C` is set to `false`.

If you scroll down further, set `IGNORE_EEPROM` to `true`, then set `EEPROM_SIM` to the following:

.. code-block:: java

    byte EEPROM_SIM[] = {
        //-- general --
        I2C_ADDRESS, // i2c address
        NUM_DEVICES, // # of devices
        0, 0, 0, 0, 0, 0, //reserved
        //-- devices --
        0x03, 0x01, 0x0C, 0x01 //led device at port 12
    };


This is sort of a registery for all of our devices, stored in a data-efficient way.
The first 8 bytes are reserved, with the first two set to the I2C address of the microcontroller and the second to the number of devices attached to the bot.

After those 8 bytes is where the devices are assigned. The first byte is how many of the next few bytes the device takes up (in this case, 3 bytes).
The second byte is the device type (0x01 is a pin output/input) and the third byte is the pin the device is attached to (0x0C is 12 in decimal).
Finally, the last byte is the pin mode (0x01 is output).

The program will interpret this data and set up the devices accordingly.

Now, we can upload the code to the Arduino!

Step 6: Running the Code
------------------------

If everything is wired correctly, you can run the controlling code on the Raspberry Pi with:

.. code-block:: console

    (~/bot/controlling) $ ./gradlew run
    
The light should turn on and off every second!

If it doesn't, check the wiring and the code to make sure everything is correct.
You can check the Serial Monitor in the Arduino IDE to see if the Arduino is receiving the messages from the Raspberry Pi, or if there are any errors.

If you have any questions, feel free to ask me via creating an `issue <https://github.com/JaidenAGrimminck/bot/issues>`_.
