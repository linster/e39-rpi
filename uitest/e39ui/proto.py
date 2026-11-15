"""The probe's Python gRPC code, generated from the HMI's probe.proto on first use.

Nothing generated is committed: the stubs are built into uitest/.gen/ with
grpcio-tools whenever probe.proto is newer than them.
"""

import importlib
import os
import sys

UITEST = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
REPO = os.path.dirname(UITEST)
PROTO_ROOT = os.path.join(REPO, "composeApp", "src", "desktopMain", "proto")
PROTO = os.path.join(PROTO_ROOT, "e39hmi_test", "v1", "probe.proto")
GEN = os.path.join(UITEST, ".gen")


def _generate():
    from grpc_tools import protoc
    import grpc_tools

    os.makedirs(GEN, exist_ok=True)
    well_known = os.path.join(os.path.dirname(grpc_tools.__file__), "_proto")
    args = ["protoc", f"-I{PROTO_ROOT}", f"-I{well_known}",
            f"--python_out={GEN}", f"--grpc_python_out={GEN}", PROTO]
    if protoc.main(args) != 0:
        raise RuntimeError(f"protoc failed for {PROTO}")
    # Packages for e39hmi_test/v1 so "import e39hmi_test.v1.probe_pb2" works.
    for d in (os.path.join(GEN, "e39hmi_test"), os.path.join(GEN, "e39hmi_test", "v1")):
        open(os.path.join(d, "__init__.py"), "a").close()


def load():
    """(probe_pb2, probe_pb2_grpc), generating them if needed."""
    out = os.path.join(GEN, "e39hmi_test", "v1", "probe_pb2.py")
    if not os.path.exists(out) or os.path.getmtime(out) < os.path.getmtime(PROTO):
        _generate()
    if GEN not in sys.path:
        sys.path.insert(0, GEN)
    pb2 = importlib.import_module("e39hmi_test.v1.probe_pb2")
    grpc_pb2 = importlib.import_module("e39hmi_test.v1.probe_pb2_grpc")
    return pb2, grpc_pb2
