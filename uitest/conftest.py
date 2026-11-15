"""pytest setup for the HMI's UI tests: which HMI (--target), input path, mail data.

See README.md. Each test gets `hmi` (e39ui.hmi.Hmi): the mail app reset,
overlays closed, on the root screen, screenshots going to
out/<run>/<test>/.
"""

import datetime
import os
import re

import pytest

from e39ui.hmi import Hmi
from e39ui.ibus import IBusWriter
from e39ui.targets.local import LocalTarget
from e39ui.targets.remote import RemoteTarget, qemu_state

UITEST = os.path.dirname(os.path.abspath(__file__))


def pytest_addoption(parser):
    g = parser.getgroup("e39ui", "e39-rpi HMI UI tests")
    g.addoption("--target", choices=["local", "qemu", "device"], default="local",
                help="local: the jar on a hidden X display (default); qemu: a running run-qemu.sh; device: a Pi over ssh")
    g.addoption("--input", choices=["probe", "ibus"], default="probe",
                help="probe: input through the test probe (default, waits for the HMI between events); "
                     "ibus: real IBus packets (a pty locally, QEMU's IBus socket)")
    g.addoption("--mail", choices=["fake", "bridge"], default="fake",
                help="local target: sample mail and contacts (default) or the PIM bridge's (--bridge)")
    g.addoption("--bridge", default="localhost:50052", help="the PIM bridge for --mail bridge (host:port)")
    g.addoption("--bridge-maildir", default=None,
                help="--mail bridge: the bridge's Local Folders maildir on this machine (e.g. "
                     "~/.local/share/e39pim-dev/share/local-mail), for tests that deliver mail into it")
    g.addoption("--jar", default=None, help="local target: the HMI uber jar (default: the newest in composeApp/build/compose/jars)")
    g.addoption("--show", action="store_true", help="local target: on this display instead of a hidden one")
    g.addoption("--qemu-state", default=None, help="qemu target: run-qemu.sh's state file (default: the only one)")
    g.addoption("--device", default=None, help="device target: the Pi's host name or address")
    g.addoption("--ssh-port", type=int, default=None, help="device/qemu: ssh port (default 22 / 2222)")
    g.addoption("--probe-port", type=int, default=50199, help="device/qemu: the probe's port on the device")
    g.addoption("--no-restart", action="store_true", help="device/qemu: don't restart the HMI (it already runs with the probe)")
    g.addoption("--out", default=None, help="where screenshots and logs go (default: uitest/out/<date-time>)")


def pytest_configure(config):
    config.addinivalue_line("markers", "fake_mail: needs sample mail (skipped otherwise)")
    config.addinivalue_line("markers", "bridge_mail: needs the PIM bridge's mail (skipped otherwise)")
    config.addinivalue_line("markers", "fake_contacts: needs sample contacts (skipped otherwise)")


@pytest.fixture(scope="session")
def out_dir(pytestconfig):
    out = pytestconfig.getoption("--out") or os.path.join(
        UITEST, "out", datetime.datetime.now().strftime("%Y%m%d-%H%M%S"))
    os.makedirs(out, exist_ok=True)
    return out


@pytest.fixture(scope="session")
def bridge_maildir(pytestconfig):
    """The bridge's Local Folders maildir (--bridge-maildir), or the test is skipped."""
    path = pytestconfig.getoption("--bridge-maildir")
    if not path:
        pytest.skip("needs --bridge-maildir")
    path = os.path.expanduser(path)
    if not os.path.isdir(os.path.join(path, "inbox")):
        pytest.fail(f"--bridge-maildir {path}: no inbox/ there")
    return path


@pytest.fixture(scope="session")
def target(pytestconfig, out_dir):
    opt = pytestconfig.getoption
    kind = opt("--target")
    if kind == "local":
        t = LocalTarget(out_dir, jar=opt("--jar"), fake_mail=opt("--mail") == "fake", bridge=opt("--bridge"),
                        ibus=opt("--input") == "ibus", show=opt("--show"))
    elif kind == "qemu":
        state = qemu_state(opt("--qemu-state"))
        t = RemoteTarget(out_dir, "localhost", ssh_port=opt("--ssh-port") or 2222, probe_port=opt("--probe-port"),
                         restart=not opt("--no-restart"), ibus_socket=state.get("IBUS"))
    else:
        if not opt("--device"):
            pytest.exit("--target device needs --device HOST")
        t = RemoteTarget(out_dir, opt("--device"), ssh_port=opt("--ssh-port") or 22, probe_port=opt("--probe-port"),
                         restart=not opt("--no-restart"))
    t.start()
    yield t
    t.stop()


@pytest.fixture(scope="session")
def hmi_session(pytestconfig, target):
    writer = None
    if pytestconfig.getoption("--input") == "ibus":
        if not target.ibus_path:
            pytest.exit("--input ibus: this target has no IBus to write to")
        writer = IBusWriter(target.ibus_path, is_socket=pytestconfig.getoption("--target") == "qemu")
    h = Hmi(target.address, ibus_writer=writer)
    target.wait_ready(h)
    h.wait_idle(timeout_ms=60000)
    yield h
    h.close()


@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_makereport(item, call):
    outcome = yield
    report = outcome.get_result()
    setattr(item, "rep_" + report.when, report)


@pytest.fixture
def hmi(request, hmi_session, out_dir):
    """The HMI, reset for this test: overlays closed, the mail app as on a fresh start, on the root screen."""
    h = hmi_session
    name = re.sub(r"[^A-Za-z0-9_.-]+", "_", request.node.nodeid.replace("tests/", ""))
    h.start_test(os.path.join(out_dir, name))
    state = h.state()
    if request.node.get_closest_marker("fake_mail") and not state.fake_mail:
        pytest.skip("needs sample mail (useFakeData)")
    if request.node.get_closest_marker("bridge_mail") and state.fake_mail:
        pytest.skip("needs the PIM bridge's mail (--mail bridge)")
    if request.node.get_closest_marker("fake_contacts") and not state.fake_contacts:
        pytest.skip("needs sample contacts (useFakeData)")
    h.close_overlays()
    h.go_root()
    h.reset_mail_app()
    h.reset_contacts_app()
    yield h
    rep = getattr(request.node, "rep_call", None)
    if rep is not None and rep.failed:
        try:
            h.shot("failure")
            with open(os.path.join(h.out_dir, "failure-state.txt"), "w") as f:
                f.write(str(h.state()))
        except Exception as e:
            print(f"Couldn't record the failure: {e}")
