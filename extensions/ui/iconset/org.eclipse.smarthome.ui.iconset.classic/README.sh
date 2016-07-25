#!/usr/bin/env sh


README="$(cd "$(dirname "$0")"; pwd)/README.md"


cat <<EOF > "${README}"
---
layout: documentation
---

{% include base.html %}

# Classic Icon Set

This is a modernized version of the original icon set of openHAB 1.<br/><br/>

EOF


for icon in icons/*.png; do

  name="$(basename "${icon}" | cut -d '.' -f1)"

  if [ "${name}" != 'none' ]; then
    echo "![${name}](${icon} \"${name}\")" >> "${README}"
  fi

done
