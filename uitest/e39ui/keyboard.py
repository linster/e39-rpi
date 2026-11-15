"""The on-screen QWERTY keyboard's keys (composeApp/.../keyboard/Keys.kt, qwertyKeyboardByRow):
each key's label without and with Shift. The space bar has no label."""

ROWS = [
    [("1", "!"), ("2", "@"), ("3", "#"), ("4", "$"), ("5", "%"), ("6", "^"), ("7", "&"), ("8", "*"),
     ("9", "("), ("0", ")"), ("-", "_"), ("=", "+")],
    [("q", "Q"), ("w", "W"), ("e", "E"), ("r", "R"), ("t", "T"), ("y", "Y"), ("u", "U"), ("i", "I"),
     ("o", "O"), ("p", "P"), ("[", "{"), ("]", "}"), ("\\", "|")],
    [("a", "A"), ("s", "S"), ("d", "D"), ("f", "F"), ("g", "G"), ("h", "H"), ("j", "J"), ("k", "K"),
     ("l", "L"), (";", ":"), ("'", '"')],
    [("z", "Z"), ("x", "X"), ("c", "C"), ("v", "V"), ("b", "B"), ("n", "N"), ("m", "M"), (",", "<"),
     (".", ">"), ("/", "?")],
]

_KEYS = {}
for row in ROWS:
    for lower, upper in row:
        _KEYS.setdefault(lower, (lower, False))
        _KEYS.setdefault(upper, (upper, True))


def key_for(c):
    """(label to select, needs Shift) for character c."""
    if c not in _KEYS:
        raise ValueError(f"{c!r} isn't on the HMI's keyboard")
    return _KEYS[c]
