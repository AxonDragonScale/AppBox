#!/usr/bin/env bash
# Verifies the context graph in docs/ still points at real things.
#
# 1. Every relative markdown link resolves.
# 2. Every doc is reachable from the index (docs/context.md).
# 3. Every repo path written in backticks exists.
#
# Runs on commit via .githooks/pre-commit. Run by hand with ./scripts/check-docs.sh

set -uo pipefail

root="$(git rev-parse --show-toplevel)" || exit 1
cd "$root" || exit 1

fail=0
errors=""

add_error() {
  errors="${errors}  $1"$'\n'
  fail=1
}

# 1. Markdown links ---------------------------------------------------------
while IFS= read -r doc; do
  dir="$(dirname "$doc")"
  for link in $(grep -oE '\]\([^)]+\)' "$doc" | sed 's/^](//; s/)$//'); do
    case "$link" in
      http://*|https://*|mailto:*|'#'*) continue ;;
    esac
    target="${link%%#*}"
    [ -n "$target" ] || continue
    if [ ! -e "$dir/$target" ]; then
      add_error "$doc: broken link -> $link"
    fi
  done
done < <(find docs -name '*.md' | sort)

# 2. Index reachability -----------------------------------------------------
indexed="$(grep -oE '\]\([^)]+\)' docs/context.md | sed 's/^](//; s/)$//; s/#.*//')"
while IFS= read -r doc; do
  [ "$doc" = "docs/context.md" ] && continue
  rel="${doc#docs/}"
  if ! printf '%s\n' "$indexed" | grep -qxF "./$rel"; then
    add_error "$doc: not linked from docs/context.md"
  fi
done < <(find docs -name '*.md' | sort)

# 3. Backticked file paths --------------------------------------------------
while IFS= read -r doc; do
  dir="$(dirname "$doc")"
  # Only root-relative paths are checked: `apps/...`, `:core:x`-style fragments,
  # bare filenames and planned paths are not assertions about the filesystem.
  for token in $(grep -oE '`[^`]+`' "$doc" | tr -d '`'); do
    case "$token" in
      apps/*|core/*|libraries/*|build-logic/*|docs/*|gradle/*|scripts/*|keystore/*) ;;
      *) continue ;;
    esac
    case "$token" in
      *'*'*|*'<'*|*'>'*|*'$'*|*'|'*|*'...'*|*' '*) continue ;;
    esac
    case "$token" in
      *.kt|*.kts|*.md|*.toml|*.xml|*.pro|*.fig|*.json|*.sh) ;;
      *) continue ;;
    esac
    if [ ! -e "$root/$token" ]; then
      add_error "$doc: references missing path -> $token"
    fi
  done
done < <(find docs -name '*.md' | sort)

if [ "$fail" -ne 0 ]; then
  echo "Doc check failed:"
  printf '%s' "$errors"
  echo
  echo "Fix the paths above, or add the doc to the index in docs/context.md."
  exit 1
fi

echo "Doc check passed."
