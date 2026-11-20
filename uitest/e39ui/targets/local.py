"""The HMI on this computer: the uber jar on a hidden Xvfb display, with a scratch configuration folder."""

import glob
import os
import shutil
import socket
import subprocess
import tempfile
import time

from ..proto import REPO

CONFIG = """e39Config {{
    carPlatformConfig {{
        isPi=false
        initialCarPlatformConfiguration=LAPTOP
        DisplayDriver=MK4
        iBusInterfaceUri="{ibus_uri}"
        SerialPortBaudRate="SERIAL_115200_8N1"
        ServiceGroupsOnStartup=[{service_groups}]
    }}
    loadingWindowConfig {{
        AutoLaunchCarPlatformOnOpen=true
        AutoLaunchHmi=true
        AutoLaunchHmiDelayMs=30
        HideMenuBehindLoading=true
    }}
    windowManagerConfig {{
        "DEBUG_HMI_DECORATION"=false
        "DEBUG_HMI_SHIFT_RIGHT"=false
        HmiWindowSize {{
            first=800
            second=468
        }}
        brightnessCompensation=0
    }}
    pimConfig {{
        backend {{
            host="{bridge_host}"
            port={bridge_port}
        }}
        mail {{
            useFakeData={fake_mail}
        }}
        contacts {{
            useFakeData={fake_mail}
        }}
        accounts {{
            useFakeData={fake_mail}
        }}
        loginRelay {{
            host="127.0.0.1"
            port=0
        }}
    }}
    mapConfig {{
        backend {{
            host="{maps_host}"
            port={maps_port}
        }}
    }}
}}
"""

THEME = """themeConfig {
    "Theme setting for windowsize takes precedence over main conf file"=true
    themeName=BmwBlueDoubledPixels
}
"""


def wait_window_settled(hmi, still_s=1.0, timeout_s=20):
    """Waits until the HMI window has stayed put for still_s (it moves once after opening)."""
    deadline = time.time() + timeout_s
    last, since = None, time.time()
    while time.time() < deadline:
        s = hmi.state()
        where = (s.window_x, s.window_y)
        if where != last:
            last, since = where, time.time()
        elif time.time() - since >= still_s:
            return
        time.sleep(0.2)


def free_port():
    with socket.socket() as s:
        s.bind(("127.0.0.1", 0))
        return s.getsockname()[1]


def default_jar():
    jars = sorted(glob.glob(os.path.join(REPO, "composeApp", "build", "compose", "jars", "e39Rpi-linux-*.jar")),
                  key=os.path.getmtime)
    return jars[-1] if jars else None


def java_binary():
    home = os.environ.get("JAVA_HOME")
    if home and os.path.exists(os.path.join(home, "bin", "java")):
        return os.path.join(home, "bin", "java")
    found = shutil.which("java")
    if not found:
        raise RuntimeError("No java: set JAVA_HOME (the HMI needs Java 17 or newer)")
    return found


