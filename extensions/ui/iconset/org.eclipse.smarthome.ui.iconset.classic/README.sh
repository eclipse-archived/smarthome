#!/usr/bin/env sh


README="$(cd "$(dirname "$0")"; pwd)/README.md"


cat <<EOF > "${README}"
# Classic Icon Set

This is a modernized version of the original icon set of openHAB 1. The set is provided with the distribution in both the PNG and SVG file format. Move your mouse over an icon to learn its name.<br/><br/>

EOF


for icon in $(ls icons/*.png | sort -V); do

  name="$(basename "${icon}" | cut -d '.' -f1)"

  if [ "${name}" != 'none' ]; then
    echo "![${name}](${icon} \"${name}\")" >> "${README}"
  fi

done
