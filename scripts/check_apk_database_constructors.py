#!/usr/bin/env python3
"""Check the packaged DEX for Room constructors needed during app startup."""
import argparse
import re
import subprocess
import sys

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument("apk")
parser.add_argument("--apkanalyzer", default="apkanalyzer")
args = parser.parse_args()
classes = (
    "androidx.work.impl.WorkDatabase_Impl",
    "com.bbinxx.onecharge.data.local.OneChargeDatabase_Impl",
)
for name in classes:
    result = subprocess.run(
        [args.apkanalyzer, "dex", "code", "--class", name, args.apk],
        capture_output=True, text=True, check=True,
    )
    if not re.search(r"^\.method public constructor <init>\(\)V\s*$", result.stdout, re.MULTILINE):
        print(f"FAIL: {name} is missing its public no-argument constructor", file=sys.stderr)
        sys.exit(1)
    print(f"PASS: {name}")
