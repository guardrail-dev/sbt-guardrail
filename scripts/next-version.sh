# next-version.sh
#   Guess the next version of the sbt-guardrail plugin based on the current library dependency
#   Ideally, this'll let actions automatically tag and release.
#
#   Strategy is to pull the com.twilio %% guardrail % <VERSION>, and loop while the following holds:
#   ... check to see if a tag with that version exists,
#   ... check to see if we have a fourth version component (eg: 0.10.20.1)
#   ... if not, add it.
#   ... if so, increment it.

#   Usage: render_version "0" "1" "2"
# Returns: 0.1.2
render_version() {
  parts=( "$@" )
  NEW_VERSION="${parts[0]}.${parts[1]}.${parts[2]}"
  if [ "${#parts[@]}" -ge 4 ]; then
    NEW_VERSION="${NEW_VERSION}.${parts[3]}"
  fi
  echo $NEW_VERSION
}

# Extract library version
version="$(grep -ho 'com\.twilio" %% "guardrail" % "[^"]*"' modules/core/build.sbt | grep -ho '\([0-9]\+\)\(\.[0-9]\+\)\{1,\}')"

# Check to see if we've already released for this library version
if git rev-parse "v${version}" >/dev/null 2>&1; then
  # Split version parts
  parts=( ${version//./ } )
  # Iterate until we find an untagged version
  while git rev-parse "v${version}" >/dev/null 2>&1; do
    # If we already have a sub-version,
    if [ "${#parts[@]}" -ge 4 ]; then
      # bump that version
      ((parts[3]++))
    else # otherwise
      # Set the sub-version to 1
      parts[3]=1
    fi
    version="$(render_version "${parts[@]}")"
  done
fi

echo "$version"
