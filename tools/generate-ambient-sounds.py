"""Generate Nota Bene's small, offline, seamlessly repeating ambient WAV assets."""

from pathlib import Path
import wave

import numpy as np


RATE = 22_050
DURATION = 14
COUNT = RATE * DURATION
TIME = np.arange(COUNT, dtype=np.float64) / RATE
OUT = Path(__file__).resolve().parents[1] / "app" / "src" / "main" / "res" / "raw"
RNG = np.random.default_rng(7401)


def periodic_noise(slope: float, low: float = 0.0, high: float = 1.0) -> np.ndarray:
    frequencies = np.fft.rfftfreq(COUNT, 1 / RATE)
    phases = RNG.uniform(0, 2 * np.pi, len(frequencies))
    amplitudes = np.ones_like(frequencies)
    amplitudes[1:] = np.power(frequencies[1:], -slope)
    amplitudes[0] = 0
    amplitudes[(frequencies < low) | (frequencies > high * RATE / 2)] = 0
    signal = np.fft.irfft(amplitudes * np.exp(1j * phases), n=COUNT)
    return signal / (np.std(signal) + 1e-9)


def soft_event(start: float, length: float, carrier: np.ndarray) -> np.ndarray:
    result = np.zeros(COUNT)
    first = int(start * RATE)
    size = min(int(length * RATE), COUNT - first)
    envelope = np.sin(np.linspace(0, np.pi, size)) ** 2
    result[first:first + size] = carrier[:size] * envelope
    return result


def wind() -> np.ndarray:
    breath = periodic_noise(1.05, 25, 2_000)
    air = periodic_noise(0.55, 180, 4_600)
    swell = 0.58 + 0.20 * np.sin(2 * np.pi * TIME / 7) + 0.12 * np.sin(2 * np.pi * TIME / 3.5)
    return 0.22 * breath * swell + 0.045 * air


def fountain() -> np.ndarray:
    water = periodic_noise(0.28, 240, 6_800)
    body = periodic_noise(0.92, 55, 1_100)
    signal = 0.12 * water + 0.09 * body
    for index, start in enumerate((1.2, 2.8, 4.9, 7.4, 9.1, 11.8)):
        length = 0.55
        local = np.arange(int(length * RATE)) / RATE
        tone = np.sin(2 * np.pi * (920 + index * 73) * local + 21 * local**2)
        signal += 0.055 * soft_event(start, length, tone)
    return signal


def nature() -> np.ndarray:
    breeze = 0.10 * periodic_noise(0.9, 35, 2_600)
    signal = breeze * (0.72 + 0.18 * np.sin(2 * np.pi * TIME / 7))
    for index, start in enumerate((1.8, 5.1, 8.7, 11.3)):
        length = 0.9
        local = np.arange(int(length * RATE)) / RATE
        base = 1_250 + index * 110
        chirp = np.sin(2 * np.pi * (base * local + 260 * local**2))
        chirp += 0.45 * np.sin(2 * np.pi * ((base * 1.47) * local + 180 * local**2))
        pulse = np.maximum(0, np.sin(2 * np.pi * 6 * local))
        signal += 0.045 * soft_event(start, length, chirp * pulse)
    return signal


def chimes() -> np.ndarray:
    signal = 0.035 * periodic_noise(0.95, 40, 1_800)
    for index, start in enumerate((0.8, 3.4, 6.2, 9.6, 12.0)):
        length = 1.45
        local = np.arange(int(length * RATE)) / RATE
        fundamental = (392, 440, 523.25, 587.33, 659.25)[index]
        bell = sum(
            weight * np.sin(2 * np.pi * fundamental * ratio * local)
            for ratio, weight in ((1.0, 1.0), (2.01, 0.42), (2.72, 0.24), (4.08, 0.12))
        )
        bell *= np.exp(-2.4 * local)
        signal += 0.105 * soft_event(start, length, bell)
    return signal


def write(name: str, signal: np.ndarray) -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    signal -= np.mean(signal)
    peak = np.max(np.abs(signal)) + 1e-9
    pcm = np.int16(np.clip(signal * (0.78 / peak), -1, 1) * 32767)
    with wave.open(str(OUT / f"ambient_{name}.wav"), "wb") as output:
        output.setnchannels(1)
        output.setsampwidth(2)
        output.setframerate(RATE)
        output.writeframes(pcm.tobytes())


for sound_name, generator in (
    ("wind", wind),
    ("fountain", fountain),
    ("nature", nature),
    ("chimes", chimes),
):
    write(sound_name, generator())
