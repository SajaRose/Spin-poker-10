#!/usr/bin/env python3
"""Générateur de sons synthétiques pour Spin Poker 10.

Produit des WAV 16 bits mono 44.1 kHz dans app/src/main/res/raw.
Aucune dépendance externe : bibliothèque standard uniquement.
"""
import math
import os
import random
import struct
import wave

RATE = 44100
OUT = os.path.join(os.path.dirname(__file__), "..", "app", "src", "main", "res", "raw")


def write_wav(name, samples):
    os.makedirs(OUT, exist_ok=True)
    path = os.path.join(OUT, name + ".wav")
    with wave.open(path, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        frames = bytearray()
        for s in samples:
            v = max(-1.0, min(1.0, s))
            frames += struct.pack("<h", int(v * 32767))
        w.writeframes(bytes(frames))
    print("OK", path)


def tone(freq, dur, vol=0.6, attack=0.005, decay=None):
    n = int(RATE * dur)
    out = []
    ph = 0.0
    for i in range(n):
        t = i / RATE
        env = min(1.0, t / attack) if attack > 0 else 1.0
        rel = decay if decay is not None else dur
        if t > dur - rel:
            env *= max(0.0, (dur - t) / rel)
        # léger vibrato pour un son plus vivant
        ph += 2 * math.pi * (freq + 4 * math.sin(2 * math.pi * 5 * t)) / RATE
        out.append(vol * env * math.sin(ph))
    return out


def noise(dur, vol=0.5, lowpass=0.2):
    n = int(RATE * dur)
    out = []
    prev = 0.0
    for i in range(n):
        t = i / RATE
        env = math.sin(math.pi * min(1.0, t / dur))
        prev = prev + lowpass * (random.uniform(-1, 1) - prev)
        out.append(vol * env * prev)
    return out


def mix(*layers):
    n = max(len(l) for l in layers)
    out = [0.0] * n
    for l in layers:
        for i, s in enumerate(l):
            out[i] += s
    peak = max(1e-9, max(abs(s) for s in out))
    if peak > 0.95:
        out = [s * 0.95 / peak for s in out]
    return out


def concat(*layers):
    out = []
    for l in layers:
        out.extend(l)
    return out


def sweep(f0, f1, dur, vol=0.5):
    n = int(RATE * dur)
    out = []
    ph = 0.0
    for i in range(n):
        t = i / RATE
        f = f0 + (f1 - f0) * (t / dur)
        ph += 2 * math.pi * f / RATE
        env = math.sin(math.pi * min(1.0, t / dur) ** 0.5)
        out.append(vol * env * math.sin(ph))
    return out


# click : blip court de bouton
write_wav("click", tone(1800, 0.05, vol=0.5, decay=0.04))
# deal : whoosh de distribution des cartes
write_wav("deal", mix(noise(0.35, vol=0.35), sweep(300, 900, 0.35, vol=0.2)))
# reel_stop : arrêt de rouleau, thock grave
write_wav("reel_stop", mix(tone(150, 0.12, vol=0.7, decay=0.1), tone(900, 0.05, vol=0.3, decay=0.04)))
# hold : ding de maintien
write_wav("hold", mix(tone(660, 0.25, vol=0.4, decay=0.2), tone(1320, 0.25, vol=0.2, decay=0.2)))
# coin : pièce de crédit
write_wav("coin", concat(tone(2500, 0.08, vol=0.4, decay=0.07), tone(2000, 0.1, vol=0.35, decay=0.09)))
# win : arpeggio montant
write_wav("win", concat(
    tone(523, 0.12, vol=0.5, decay=0.1),
    tone(659, 0.12, vol=0.5, decay=0.1),
    tone(784, 0.12, vol=0.5, decay=0.1),
    tone(1046, 0.3, vol=0.55, decay=0.25),
))
# bigwin : fanfare
write_wav("bigwin", mix(
    concat(tone(392, 0.15, vol=0.4), tone(523, 0.15, vol=0.45), tone(659, 0.15, vol=0.45),
           tone(784, 0.4, vol=0.5, decay=0.3)),
    concat(tone(196, 0.45, vol=0.3, decay=0.3), tone(0, 0.15), tone(262, 0.4, vol=0.25, decay=0.3)),
))
# lose : descente douce
write_wav("lose", sweep(400, 180, 0.45, vol=0.3))
