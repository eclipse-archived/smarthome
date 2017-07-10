#!/usr/bin/env sh

READMEMD="$(cd "$(dirname "$0")"; pwd)/README.md"

cat <<EOF > "$READMEMD"
# Classic Icon Set

This is a modernized version of the original icon set from openHAB 1.x.
The set is provided with the distribution in both the PNG and SVG file format.

<div id="iconset-preview">
EOF

for icon in $(ls icons/*.png | sort -V); do
  name="$(basename "$icon" | cut -d '.' -f1)"
  echo "Adding icon '$name'"
  if [ "$name" = "none" ]; then continue; fi
  cat <<EOF >> "$READMEMD"
  <figure style="width: 128px; display: inline-block; text-align: center; font-size: 0.8em; margin: 16px 8px;">
    <img src="$icon" alt="$name" title="$name">
    <figcaption>$name</figcaption>
  </figure>
EOF
done

echo "</div>" >> "$READMEMD"
echo "Finished."
