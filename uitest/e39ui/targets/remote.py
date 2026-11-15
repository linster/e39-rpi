"""The HMI in QEMU (scripts/run-qemu.sh in e39-rpi-yocto) or on a Pi, over ssh.

The image runs the HMI as the hmi user's systemd user service, which exports
every variable in /etc/default/e39rpi-hmi. To test, the runner (as root over
ssh) adds E39_TEST_PROBE there, restarts the service, and tunnels the
probe's port (it listens on the device's 127.0.0.1 only). The HMI on the
device must be a build with the probe (this branch or later).
"""

import glob
import os
import subprocess
import time

from .local import free_port

ENV_FILE = "/etc/default/e39rpi-hmi"
RESTART = ("runuser -u hmi -- env XDG_RUNTIME_DIR=/run/user/$(id -u hmi) "
           "systemctl --user restart e39rpi-hmi.service")


class RemoteTarget:
    def __init__(self, out_dir, host, ssh_port=22, user="root", probe_port=50199, restart=True,
                 ibus_socket=None, startup_timeout_s=180):
        self.out_dir = out_dir
        self.host = host
        self.ssh_port = ssh_port
        self.user = user
        self.probe_port = probe_port
        self.restart = restart
        self.ibus_path = ibus_socket
        self.startup_timeout_s = startup_timeout_s
        self.tunnel = None
        self.address = None

    def _ssh_args(self):
        return ["ssh", "-p", str(self.ssh_port), "-o", "BatchMode=yes", "-o", "StrictHostKeyChecking=accept-new",
                f"{self.user}@{self.host}"]

    def ssh(self, command):
        r = subprocess.run(self._ssh_args() + [command], capture_output=True, text=True, timeout=60)
        if r.returncode != 0:
            raise RuntimeError(f"ssh {self.host}: {command}\n{r.stderr.strip()}")
        return r.stdout

    def start(self):
        os.makedirs(self.out_dir, exist_ok=True)
        line = f"E39_TEST_PROBE={self.probe_port}"
        changed = self.ssh(f"grep -qx '{line}' {ENV_FILE} && echo same || "
                           f"(sed -i '/^E39_TEST_PROBE=/d' {ENV_FILE}; echo '{line}' >> {ENV_FILE}; echo changed)").strip()
        if self.restart or changed == "changed":
            self.ssh(RESTART)
        local = free_port()
        self.tunnel = subprocess.Popen(
            self._ssh_args()[:1] + ["-N", "-L", f"{local}:127.0.0.1:{self.probe_port}"] + self._ssh_args()[1:],
            stdout=subprocess.DEVNULL, stderr=open(os.path.join(self.out_dir, "ssh-tunnel.log"), "wb"))
        self.address = f"127.0.0.1:{local}"

    def wait_ready(self, hmi):
        deadline = time.time() + self.startup_timeout_s
        while True:
            try:
                if hmi.state().hmi_window_open:
                    return
            except Exception:
                pass
            if self.tunnel.poll() is not None:
                raise RuntimeError(f"The ssh tunnel ended; see {self.out_dir}/ssh-tunnel.log")
            if time.time() > deadline:
                raise RuntimeError(f"No HMI with a test probe on {self.host} after {self.startup_timeout_s} s "
                                   f"(journalctl -t e39rpi-hmi on the device)")
            time.sleep(1)

    def save_log(self):
        try:
            log = self.ssh("journalctl -t e39rpi-hmi -b --no-pager -n 5000")
            with open(os.path.join(self.out_dir, "hmi.log"), "w") as f:
                f.write(log)
        except Exception as e:
            print(f"Couldn't fetch the HMI's log: {e}")

    def stop(self):
        self.save_log()
        if self.tunnel and self.tunnel.poll() is None:
            self.tunnel.terminate()


def qemu_state(path=None):
    """The running QEMU's state file (scripts/run-qemu.sh): ssh port is 2222; IBUS = the IBus socket."""
    if not path:
        runtime = os.environ.get("XDG_RUNTIME_DIR", f"/run/user/{os.getuid()}")
        found = glob.glob(os.path.join(runtime, "e39rpi-qemu.*", "state"))
        if len(found) != 1:
            raise RuntimeError(f"{len(found)} QEMU runs in {runtime}; is run-qemu.sh running? (or --qemu-state)")
        path = found[0]
    with open(path) as f:
        return dict(line.strip().split("=", 1) for line in f if "=" in line)