class LocalTarget:
    """Starts Xvfb and the HMI; `address` is the probe's. `ibus=True`: a socat
    pty pair stands in for the IBus adapter, and `ibus_path` is the end
    the runner writes to."""

    def __init__(self, out_dir, jar=None, fake_mail=True, bridge="localhost:50052", ibus=False,
                 show=False, startup_timeout_s=120, maps_backend="localhost:50051"):
        self.out_dir = out_dir
        self.jar = jar or default_jar()
        if not self.jar or not os.path.exists(self.jar):
            raise RuntimeError("No HMI jar: build it with\n"
                               "  ./gradlew :composeApp:packageUberJarForCurrentOS\n"
                               "or pass --jar")
        self.fake_mail = fake_mail
        self.bridge = bridge
        self.maps_backend = maps_backend
        self.want_ibus = ibus
        self.show = show
        self.startup_timeout_s = startup_timeout_s
        self.procs = []
        self.ibus_path = None
        self.address = None

    def start(self):
        os.makedirs(self.out_dir, exist_ok=True)
        self.workdir = tempfile.mkdtemp(prefix="e39ui-")
        config_dir = os.path.join(self.workdir, "config")
        os.makedirs(config_dir)

        # A pty pair stands in for the IBus adapter. The HMI opens its port
        # even without the SerialInterface services (and fails for a path
        # that doesn't exist), so there's always one; with ibus=True the
        # services run and the runner writes knob packets to ibus_path.
        hmi_end = os.path.join(self.workdir, "ibus-hmi")
        test_end = os.path.join(self.workdir, "ibus-test")
        self._spawn(["socat", f"pty,raw,echo=0,link={hmi_end}", f"pty,raw,echo=0,link={test_end}"], "socat.log")
        self._wait(lambda: os.path.exists(hmi_end) and os.path.exists(test_end), "the IBus pty", 10)
        ibus_uri = os.path.realpath(hmi_end)
        groups = ["Pim", "CliPrinters"]
        if self.want_ibus:
            self.ibus_path = test_end
            groups.append("SerialInterface")

        host, _, port = self.bridge.partition(":")
        maps_host, _, maps_port = self.maps_backend.partition(":")
        with open(os.path.join(config_dir, "config.conf"), "w") as f:
            f.write(CONFIG.format(ibus_uri=ibus_uri, service_groups=", ".join(groups), bridge_host=host,
                                  bridge_port=int(port or 50052), fake_mail="true" if self.fake_mail else "false",
                                  maps_host=maps_host, maps_port=int(maps_port or 50051)))
        with open(os.path.join(config_dir, "theme.conf"), "w") as f:
            f.write(THEME)

        env = dict(os.environ)
        if not self.show:
            env["DISPLAY"] = self._start_xvfb()
        port = free_port()
        env.update({
            "E39_CONFIG_DIR": config_dir,
            "E39_TEST_PROBE": str(port),
            "E39_LOG_TARGET": "stdout",
        })
        self._spawn([java_binary(), "-jar", self.jar], "hmi.log", env=env)
        self.address = f"127.0.0.1:{port}"

    def wait_ready(self, hmi):
        def ready():
            if self.procs[-1].poll() is not None:
                raise RuntimeError(f"The HMI exited ({self.procs[-1].returncode}); see {self.out_dir}/hmi.log")
            try:
                return hmi.state().hmi_window_open
            except Exception:
                return False
        self._wait(ready, "the HMI window", self.startup_timeout_s)
        wait_window_settled(hmi)

    def _start_xvfb(self):
        r, w = os.pipe()
        self._spawn(["Xvfb", "-displayfd", str(w), "-screen", "0", "1280x800x24", "-nolisten", "tcp"],
                    "xvfb.log", pass_fds=(w,))
        os.close(w)
        number = b""
        deadline = time.time() + 15
        while not number.endswith(b"\n") and time.time() < deadline:
            chunk = os.read(r, 16)
            if not chunk:
                break
            number += chunk
        os.close(r)
        if not number.strip():
            raise RuntimeError(f"Xvfb didn't start; see {self.out_dir}/xvfb.log")
        return ":" + number.strip().decode()

    def _spawn(self, cmd, log, **kwargs):
        f = open(os.path.join(self.out_dir, log), "wb")
        p = subprocess.Popen(cmd, stdout=f, stderr=subprocess.STDOUT, start_new_session=True, **kwargs)
        self.procs.append(p)
        return p

    @staticmethod
    def _wait(check, what, timeout_s):
        deadline = time.time() + timeout_s
        while not check():
            if time.time() > deadline:
                raise RuntimeError(f"Timed out waiting for {what}")
            time.sleep(0.25)

    def stop(self):
        for p in reversed(self.procs):
            if p.poll() is None:
                p.terminate()
                try:
                    p.wait(10)
                except subprocess.TimeoutExpired:
                    p.kill()
        shutil.rmtree(getattr(self, "workdir", ""), ignore_errors=True)
