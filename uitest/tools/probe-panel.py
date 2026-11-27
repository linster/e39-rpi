#!/usr/bin/env python3
"""A window of buttons for the HMI's test probe: the knob, navigation, resets,
GPS position, crashing the HMI (#78), debug windows, and a live picture of the
HMI window. For poking at a running HMI by hand: on a Pi or in QEMU over ssh,
or a local HMI started with E39_TEST_PROBE.

    tools/probe-panel.py --device e39pi.local    # a Pi (ssh as root)
    tools/probe-panel.py --qemu                  # run-qemu.sh's QEMU (ssh port 2222)
    tools/probe-panel.py --address 127.0.0.1:50199   # a probe already reachable

On a Pi or in QEMU it does what the UI tests' remote target does: it adds
E39_TEST_PROBE to /etc/default/e39rpi-hmi (restarting the HMI only if it wasn't
there yet, or with --restart) and tunnels the probe's port over ssh. The HMI
there must be a build with the probe RPCs used (CrashHmi: #78 or later).
"Probe off" takes E39_TEST_PROBE out again and restarts the HMI.

Run it from uitest/ or anywhere: it finds e39ui next to it.
"""

import argparse
import base64
import os
import queue
import re
import sys
import tempfile
import threading
import time
import tkinter as tk
from tkinter import messagebox, ttk

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import grpc  # noqa: E402

from e39ui.hmi import Hmi, pb2  # noqa: E402
from e39ui.targets.remote import ENV_FILE, RESTART, RemoteTarget, qemu_state  # noqa: E402

ERROR_SCREEN = "E39 HMI Error"
POLL_S = 1.0


def describe(e):
    """An exception as one line: a gRPC error's status and details."""
    if isinstance(e, grpc.RpcError):
        return f"{e.code().name}: {e.details()}"
    return f"{type(e).__name__}: {e}"


