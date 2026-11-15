# HMI UI tests (pytest)

Drives the HMI with the knob and checks what it shows, through the HMI's
**test probe**: a gRPC server inside the HMI
(`composeApp/src/desktopMain/proto/e39hmi_test/v1/probe.proto`,
`ca.stefanm.ibus.testprobe.TestProbe`), off unless the HMI is started with
`E39_TEST_PROBE=<port>`, listening on 127.0.0.1 only.

The probe tells the runner the current screen and back stack, the
notification, which overlays and knob listeners are on, every text on
screen with its position (from Compose's accessibility tree) and which item
the knob is on (the theme's highlight in a screenshot); it takes knob input
(waiting for the HMI to settle between events), screenshots, and can jump to
any registered screen (like the HMI Navigator Debug window). For sample
mail, it also resets the mail app and delivers new mail.

## Running

Once:

```sh
sudo apt install xvfb socat python3-pytest python3-grpcio python3-grpc-tools
# or: pip install -r uitest/requirements.txt (plus Xvfb and socat)
```

Each time the HMI changed, build its jar (the local target runs it):

```sh
JAVA_HOME=~/.jdks/corretto-22.0.2 ./gradlew :composeApp:packageUberJarForCurrentOS
```

Then, from `uitest/`:

```sh
JAVA_HOME=~/.jdks/corretto-22.0.2 pytest                    # all tests, this laptop, sample mail
pytest tests/mail/test_03_paging.py                          # one test
pytest -k reader                                             # by name
pytest --show                                                # on your display instead of a hidden one
pytest --input ibus                                          # knob input as real IBus packets
pytest --mail bridge                                         # the PIM bridge's mail (see "Bridge mode")
pytest --target qemu                                         # a running e39-rpi-yocto scripts/run-qemu.sh
pytest --target device --device e39-rpi.local                # a Pi
```

Results: screenshots of each step (`NN-name.png`), on failure
`NN-failure.png` and `failure-state.txt`, and the HMI's log, in
`uitest/out/<date-time>/` (or `--out DIR`); one folder per test.

### Targets

- **local** (default): the newest `composeApp/build/compose/jars/*.jar` (or
  `--jar`) on a hidden Xvfb display, with a scratch configuration folder
  (`E39_CONFIG_DIR`: sample or bridge mail, 800×468, the BMW Blue theme;
  /var/lib/e39 isn't touched) and a socat pty pair standing in for the IBus
  adapter. Starts in about 5 s.
- **qemu / device**: over ssh as root (QEMU: port 2222). The runner adds
  `E39_TEST_PROBE=<--probe-port>` to `/etc/default/e39rpi-hmi`, restarts the
  hmi user's `e39rpi-hmi` service (`--no-restart` to skip) and tunnels the
  probe's port. The HMI on the device must be a build with the probe. Sample
  mail tests are skipped unless the device's config has
  `pimConfig.mail.useFakeData=true`. `--input ibus` on QEMU uses its IBus
  socket (close scripts/qemu-utility.py first: one client at a time).

### Bridge mode

`--mail bridge` (`--bridge host:port`, default `localhost:50052`) runs the
tests marked `bridge_mail` (`test_2x_bridge_*`) against a PIM bridge;
sample-mail tests are skipped. They expect the dev bridge's "Local
Folders" (e39-rpi-pim `scripts/run-dev.sh`: an inbox with six messages)
plus an "e39-list" folder of 250 messages for paging, written by:

```sh
uitest/tools/make-bridge-testmail.py ~/.local/share/e39pim-dev/share/local-mail --sync e39pim-dev
```

(`--sync` asks the bridge container's maildir resource to fetch the new
folder: the bridge only lists what Akonadi has fetched.) The tests undo
what they change (flags, moves), so they can run again. The dev bridge has
no sending account and no trash folder: the tests check that sending and
deleting are refused cleanly. `--bridge-maildir
~/.local/share/e39pim-dev/share/local-mail` also runs `test_24`, which
drops a message into the inbox and waits (the bridge reports new mail after
30 s of quiet) for the notification.

```sh
pytest -m bridge_mail --mail bridge --bridge-maildir ~/.local/share/e39pim-dev/share/local-mail
```

### Input

- **probe** (default): events go into the HMI's input stream, one at a time,
  each after the HMI has settled (no UI changes for 300 ms and someone
  listening). Lost events (nothing listening) fail the test.
- **ibus**: the BMBT's IBus packets (same bytes as e39-rpi-yocto's
  scripts/hmi.py), through the serial path the car uses. Turns can be lost
  while the HMI redraws (TODO.md, "KnobListenerService").

## Writing tests

`tests/<app>/test_NN_what.py`, one flow per file. The `hmi` fixture
(conftest.py) gives each test a fresh start: overlays and notification
closed, the mail app reset, on the root screen. `e39ui/hmi.py`:

```python
hmi.go("MailScreen")                    # jump to a registered screen
hmi.select(r"E39 list")                 # turn to the item matching the regex (ignoring case), press
hmi.wait_for_screen("MessageListScreen")
hmi.expect_text(r"· 1–100 of 250$")     # some text on screen matches
hmi.right(3); hmi.left(); hmi.press(); hmi.next_track()
hmi.state()                             # screen, back stack, notification, overlays, knob listeners
hmi.items(); hmi.selected()             # the knob's positions (the top overlay's) and its current one
hmi.type_text("Hi Anna")                # on the open on-screen keyboard, then Return
hmi.wait_for_notification(r"^Mail sent$")   # then hides it (it covers the top of the screen)
hmi.shot("name")                        # screenshot into the test's folder
```

Mark tests that need sample mail `@pytest.mark.fake_mail`, sample contacts
`@pytest.mark.fake_contacts`, sample accounts `@pytest.mark.fake_accounts`
(`--mail` chooses all three: samples, or the bridge's). Tests live by app:
`tests/mail/`, `tests/contacts/`, `tests/accounts/`. The local HMI's phone
login page (`PhoneLoginRelay`) listens on 127.0.0.1 and a free port; the
accounts tests play the phone with plain HTTP requests.

Highlight detection needs the HMI's window drawn: keep the screen unlocked
while the tests run (a locked screen fails every test with "No item is
highlighted").

## HMI bugs the tests found

- After a list screen laid out again (a notification, changed text, a
  closed side pane), no item was highlighted, and a press could run another
  row's action (`SmoothScroll` slot IDs, `CallWhen`, `AllocatedIdManager`):
  `test_10`, `test_14`. Fixed; `select()` fails when no item is highlighted.
- `EditConnectionScreen` wasn't registered with the navigator (`test_00`).
- The main menu's Mail count, sample mail and notification settings in the
  car platform's services (see the PR that added these tests).
