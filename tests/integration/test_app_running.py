#!/usr/bin/env python3
"""
Integration tests for text_to_isl.

Verifies that required application files exist, the Flask server starts,
and submitting text returns a non-empty sign-word mapping response.
"""

from __future__ import annotations

import os
import signal
import subprocess
import sys
import threading
import time
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

# Repo root (tests/integration/ -> repo root)
REPO_ROOT = Path(__file__).resolve().parents[2]
# Default to 5055 so local runs do not collide with macOS AirPlay Receiver on 5000
IT_PORT = os.environ.get("PORT", os.environ.get("IT_PORT", "5055"))
BASE_URL = os.environ.get("IT_BASE_URL", f"http://127.0.0.1:{IT_PORT}")
SERVER_START_TIMEOUT = int(os.environ.get("IT_SERVER_START_TIMEOUT", "600"))
REQUEST_TIMEOUT = int(os.environ.get("IT_REQUEST_TIMEOUT", "300"))
TEST_INPUT = os.environ.get("IT_TEST_INPUT", "Hello world")


class IntegrationTestError(Exception):
    pass


def log(msg: str) -> None:
    print(f"[IT] {msg}", flush=True)


def required_paths() -> list[Path]:
    return [
        REPO_ROOT / "main.py",
        REPO_ROOT / "requirements.txt",
        REPO_ROOT / "words.txt",
        REPO_ROOT / "templates" / "index.html",
        REPO_ROOT / "static" / "js" / "script.js",
        REPO_ROOT / "static" / "css" / "style.css",
        REPO_ROOT / "static" / "SignFiles",
        REPO_ROOT / "Dockerfile",
        REPO_ROOT / "docker-compose.yml",
        REPO_ROOT / "stanford-parser-full-2018-10-17.jar",
    ]


def test_required_files_exist() -> None:
    log("Checking required application files...")
    missing = [str(p.relative_to(REPO_ROOT)) for p in required_paths() if not p.exists()]
    if missing:
        raise IntegrationTestError(f"Missing required files/dirs: {missing}")

    sign_files = list((REPO_ROOT / "static" / "SignFiles").glob("*.sigml"))
    if len(sign_files) < 10:
        raise IntegrationTestError(
            f"Expected many .sigml sign files, found only {len(sign_files)}"
        )

    words = (REPO_ROOT / "words.txt").read_text(encoding="utf-8", errors="ignore").strip()
    if not words:
        raise IntegrationTestError("words.txt is empty")

    jar_size = (REPO_ROOT / "stanford-parser-full-2018-10-17.jar").stat().st_size
    if jar_size < 1000:
        raise IntegrationTestError(
            f"stanford-parser-full-2018-10-17.jar looks invalid ({jar_size} bytes). "
            "Git LFS may not have pulled the real file."
        )

    log(f"All required files present ({len(sign_files)} sigml files, jar={jar_size} bytes)")


def _start_log_reader(proc: subprocess.Popen, collected: list[str]) -> threading.Thread:
    """Continuously drain server stdout so the process cannot block on a full pipe."""

    def _reader() -> None:
        if proc.stdout is None:
            return
        try:
            for line in proc.stdout:
                collected.append(line)
                print(line, end="", flush=True)
        except Exception:
            pass

    t = threading.Thread(target=_reader, name="server-log-reader", daemon=True)
    t.start()
    return t


def wait_for_server(
    url: str,
    timeout: int,
    proc: subprocess.Popen | None = None,
    log_buf: list[str] | None = None,
) -> None:
    deadline = time.time() + timeout
    last_err = None
    log_buf = log_buf if log_buf is not None else []
    while time.time() < deadline:
        if proc is not None and proc.poll() is not None:
            time.sleep(0.2)  # let log reader flush
            tail = "".join(log_buf)[-6000:]
            raise IntegrationTestError(
                f"Server process exited early with code {proc.returncode}. Output tail:\n{tail}"
            )
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "text-to-isl-it/1.0"})
            with urllib.request.urlopen(req, timeout=5) as resp:
                if resp.status == 200:
                    body = resp.read(200).decode("utf-8", errors="ignore")
                    if "html" in body.lower() or "<!doctype" in body.lower() or body.strip():
                        log(f"Server is up at {url}")
                        return
        except urllib.error.HTTPError as exc:
            last_err = exc
        except Exception as exc:  # noqa: BLE001 - collect and retry until timeout
            last_err = exc
        time.sleep(3)
    tail = "".join(log_buf)[-4000:]
    raise IntegrationTestError(
        f"Server did not become ready at {url} within {timeout}s. "
        f"Last error: {last_err}\nServer log tail:\n{tail}"
    )


def http_get(url: str) -> tuple[int, str]:
    with urllib.request.urlopen(url, timeout=REQUEST_TIMEOUT) as resp:
        return resp.status, resp.read().decode("utf-8", errors="ignore")


def http_post_form(url: str, data: dict[str, str]) -> tuple[int, str]:
    encoded = urllib.parse.urlencode(data).encode("utf-8")
    req = urllib.request.Request(
        url,
        data=encoded,
        method="POST",
        headers={"Content-Type": "application/x-www-form-urlencoded"},
    )
    with urllib.request.urlopen(req, timeout=REQUEST_TIMEOUT) as resp:
        return resp.status, resp.read().decode("utf-8", errors="ignore")


