"""Adding a Google account opens its login's QR code; the phone's page finishes it (the test plays the phone)."""

import re
import urllib.parse
import urllib.request

import pytest

# Where the sample logins end (FakeAccountRepository.REDIRECT).
REDIRECT = "http://127.0.0.1:48123/"


def phone_link(hmi):
    texts = [t.text for t in hmi.texts()]
    for t in texts:
        m = re.match(r"^Or open: (http://\S+/login/\w+)$", t)
        if m:
            return m.group(1)
    raise AssertionError(f"no link on the screen: {texts}")


def get(url):
    with urllib.request.urlopen(url, timeout=10) as r:
        return r.read().decode()


def paste(url, pasted):
    data = urllib.parse.urlencode({"url": pasted}).encode()
    with urllib.request.urlopen(url, data=data, timeout=20) as r:
        return r.read().decode()


@pytest.mark.fake_accounts
def test_google_login_on_a_phone(hmi):
    hmi.go("PimAccountsScreen")
    hmi.select(r"^Add Google Account\.\.\.$")
    hmi.wait_for_screen("PhoneLoginScreen")
    hmi.wait_for_notification(r"^Google account added$")
    hmi.expect_text(r"^Log in on your phone$")
    link = phone_link(hmi)
    hmi.shot("qr")

    # The phone opens the link: the login's button and the paste box.
    page = get(link)
    assert "Open the login" in page and "accounts.google.com" in page and "Finish Login" in page, page

    # Pasting something else: said so, nothing ends.
    page = paste(link, "https://www.google.com/")
    assert "isn't the address of Google's last page" in page, page
    hmi.expect_screen("PhoneLoginScreen")

    # Google's last address (copied without the scheme): the login ends, the screen goes back.
    page = paste(link, REDIRECT.removeprefix("http://") + "?state=x&code=4/0Abc-def&scope=email")
    assert "finishing the login" in page, page
    hmi.wait_for_screen("PimAccountsScreen")
    hmi.wait_for_notification(r"^Logged in$")
    hmi.expect_text(r"^WAITING", present=False)
    hmi.expect_text(r"^GOOGLE · LOG IN NEEDED", present=False)

    # The link has ended.
    try:
        get(link)
        raise AssertionError("the link still works")
    except urllib.error.HTTPError as e:
        assert e.code == 404
