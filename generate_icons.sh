#!/bin/bash
SVG_FILE="/tmp/onecharge_icon.svg"
RES_DIR="app/src/main/res"

# Generate adaptive foreground icons
inkscape -w 108 -h 108 "$SVG_FILE" -o "$RES_DIR/mipmap-mdpi/ic_launcher_foreground.png"
inkscape -w 162 -h 162 "$SVG_FILE" -o "$RES_DIR/mipmap-hdpi/ic_launcher_foreground.png"
inkscape -w 216 -h 216 "$SVG_FILE" -o "$RES_DIR/mipmap-xhdpi/ic_launcher_foreground.png"
inkscape -w 324 -h 324 "$SVG_FILE" -o "$RES_DIR/mipmap-xxhdpi/ic_launcher_foreground.png"
inkscape -w 432 -h 432 "$SVG_FILE" -o "$RES_DIR/mipmap-xxxhdpi/ic_launcher_foreground.png"

# Generate legacy icons
inkscape -w 48 -h 48 "$SVG_FILE" -o "$RES_DIR/mipmap-mdpi/ic_launcher.png"
inkscape -w 72 -h 72 "$SVG_FILE" -o "$RES_DIR/mipmap-hdpi/ic_launcher.png"
inkscape -w 96 -h 96 "$SVG_FILE" -o "$RES_DIR/mipmap-xhdpi/ic_launcher.png"
inkscape -w 144 -h 144 "$SVG_FILE" -o "$RES_DIR/mipmap-xxhdpi/ic_launcher.png"
inkscape -w 192 -h 192 "$SVG_FILE" -o "$RES_DIR/mipmap-xxxhdpi/ic_launcher.png"

# Duplicate for round
cp "$RES_DIR/mipmap-mdpi/ic_launcher.png" "$RES_DIR/mipmap-mdpi/ic_launcher_round.png"
cp "$RES_DIR/mipmap-hdpi/ic_launcher.png" "$RES_DIR/mipmap-hdpi/ic_launcher_round.png"
cp "$RES_DIR/mipmap-xhdpi/ic_launcher.png" "$RES_DIR/mipmap-xhdpi/ic_launcher_round.png"
cp "$RES_DIR/mipmap-xxhdpi/ic_launcher.png" "$RES_DIR/mipmap-xxhdpi/ic_launcher_round.png"
cp "$RES_DIR/mipmap-xxxhdpi/ic_launcher.png" "$RES_DIR/mipmap-xxxhdpi/ic_launcher_round.png"

# Remove existing webp
rm -f $RES_DIR/mipmap-*/ic_launcher.webp
rm -f $RES_DIR/mipmap-*/ic_launcher_round.webp
rm -f $RES_DIR/drawable/ic_launcher_foreground.xml
