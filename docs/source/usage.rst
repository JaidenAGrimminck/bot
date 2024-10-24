Usage
=====

.. _installation:

Prerequisites
-------------

To use the project, first you need a few prerequisites:

- Git
- `Python 3.12 or higher <https://www.python.org/>`_
- `Java 20 or higher <https://www.java.com/download/>`_
- `NodeJS 20 or higher <https://nodejs.org/en/download/>`_ (with a compatible version of npm)

And if you're actually planning to build this:

- `Arduino IDE <https://www.arduino.cc/en/software>`_
- A compatible microcontroller
- Raspberry Pi (that can run Java 20, Python 3.12, etc)

Installation
------------

Once you have all of these installed, you can install the project with:

.. code-block:: console

   (~) $ git clone https://github.com/JaidenAGrimminck/bot.git
   (~) $ cd bot

Controlling Code Setup
----------------------

To get started on the controlling code, we can go ahead and do the following steps:

.. code-block:: console

   (~/bot) $ cd controlling
   (~/bot/controlling) $ ./gradlew clean
   (~/bot/controlling) $ ./gradlew build

This will build the controlling code and prepare it for deployment.

To continue learning how to use the controlling code, check out the :ref:`controlling_get_started` section.
