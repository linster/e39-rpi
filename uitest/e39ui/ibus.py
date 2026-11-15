"""IBus packets for the BMBT knob and buttons (the HMI's IBusInputMessageParser).

Packet: source, length (data + 2), destination, data..., XOR of all before.
The same bytes as e39-rpi-yocto's scripts/hmi.py.
"""

import socket
import time

BMBT, NAV_VM, RADIO, BROADCAST = 0xF0, 0x3B, 0x68, 0xFF


def packet(src, dst, data):
    body = bytes([src, len(data) + 2, dst]) + bytes(data)
    xor = 0
    for b in body:
        xor ^= b
    return body + bytes([xor])


def knob_turn(right, clicks=1):
    if not 1 <= clicks <= 9:
        raise ValueError("clicks: 1..9")
    return [packet(BMBT, NAV_VM, [0x49, (0x80 if right else 0x00) | clicks])]


# Press: push, then release (like the real BMBT).
KNOB_PRESS = [packet(BMBT, NAV_VM, [0x48, 0x05]), packet(BMBT, NAV_VM, [0x48, 0x85])]
MENU = [packet(BMBT, BROADCAST, [0x48, 0x34]), packet(BMBT, BROADCAST, [0x48, 0xB4])]
NEXT_TRACK = [packet(BMBT, RADIO, [0x48, 0x00])]
PREV_TRACK = [packet(BMBT, RADIO, [0x48, 0x10])]


class IBusWriter:
    """Writes packets to the HMI's IBus: a file (a pty's other end) or a unix socket (QEMU's ibus.sock)."""

    def __init__(self, path, is_socket=False, gap=0.08):
        self.gap = gap
        if is_socket:
            self._sock = socket.socket(socket.AF_UNIX)
            self._sock.connect(path)
            self._write = self._sock.sendall
            self._close = self._sock.close
        else:
            f = open(path, "wb", buffering=0)
            self._write = f.write
            self._close = f.close

    def send(self, packets):
        for p in packets:
            self._write(p)
            time.sleep(self.gap)

    def close(self):
        self._close()
