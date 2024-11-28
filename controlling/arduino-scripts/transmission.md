Test results from transmission tests in Serial.

Based on my results, I've deemed that RETURNING in Serial is UNRELIABLE and SHOULD NOT BE USED WITH RAW BYTES.

Baed on my tests, random raw bytes (I'm not 100% the source, maybe noise or something?) are left in the buffer, leading to it
being random buffer sizes and therefore unreliable. The amount of bytes that could be in a buffer
can range anywhere from ~4 to ~7 bytes, based on my test.

Additionally, I've found the problem for random stops in Serial in previous programs.

The reason was that on converting one of these unreliable doubles, an issue occurs in the conversion process, leading to potentially a memory leak or a buffer overflow.

or something like that. It's a *really, really* weird error.

If I loop through the first four bytes, this error occurs and stops. But, if I loop through and past the first four bytes, it works fine (huh?).

Additionally, alignment seems semi-important, but not necessarily. I've found that Serial is usually quite reliable on positioning (there's prob an internal protocol that's used to align immediately at the beginning.)

What's interesting is that it's usually the last 4 bytes that are reliable... let me do some tests based on that.

Odd. I replugged it back in, and suddenly it's super reliable. Like, only 4 bytes are left per round (which is what I want) and it's super reliable. I'm not sure what happened, but it's working now.

Let me try adding in an initial random 4 bytes to see if it's still reliable.

No, yeah, totally working. That's super odd.

# Conclusion

I'm not sure what happened, but it's working now. I'm not sure if it's a hardware issue or a software issue, but it's working now. I'll keep an eye on it, but it's working now.

I think it actually might be an issue with not plugging in the cable fully? It would explain the random bytes left in the buffer due to noise.

Some potential things I can do:
- Add data protection, as in parity bits or something.
- Add a checksum to the end of the data.
- Add a start and end byte to the data.
- Add a timeout to the data.
- Add a buffer size to the data.

Etc.