class Panel:
    def __init__(self, root, args):
        self.root = root
        self.args = args
        self.hmi = None
        self.target = None
        self.ui = queue.Queue()        # callables to run on the Tk thread
        self.actions = queue.Queue()   # (label, fn) for the worker thread
        self.live = tk.BooleanVar(value=True)
        self.photo = None
        self.polling = False
        self.connected_once = False

        root.title(f"E39 probe panel: {self.where()}")
        self.build()
        threading.Thread(target=self.worker, daemon=True).start()
        self.root.after(50, self.drain)
        self.run("Connect", self.connect)

    # --- threads ----------------------------------------------------------

    def where(self):
        a = self.args
        return a.address or (f"QEMU (ssh port {a.ssh_port or 2222})" if a.qemu else a.device)

    def on_ui(self, fn):
        self.ui.put(fn)

    def drain(self):
        while True:
            try:
                self.ui.get_nowait()()
            except queue.Empty:
                break
        self.root.after(50, self.drain)

    def run(self, label, fn):
        """Runs fn() on the worker thread (one action at a time, in order), logging the outcome."""
        self.actions.put((label, fn))

    def worker(self):
        while True:
            label, fn = self.actions.get()
            self.log(f"{label}...")
            try:
                result = fn()
                self.log(f"{label}: {result}" if isinstance(result, str) and result else f"{label}: done")
            except Exception as e:
                self.log(f"{label} failed: {describe(e)}")

    def poller(self):
        """The HMI's state (and picture, with Live) every POLL_S, on a thread of its own."""
        while True:
            try:
                s = self.hmi.state()
                self.on_ui(lambda s=s: self.show_state(s))
                if self.live.get():
                    png = self.hmi.screenshot_png(ERROR_SCREEN if self.error_screen_showing() else "")
                    self.on_ui(lambda png=png: self.show_picture(png))
            except Exception as e:
                self.on_ui(lambda e=e: self.status.set(f"No state: {describe(e)}"))
            time.sleep(POLL_S)

    def error_screen_showing(self):
        try:
            self.hmi.texts(ERROR_SCREEN)
            return True
        except grpc.RpcError:
            return False

    def log(self, line):
        def append():
            self.log_box.configure(state="normal")
            self.log_box.insert("end", time.strftime("%H:%M:%S ") + line + "\n")
            self.log_box.see("end")
            self.log_box.configure(state="disabled")
        self.on_ui(append)

    # --- connecting -------------------------------------------------------

    def disconnect(self):
        if self.hmi:
            self.hmi.close()
            self.hmi = None
        if self.target and self.target.tunnel and self.target.tunnel.poll() is None:
            self.target.tunnel.terminate()
        self.target = None

    def connect(self):
        """Connects (again): a new ssh tunnel and channel. Only the first connect restarts the HMI with --restart."""
        self.disconnect()
        a = self.args
        restart = a.restart and not self.connected_once
        if a.address:
            self.hmi = Hmi(a.address)
        else:
            out = tempfile.mkdtemp(prefix="e39-probe-panel-")
            if a.qemu:
                state = qemu_state(a.qemu_state)
                self.target = RemoteTarget(out, "localhost", ssh_port=a.ssh_port or 2222, probe_port=a.probe_port,
                                           restart=restart, ibus_socket=state.get("IBUS"))
            else:
                self.target = RemoteTarget(out, a.device, ssh_port=a.ssh_port or 22, probe_port=a.probe_port,
                                           restart=restart)
            self.log(f"ssh to {self.where()}: turning the probe on (the HMI restarts if it was off)")
            self.target.start()
            self.hmi = Hmi(self.target.address)
            self.target.wait_ready(self.hmi)
        self.hmi.state()
        self.connected_once = True
        if not self.polling:
            self.polling = True
            threading.Thread(target=self.poller, daemon=True).start()
        self.run("List screens", self.load_screens)
        self.run("List debug windows", self.load_debug_windows)
        return f"connected to {self.where()}"

    def probe_off(self):
        if not self.target:
            return "not over ssh: nothing to turn off"
        self.target.ssh(f"sed -i '/^E39_TEST_PROBE=/d' {ENV_FILE}")
        self.target.ssh(RESTART)
        return "E39_TEST_PROBE removed; the HMI restarts without the probe (this panel stops working)"

    def load_screens(self):
        registered, _ = self.hmi.list_screens()
        names = sorted({n.rsplit(".", 1)[-1] for n in registered})
        self.on_ui(lambda: self.screen_box.configure(values=names))
        return f"{len(names)} screens"

    def load_debug_windows(self):
        # The probe lists the titles when asked for one that isn't there.
        try:
            self.hmi.open_debug_window("?")
            return "couldn't list them"
        except grpc.RpcError as e:
            titles = re.findall(r'"([^"]+)"', (e.details() or "").partition("there are:")[2])
        self.on_ui(lambda: self.debug_box.configure(values=titles))
        return f"{len(titles)} windows"

    # --- the window ---------------------------------------------------------

    def build(self):
        r = self.root
        left = ttk.Frame(r, padding=6)
        left.grid(row=0, column=0, sticky="ns")
        right = ttk.Frame(r, padding=6)
        right.grid(row=0, column=1, sticky="nsew")
        r.columnconfigure(1, weight=1)
        r.rowconfigure(0, weight=1)

        def group(title):
            f = ttk.LabelFrame(left, text=title, padding=4)
            f.pack(fill="x", pady=3)
            return f

        def buttons(parent, *specs):
            row = ttk.Frame(parent)
            row.pack(fill="x", pady=1)
            for text, fn in specs:
                ttk.Button(row, text=text, command=fn).pack(side="left", padx=1)

        h = lambda: self.hmi  # noqa: E731

        knob = group("Knob")
        buttons(knob, ("◀ Left", lambda: self.run("Left", lambda: h().left() and None)),
                ("Press", lambda: self.run("Press", lambda: h().press() and None)),
                ("Right ▶", lambda: self.run("Right", lambda: h().right() and None)))
        buttons(knob, ("Menu", lambda: self.run("Menu", lambda: h().menu() and None)),
                ("⏮ Prev", lambda: self.run("Prev track", lambda: h().prev_track() and None)),
                ("Next ⏭", lambda: self.run("Next track", lambda: h().next_track() and None)))

        nav = group("Navigate")
        buttons(nav, ("Back", lambda: self.run("Back", lambda: h().back() and None)),
                ("Root", lambda: self.run("Root", lambda: h().go_root() and None)),
                ("Close overlays", lambda: self.run("Close overlays", lambda: h().close_overlays() and None)),
                ("Clear notification", lambda: self.run("Clear notification",
                                                        lambda: h().clear_notification() and None)))
        row = ttk.Frame(nav)
        row.pack(fill="x", pady=1)
        self.screen_box = ttk.Combobox(row, width=34)
        self.screen_box.pack(side="left")
        ttk.Button(row, text="Go", command=lambda: self.go(self.screen_box.get())).pack(side="left", padx=2)

        hmi = group("HMI")
        buttons(hmi, ("Crash the HMI…", self.crash),
                ("Reopen window", lambda: self.run("Reopen window", lambda: h().reopen_window() and None)))
        buttons(hmi, ("Reconnect", lambda: self.run("Reconnect", self.connect)),
                ("Probe off…", self.confirm_probe_off))

        resets = group("Reset")
        buttons(resets, ("Mail", lambda: self.run("Reset mail", lambda: h().reset_mail_app())),
                ("Contacts", lambda: self.run("Reset contacts", lambda: h().reset_contacts_app())),
                ("Accounts", lambda: self.run("Reset accounts", lambda: h().reset_accounts())),
                ("Guidance", lambda: self.run("Reset guidance", lambda: h().reset_guidance())))

        gps = group("GPS position (FakeLocationService)")
        row = ttk.Frame(gps)
        row.pack(fill="x")
        self.lat, self.lng = tk.StringVar(value="51.0447"), tk.StringVar(value="-114.0719")
        ttk.Entry(row, textvariable=self.lat, width=11).pack(side="left")
        ttk.Entry(row, textvariable=self.lng, width=11).pack(side="left", padx=2)
        ttk.Button(row, text="Set", command=self.set_position).pack(side="left")

        typing = group("Type on the on-screen keyboard")
        row = ttk.Frame(typing)
        row.pack(fill="x")
        self.text = tk.StringVar()
        ttk.Entry(row, textvariable=self.text, width=28).pack(side="left")
        ttk.Button(row, text="Type", command=lambda: self.run(
            "Type", lambda t=self.text.get(): h().type_text(t) and None)).pack(side="left", padx=2)

        mail = group("Deliver sample mail")
        self.subject = tk.StringVar(value="Hello from the probe panel")
        ttk.Entry(mail, textvariable=self.subject, width=36).pack(fill="x")
        buttons(mail, ("Deliver", lambda: self.run(
            "Deliver mail", lambda s=self.subject.get(): h().deliver_mail(subject=s, text=s, from_name="Probe Panel",
                                                                          from_email="probe@example.com"))))

        debug = group("Debug windows")
        row = ttk.Frame(debug)
        row.pack(fill="x")
        self.debug_box = ttk.Combobox(row, width=26)
        self.debug_box.pack(side="left")
        ttk.Button(row, text="Open", command=lambda: self.run(
            "Open", lambda t=self.debug_box.get(): h().open_debug_window(t))).pack(side="left", padx=1)
        ttk.Button(row, text="Close", command=lambda: self.run(
            "Close", lambda t=self.debug_box.get(): h().close_debug_window(t))).pack(side="left", padx=1)

        # Right: the picture, the state, the log.
        top = ttk.Frame(right)
        top.pack(fill="x")
        ttk.Checkbutton(top, text="Live picture", variable=self.live).pack(side="left")
        ttk.Button(top, text="Save screenshot", command=self.save_screenshot).pack(side="left", padx=4)
        self.picture = ttk.Label(right, text="(no picture yet)", anchor="center")
        self.picture.pack(fill="both", expand=True)
        self.status = tk.StringVar(value="Connecting...")
        ttk.Label(right, textvariable=self.status, justify="left", font=("TkFixedFont", 9)).pack(fill="x", pady=4)
        self.log_box = tk.Text(right, height=10, state="disabled", font=("TkFixedFont", 9))
        self.log_box.pack(fill="both")

    def show_state(self, s):
        lines = [
            f"screen: {s.screen}   window open: {s.hmi_window_open}   theme: {s.theme}",
            f"back stack: {' < '.join(s.back_stack) or '(empty)'}",
            f"overlays: side pane {s.side_pane_open}, modal menu {s.modal_menu_open}, keyboard {s.keyboard_open}",
        ]
        if s.notification.visible:
            lines.append(f"notification: {s.notification.top_text}")
        self.status.set("\n".join(lines))

    def show_picture(self, png):
        self.photo = tk.PhotoImage(data=base64.b64encode(png))
        self.picture.configure(image=self.photo, text="")

    # --- actions --------------------------------------------------------------

    def go(self, screen):
        if screen:
            self.run(f"Go to {screen}", lambda: self.hmi.go(screen) and None)

    def crash(self):
        if messagebox.askyesno("Crash the HMI", f"Make the HMI window on {self.where()} throw?\n\n"
                                                "With the error screen on (the Pi), it shows the error, then "
                                                "the main menu; a second crash within 2 minutes restarts the HMI."):
            self.run("Crash the HMI", lambda: self.hmi.crash_hmi())

    def confirm_probe_off(self):
        if messagebox.askyesno("Probe off", f"Remove E39_TEST_PROBE on {self.where()} and restart the HMI?"):
            self.run("Probe off", self.probe_off)

    def set_position(self):
        try:
            lat, lng = float(self.lat.get()), float(self.lng.get())
        except ValueError:
            self.log("Set position: latitude and longitude must be numbers")
            return
        self.run(f"Set position {lat}, {lng}", lambda: self.hmi.set_position(lat, lng) and None)

    def save_screenshot(self):
        def save():
            path = os.path.join(os.getcwd(), time.strftime("hmi-%Y%m%d-%H%M%S.png"))
            with open(path, "wb") as f:
                f.write(self.hmi.screenshot_png())
            return path
        self.run("Screenshot", save)

    def close(self):
        self.disconnect()
        self.root.destroy()


def main():
    p = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    where = p.add_mutually_exclusive_group(required=True)
    where.add_argument("--device", help="a Pi's host name or address (ssh as root)")
    where.add_argument("--qemu", action="store_true", help="the QEMU run-qemu.sh started")
    where.add_argument("--address", help="a probe already reachable, host:port")
    p.add_argument("--ssh-port", type=int, default=None, help="default 22, or 2222 for --qemu")
    p.add_argument("--qemu-state", default=None, help="run-qemu.sh's state file (default: the only one)")
    p.add_argument("--probe-port", type=int, default=50199, help="the probe's port on the device")
    p.add_argument("--restart", action="store_true", help="restart the HMI even if the probe is already on")
    args = p.parse_args()

    root = tk.Tk()
    panel = Panel(root, args)
    root.protocol("WM_DELETE_WINDOW", panel.close)
    root.mainloop()


if __name__ == "__main__":
    main()
