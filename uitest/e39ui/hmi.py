"""Driving the HMI through its test probe (and, optionally, real IBus packets)."""

import os
import re
import time
import warnings

import grpc

from . import ibus
from .proto import load

pb2, pb2_grpc = load()


class HmiError(AssertionError):
    pass


class Hmi:
    """One HMI with a test probe at `address` ("127.0.0.1:port").

    Input goes through the probe (default: each event waits until the HMI
    has settled) or, with `ibus_writer`, as IBus packets (the car's path;
    the HMI may drop turns while it redraws, see TODO.md).
    """

    def __init__(self, address, ibus_writer=None, timeout_s=15):
        self.address = address
        self.channel = grpc.insecure_channel(address)
        self.stub = pb2_grpc.HmiTestProbeStub(self.channel)
        self.ibus = ibus_writer
        self.timeout_s = timeout_s
        self.out_dir = None
        self._shots = 0

    # --- state -----------------------------------------------------------

    def state(self):
        return self.stub.GetState(pb2.GetStateRequest(), timeout=self.timeout_s)

    def wait_idle(self, quiet_ms=300, timeout_ms=10000, from_now=False):
        return self.stub.WaitForIdle(pb2.WaitForIdleRequest(quiet_ms=quiet_ms, timeout_ms=timeout_ms, from_now=from_now),
                                     timeout=timeout_ms / 1000 + 5)

    def wait_for(self, check, what, timeout_s=10):
        """Polls the state until check(state) is true; HmiError with the last state otherwise."""
        deadline = time.time() + timeout_s
        while True:
            s = self.state()
            if check(s):
                return s
            if time.time() > deadline:
                raise HmiError(f"Timed out waiting for {what}; now on {s.screen} "
                               f"(notification: {s.notification.top_text!r} visible={s.notification.visible}, "
                               f"side pane={s.side_pane_open}, keyboard={s.keyboard_open})")
            time.sleep(0.1)

    def wait_for_screen(self, screen, timeout_s=10):
        s = self.wait_for(lambda s: s.screen == screen, f"screen {screen}", timeout_s)
        return self.wait_idle()

    def expect_screen(self, screen):
        s = self.state()
        if s.screen != screen:
            raise HmiError(f"Expected screen {screen}, but on {s.screen}")
        return s

    def wait_for_notification(self, top_text_pattern, timeout_s=10, clear=True):
        """Waits for a notification whose top text matches; then hides it (`clear`), since it
        covers the top of the screen until it times out. Returns the state with it."""
        s = self.wait_for(lambda s: s.notification.visible and re.search(top_text_pattern, s.notification.top_text),
                          f"a notification matching {top_text_pattern!r}", timeout_s)
        if clear:
            self.clear_notification()
        return s

    def clear_notification(self):
        self.stub.ClearNotification(pb2.ClearNotificationRequest(), timeout=self.timeout_s)
        return self.wait_idle(from_now=True)

    # --- input -------------------------------------------------------------

    def _send(self, events, ibus_packets):
        if self.ibus:
            self.wait_idle()
            self.ibus.send(ibus_packets)
            return self.wait_idle(from_now=True)
        r = self.stub.SendInput(pb2.SendInputRequest(events=events, wait_for_idle=True), timeout=self.timeout_s * 4)
        if r.dropped:
            raise HmiError(f"{r.dropped} input(s) lost (nothing listening) on {r.state.screen}")
        return r.state

    def right(self, clicks=1):
        return self._turn(True, clicks)

    def left(self, clicks=1):
        return self._turn(False, clicks)

    def _turn(self, right, clicks):
        d = pb2.KnobTurn.RIGHT if right else pb2.KnobTurn.LEFT
        state = None
        for _ in range(clicks):
            state = self._send([pb2.InputEvent(knob_turn=pb2.KnobTurn(direction=d, clicks=1))], ibus.knob_turn(right, 1))
        return state

    def press(self):
        return self._send([pb2.InputEvent(knob_press=pb2.Empty())], ibus.KNOB_PRESS)

    def next_track(self):
        return self._send([pb2.InputEvent(next_track=pb2.Empty())], ibus.NEXT_TRACK)

    def prev_track(self):
        return self._send([pb2.InputEvent(prev_track=pb2.Empty())], ibus.PREV_TRACK)

    def menu(self):
        return self._send([pb2.InputEvent(menu=pb2.Empty())], ibus.MENU)

    def select(self, position):
        """Turns right `position` clicks from where the knob is, then presses."""
        if position:
            self.right(position)
        return self.press()

    # --- what's on screen ---------------------------------------------------

    def texts(self):
        """Everything with text on screen, top to bottom (ScreenText: text, x, y, width, height, clickable, selected)."""
        return list(self.stub.GetTexts(pb2.GetTextsRequest(), timeout=self.timeout_s).texts)

    def items(self):
        """The knob's positions: clickable texts, in order. With an overlay open (side pane,
        keyboard, modal menu), only its items: the top layer's."""
        items = [t for t in self.texts() if t.clickable]
        state = self.state()
        if items and (state.side_pane_open or state.keyboard_open or state.modal_menu_open):
            top = max(t.layer for t in items)
            items = [t for t in items if t.layer == top]
        return items

    def selected(self):
        """The text of the knob's current item, or None."""
        sel = [t.text for t in self.items() if t.selected]
        return sel[0] if sel else None

    def screen_text(self):
        return "\n".join(t.text for t in self.texts())

    def expect_text(self, pattern, present=True):
        """Asserts that some text on screen matches the regex `pattern`, ignoring case (or that none does)."""
        texts = [t.text for t in self.texts()]
        found = [t for t in texts if re.search(pattern, t, re.I)]
        if present and not found:
            raise HmiError(f"No text matching {pattern!r} on {self.state().screen}; texts: {texts}")
        if not present and found:
            raise HmiError(f"Unexpected {found} on {self.state().screen}")
        return found

    def select(self, pattern, press=True, ignore_case=True):
        """Moves the knob to the item whose text matches the regex `pattern` (ignoring case; the
        first one), then presses.

        Turns by the distance in screen order, then checks which item is highlighted; where the
        knob's order isn't the screen's (e.g. the main menu's quadrants), steps one click at a
        time until it is."""
        def where():
            items = self.items()
            names = [t.text for t in items]
            flags = re.I if ignore_case else 0
            target = next((i for i, t in enumerate(names) if re.search(pattern, t, flags)), None)
            if target is None:
                raise HmiError(f"No item matching {pattern!r} on {self.state().screen}; items: {names}")
            current = next((i for i, t in enumerate(items) if t.selected), None)
            if current is None:
                state = self.state()
                if state.notification.visible:
                    raise HmiError(f"No item is highlighted on {state.screen}: a notification "
                                   f"({state.notification.top_text!r}) covers the screen (wait_for_notification "
                                   f"or clear_notification first); items: {names}")
                raise HmiError(f"No item is highlighted on {state.screen}; items: {names}")
            return items, target, current


        items, target, current = where()
        if current != target:
            delta = target - current
            # The loop wraps: go the short way round.
            if abs(delta) > len(items) / 2:
                delta = delta - len(items) if delta > 0 else delta + len(items)
            self.right(delta) if delta > 0 else self.left(-delta)
            items, target, current = where()
        steps = 0
        while current != target:
            if steps > len(items) + 2:
                raise HmiError(f"Couldn't get the knob to {pattern!r}; it's on {items[current].text!r}")
            self.right()
            steps += 1
            items, target, current = where()
        return self.press() if press else self.state()

    def type_text(self, text, submit=True):
        """Types `text` on the open on-screen keyboard (QWERTY, or a grid like the telephone
        keyboard's) with the knob, then Return (`submit`).

        Starts from an empty or prefilled field: clear it first with clear_text() if needed."""
        from .keyboard import key_for
        if not self.state().keyboard_open:
            raise HmiError("The keyboard isn't open")
        for c in text:
            if c == " ":
                # The space bar has no label.
                self.select(r"^$", ignore_case=False)
                continue
            keys = self.items()
            if any(t.text == c for t in keys):
                # A key shows exactly this (digits, the telephone keyboard's keys).
                self.select("^" + re.escape(c) + "$", ignore_case=False)
                continue
            label, upper = key_for(c)
            showing_upper = any(t.text == "Q" for t in keys)
            if upper != showing_upper:
                self.select(r"^Shift$", ignore_case=False)
            self.select("^" + re.escape(label) + "$", ignore_case=False)
        if submit:
            self.select(r"^Return$", ignore_case=False)
            self.wait_for(lambda s: not s.keyboard_open, "the keyboard to close")
        return self.wait_idle(from_now=True)

    def clear_text(self, length):
        """Deletes `length` characters before the keyboard's cursor (Backspace)."""
        for _ in range(length):
            self.select(r"^Backspace$", ignore_case=False)

    # --- navigation and app state -----------------------------------------------------

    def go(self, screen):
        self.stub.Navigate(pb2.NavigateRequest(screen=screen), timeout=self.timeout_s)
        return self.wait_for_screen(screen)

    def go_root(self):
        self.stub.Navigate(pb2.NavigateRequest(root=pb2.Empty()), timeout=self.timeout_s)
        return self.wait_idle(from_now=True)

    def back(self):
        self.stub.Navigate(pb2.NavigateRequest(back=pb2.Empty()), timeout=self.timeout_s)
        return self.wait_idle(from_now=True)

    def list_screens(self):
        """(registered, unregistered): full class names of the screens the navigator knows, and of
        @AutoDiscover screens missing from Navigator.kt."""
        r = self.stub.ListScreens(pb2.ListScreensRequest(), timeout=self.timeout_s)
        return list(r.registered), list(r.unregistered)

    def close_overlays(self):
        self.stub.CloseOverlays(pb2.CloseOverlaysRequest(), timeout=self.timeout_s)
        return self.wait_idle(from_now=True)

    def reset_mail_app(self):
        self.stub.ResetMailApp(pb2.ResetMailAppRequest(), timeout=self.timeout_s)

    def reset_accounts(self):
        self.stub.ResetAccounts(pb2.ResetAccountsRequest(), timeout=self.timeout_s)

    def reset_contacts_app(self):
        self.stub.ResetContactsApp(pb2.ResetContactsAppRequest(), timeout=self.timeout_s)

    def deliver_mail(self, subject="", text="", from_name="", from_email=""):
        self.stub.DeliverFakeMail(pb2.DeliverFakeMailRequest(
            subject=subject, text=text, from_name=from_name, from_email=from_email), timeout=self.timeout_s)

    # --- screenshots -------------------------------------------------------

    def screenshot_png(self):
        return self.stub.Screenshot(pb2.ScreenshotRequest(), timeout=self.timeout_s).png

    def shot(self, name):
        """Saves a screenshot as NN-name.png in this test's output folder; returns its path."""
        self._shots += 1
        path = os.path.join(self.out_dir or ".", f"{self._shots:02d}-{name}.png")
        with open(path, "wb") as f:
            f.write(self.screenshot_png())
        return path

    def start_test(self, out_dir):
        self.out_dir = out_dir
        self._shots = 0
        os.makedirs(out_dir, exist_ok=True)

    def close(self):
        if self.ibus:
            self.ibus.close()
        self.channel.close()
