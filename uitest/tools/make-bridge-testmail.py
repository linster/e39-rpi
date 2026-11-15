#!/usr/bin/env python3
"""Mail for the bridge UI tests (pytest --mail bridge): a maildir folder "e39-list"
with 250 messages (3 unread), for paging. Writes into the maildir of a PIM bridge's
"Local Folders" account, e.g. the dev bridge's (e39-rpi-pim scripts/run-dev.sh):

  uitest/tools/make-bridge-testmail.py ~/.local/share/e39pim-dev/share/local-mail --sync e39pim-dev

--sync CONTAINER asks the maildir resource in that running bridge container to sync
(over D-Bus): the bridge lists only what Akonadi has fetched, and a new local folder's
messages aren't fetched until something asks. Without it, restart the bridge and sync
some other way. Does nothing if the folder is there already (--force: write it again).
"""

import argparse
import email.utils
import os
import shutil
import subprocess
import time

TOPICS = ["M54 cooling system", "VANOS seals", "Window regulator", "Nav screen pixels", "Rear subframe", "DISA valve"]


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("maildir", help="the Local Folders maildir (holds inbox/, sent-mail/, ...)")
    p.add_argument("--force", action="store_true", help="replace an existing e39-list folder")
    p.add_argument("--sync", metavar="CONTAINER", help="then sync the maildir resource in this bridge container")
    args = p.parse_args()
    folder = os.path.join(args.maildir, "e39-list")
    if os.path.exists(folder) and not args.force:
        print(f"{folder} is there already (--force to write it again)")
    else:
        shutil.rmtree(folder, ignore_errors=True)
        write(folder)
    if args.sync:
        sync(args.sync)


def sync(container):
    script = ("export DBUS_SESSION_BUS_ADDRESS=unix:path=/tmp/runtime-pim/bus; "
              "dbus-send --session --print-reply --dest=org.freedesktop.Akonadi.Resource.akonadi_maildir_resource_0 "
              "/ org.freedesktop.Akonadi.Resource.synchronize")
    subprocess.run(["podman", "exec", container, "sh", "-c", script], check=True, stdout=subprocess.DEVNULL)
    print(f"Asked {container}'s maildir resource to sync")


def write(folder):
    for sub in ("cur", "new", "tmp"):
        os.makedirs(os.path.join(folder, sub))
    now = time.time()
    for n in range(1, 251):
        number = 251 - n  # newest first: #250 three hours ago
        date = now - n * 3 * 3600
        body = (f"From: E39 list <e39@lists.example.org>\n"
                f"To: Stefan <stefan@example.com>\n"
                f"Subject: [e39] {TOPICS[n % len(TOPICS)]} (#{number})\n"
                f"Date: {email.utils.formatdate(date, localtime=True)}\n"
                f"Message-ID: <e39-list-{number}@lists.example.org>\n"
                f"Content-Type: text/plain; charset=utf-8\n\n"
                f"Message {number} of the E39 mailing list.\n")
        unread = n <= 3
        name = f"{int(date)}.e39list{number}.uitest" + ("" if unread else ":2,S")
        with open(os.path.join(folder, "new" if unread else "cur", name), "w") as f:
            f.write(body)
    print(f"Wrote 250 messages to {folder}")


if __name__ == "__main__":
    main()
