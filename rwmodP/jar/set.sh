#!/bin/bash
mkdir -p "libs"
for aar_file in ../app/libs/*.aar; do
  aar_name=$(basename "$aar_file" .aar)
  output_jar="libs/${aar_name}.jar"
  if [ "$aar_file" -nt "$output_jar" ]; then
    unzip -p "$aar_file" "classes.jar" > "$output_jar" 2>/dev/null
    echo "Extracted: $output_jar"
  else
    echo "Skipped (up-to-date): $output_jar"
  fi
done

echo "Extraction complete."