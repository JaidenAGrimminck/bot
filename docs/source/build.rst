Build Process
=============

.. _build-process:

Build Overview
--------------

This bot is supposed to be a sort of "capstone" of all of the robotics and coding that I've done throughout my coding career. For a bit about myself, I've been doing coding for a while now. I started back in about 1st or 2nd grade with Hour of Code, and I've been hooked ever since. As I went into high school, I got involved with FIRST. If you're a high school student (or even middle school!) and your school has a FIRST program, like FIRST Tech Challenge and FIRST Robotics Challenge, absolutely join. These are really valuable experiences that'll teach you the many skills you'll need to be successful in robotics. I co-founded a FIRST Robotics team in 9th grade and continued doing it in 10th. When I moved, I switched to FIRST Tech Challenge, and each one I've really expanded my skill set.

I originally wanted to do this project for the coding side, but the mechanical side definately was it own beast. The reason why I bring up FIRST is that the `controlling` code of the bot was inspired by that code base. This should hopefully make it easy to get used to if you're used to FTC/FRC.

First Stage: Prototyping!
-------------------------

When I started this project, I wanted to make sure that I had a prototype for the project, just as a proof-of-concept/I'm-able-to-actually-do-something-if-I-scaled-it-up. I decided the prototype to be a Raspberry Pi connected to an Arduino that I could drive around:

[image of protoype sketch etc]

I had 4 servos lying around, so that was what I used to make it. Additionally, since at the time I wanted to really get into the electronics, I ended up wiring together the Raspberry Pi and the Arduino via I2C, leading to this robot below:

[robot]

(by the way, this is similar to the robot that's made in the `TODO: put something here`)
I strapped together a quick codebase using Python to communicate with the Arduino, with a frontend hosted with Flask. My first mistake was using a REST API for commumicating the controls that were on the frontend. They "sort of" worked, but when operating the bot there were clear issues. It was quicker to setup the REST API compared to working towards making the websocket, which is justified to do so since this is a proof-of-concept, not a fully fleshed out bot.

Once everything was working, I started on the CAD.

Second Stage: CAD
-----------------

I started off by working on the CAD for the bot, going through a few different iterations. I started off with an idea to make it have shock absorption, but I quickly realized that this was out of my manufacturing capabilities, and for a first version I didn't need it to be super complex, just needed it to work.

[image]

For the second version, I abandoned the shock absorption but rather planned to get some holders for the hoverboard wheels milled out. As I was building later on, I realized that this would be a pain, and rather I had alternatives that could speed up the process.

[image 2]

For the current version, I decided to use the aluminum cutouts of the hoverboard case to hold the wheels and go with a pretty basic version of holding the RHS together, then use 3d printed mounts for holding the second layer.

[image 3]


Third Stage: Building
---------------------

This process took *a while*. I initially began with getting two hoverboards; I got one from Marktplaats and the other was donated from my school. I took them apart and salvaged the hoverboard wheels themselves.

[taking apart hoverboard]

I also ordered some aluminum RHS and plate from a place in Belgium. I originally wanted 2mm thickness for both, but they ended up sending me 3mm thick plate (they had emailed me beforehand; I said it was fine -- which was a mistake haha). I also rented a chop saw from a local store and got a blade to cut it. It went pretty well, but the 3mm plate ended up making a ton of sparks.

[chopping?]

Using a teacher at school's angle grinder, I was able to chop up my hoverboard's aluminum skeleton leaving only the end bits.

[skeleton]

Then, finally, I got a wooden plate to put on top of the bot to mount all the electronics that I would need for the bot.

Fourth Stage: Coding and Testing
--------------------------------

In the middle of this process, I needed to start getting the code together for the bot.

I decided to split this up into 3 different codebases:
1. `controlling`, the main codebase that controls the robot.
2. `site`, the front end website that's used for sharing data.
3. The Arduino code that lives on the Arduino microcontrollers.

I begun with #2, making the front end website. I'm used to FIRST's Simulation display/8840-app (a dashboard I made for FRC), so I decided to start make something similar to that. At the base level, I wanted to make a site that's like React yet lightweight, without the 1gb+ worth of packages (it comes out to approx ~15mb of packages at the current moment). Thus, I built up the site to be able to load in "modules" fast and easily.

Thus, I started off with the idea of "panels." I had a few different ideas for the beginning:

[panels screenshots]

I started with a REST API, but this was pretty slow. I then shifted any constant communication to a websocket, boosting the performance once again.

>>> Network Protocols

After I had something for the site, I needed a way to communicate with the `controlling` code. This led to me starting off with creating the network protocol.

The protocol works as follows:

Each client, once connected, needs to declare their "status": a listener, a speaker, or a passive client. Each client has direct access do a different and shared methods depending on their status. A listener is primarly meant for "listening" to different pieces of data, a speaker is primarly meant for "speaking" to the controller with data, and a passive client can do both. By becoming a speaker/listener, the client can save a couple of bytes each transmission, which can add up.

Through the protocol, there's a series of options to "subscribe" to different data sources, allowing for data to be sent without having to request it. This again saves many bytes, optimizing the network communication.

This is also the key to successful cross-platform communication, allowing for in the current program, allow for the AI using Python packages to communicate with the `controlling` code in Java, which the `site` can therefore read and display.

For any bigger data requests, there also exists a REST API which can share more one-time requests to the client.

>>> training AI

Once the protocol was built, I started by building a simulation, with the environment in Java and the neural network in Python using Tensorflow. This was slightly convoluted, but it worked well, and in theory would be able to work on the bot without even having to change the code from the simulator to the actual bot (I never did this, as I'll explain later).

I planned out the bot to have 3 ultrasonic sensors, and I had to expand the code to support multiple instances of robots being made at once, leading to this:

[image]

The environment was generated using an image I would draw then run through a program to convert it into a race course.

As I was training, there were two main faults:
1. It's slow
2. It's really really slow

Yeah, it was slow. So, I decided to replicate the entire simulation environment in Python. Though, the program was still slow. As I was doing some investigating, I found that Tensorflow was the biggest [culprit] for the slowdown. Through this, I made the decision to create my own custom network in Python. This sped up the program tremendously, but I still had an issue where the program would not learn. After a little bit of snooping and research, I found that a problem lay with how the network made the decision. I had it setup that there were three final neurons that, if activated, would do some action, but I shifted it into two final neurons, with each having a direct impact on speed/rotation.

I was then able to train it and get results in real-time, which was exciting:

[video]

Then, I was able to go back into the simulator in Java and simulate a bot driving around the course, pretty much using the setup I had before:

[video]

Fifth Stage: Electronics
------------------------

...still working on this!