def test_homepage(base_url: str) -> None:
    log("GET / (homepage)...")
    status, body = http_get(base_url.rstrip("/") + "/")
    if status != 200:
        raise IntegrationTestError(f"Homepage returned status {status}")
    if not body or len(body) < 50:
        raise IntegrationTestError("Homepage body looks empty/invalid")
    log("Homepage OK")


def test_text_to_sign_output(base_url: str) -> None:
    log(f"POST / with text='{TEST_INPUT}' (may take a while on first run)...")
    status, body = http_post_form(base_url.rstrip("/") + "/", {"text": TEST_INPUT})
    if status != 200:
        raise IntegrationTestError(f"Text submit returned status {status}, body={body[:500]!r}")
    if not body or not body.strip():
        raise IntegrationTestError("Server returned empty response for non-empty input")

    # App returns a Python/JSON-like dict of index -> word/letter tokens
    stripped = body.strip()
    if stripped in ("", "{}", "null", "None"):
        raise IntegrationTestError(f"Unexpected empty mapping response: {body!r}")

    # Must contain at least one alphabetic token from the input (reordered/lemmatized/fingerspelled)
    lower = stripped.lower()
    expected_tokens = [t for t in TEST_INPUT.lower().replace(".", " ").split() if t]
    # Also accept single-letter fingerspelling pieces from input words
    letter_hits = any(ch in lower for word in expected_tokens for ch in word if ch.isalpha())
    if not letter_hits and ":" not in stripped:
        raise IntegrationTestError(
            f"Response does not look like a sign-word mapping: {body[:500]!r}"
        )

    log(f"Text processing OK, response snippet: {stripped[:200]}")


def start_local_server() -> subprocess.Popen | None:
    """Start main.py if IT_BASE_URL is not externally provided via IT_EXTERNAL_SERVER=1."""
    if os.environ.get("IT_EXTERNAL_SERVER", "").lower() in ("1", "true", "yes"):
        log(f"Using external server at {BASE_URL}")
        return None

    env = os.environ.copy()
    env.setdefault("PYTHONUNBUFFERED", "1")
    env.setdefault("PORT", IT_PORT)
    # Lighter stanza processors in IT/CI unless explicitly disabled
    env.setdefault("IT_LIGHT_PIPELINE", "1")
    # Avoid interactive SSL issues in CI when stanza/nltk fetch resources
    env.setdefault("STANZA_RESOURCES_DIR", str(REPO_ROOT / "stanza_resources"))

    log(
        f"Starting local Flask server on port {env['PORT']} "
        f"(IT_LIGHT_PIPELINE={env.get('IT_LIGHT_PIPELINE')})..."
    )
    # Stream server logs line-by-line so CI failures are debuggable
    proc = subprocess.Popen(
        [sys.executable, "main.py"],
        cwd=str(REPO_ROOT),
        env=env,
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True,
        bufsize=1,
    )
    return proc


def stop_server(proc: subprocess.Popen | None) -> None:
    if proc is None:
        return
    try:
        if proc.poll() is None:
            proc.send_signal(signal.SIGTERM)
            try:
                proc.wait(timeout=15)
            except subprocess.TimeoutExpired:
                proc.kill()
                proc.wait(timeout=5)
    except Exception:
        pass


def main() -> int:
    os.chdir(REPO_ROOT)
    failures: list[str] = []
    proc: subprocess.Popen | None = None
    log_reader: threading.Thread | None = None
    log_buf: list[str] = []

    try:
        test_required_files_exist()
    except IntegrationTestError as exc:
        log(f"FAIL (files): {exc}")
        return 1

    try:
        proc = start_local_server()
        if proc is not None:
            log_reader = _start_log_reader(proc, log_buf)
        wait_for_server(
            BASE_URL.rstrip("/") + "/",
            SERVER_START_TIMEOUT,
            proc=proc,
            log_buf=log_buf,
        )

        try:
            test_homepage(BASE_URL)
        except IntegrationTestError as exc:
            failures.append(f"homepage: {exc}")

        try:
            test_text_to_sign_output(BASE_URL)
        except IntegrationTestError as exc:
            failures.append(f"text_output: {exc}")
        except urllib.error.URLError as exc:
            failures.append(f"text_output network error: {exc}")
        except Exception as exc:  # noqa: BLE001
            failures.append(f"text_output unexpected: {exc}")

    except IntegrationTestError as exc:
        failures.append(str(exc))
    except Exception as exc:  # noqa: BLE001
        failures.append(f"unexpected error: {exc}")
    finally:
        stop_server(proc)
        if log_reader is not None:
            log_reader.join(timeout=5)

    server_log = "".join(log_buf)
    if failures:
        log("==== INTEGRATION TESTS FAILED ====")
        for f in failures:
            log(f" - {f}")
        if server_log:
            log("==== SERVER LOG (tail) ====")
            print(server_log[-8000:], flush=True)
        return 1

    log("==== ALL INTEGRATION TESTS PASSED ====")
    return 0


if __name__ == "__main__":
    sys.exit(main())
