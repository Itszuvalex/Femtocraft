#!/usr/bin/env bash
# Builds or runs Femtocraft against a chosen ItszuLib branch, tag or commit, without touching your ItszuLib checkout.
#
#   tools/itszulib-ref.sh <ref> [gradle args...]    e.g. tools/itszulib-ref.sh main runClient
#                                                         tools/itszulib-ref.sh v0.2.0 build
#                                                         tools/itszulib-ref.sh some-branch runGameTestServer
#   tools/itszulib-ref.sh --list                    worktrees made so far
#   tools/itszulib-ref.sh --clean                   remove them all
#
# Each ref gets a detached git worktree of your ItszuLib checkout under .itszulib/<ref> (git-ignored), updated from
# ItszuLib's remote on every run, and Gradle runs with -Pitszulib_dir pointed at it (Femtocraft's composite build;
# see settings.gradle). <ref> is looked up as a remote branch, then a tag, then any local ref or commit.
#
# Environment: ITSZULIB_DIR  the ItszuLib checkout to make worktrees from (default ../ItszuLib)
#              ITSZULIB_REMOTE  its remote (default origin)
#              ITSZULIB_NO_FETCH=1  skip fetching (offline; uses what was fetched before)
set -euo pipefail

root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source_dir="${ITSZULIB_DIR:-$root/../ItszuLib}"
remote="${ITSZULIB_REMOTE:-origin}"
worktrees="$root/.itszulib"

die() { echo "itszulib-ref: $*" >&2; exit 1; }

[ $# -ge 1 ] || die "usage: tools/itszulib-ref.sh <branch|tag|commit> [gradle args...] | --list | --clean"
git -C "$source_dir" rev-parse --git-dir >/dev/null 2>&1 || die "no ItszuLib git checkout at $source_dir (set ITSZULIB_DIR)"

case "$1" in
  --list)
    git -C "$source_dir" worktree list | grep -F "$worktrees/" || echo "no worktrees under $worktrees"
    exit 0 ;;
  --clean)
    for dir in "$worktrees"/*/; do
      [ -d "$dir" ] && git -C "$source_dir" worktree remove --force "$dir" && echo "removed $dir"
    done
    git -C "$source_dir" worktree prune
    exit 0 ;;
esac

ref="$1"; shift
dir="$worktrees/${ref//\//_}"

if [ "${ITSZULIB_NO_FETCH:-0}" != 1 ]; then
  echo "itszulib-ref: fetching $remote" >&2
  git -C "$source_dir" fetch --quiet --tags "$remote" || die "fetch from $remote failed (ITSZULIB_NO_FETCH=1 to skip)"
fi

commit=""
for candidate in "refs/remotes/$remote/$ref" "refs/tags/$ref" "$ref"; do
  if commit="$(git -C "$source_dir" rev-parse --verify --quiet "$candidate^{commit}")"; then break; fi
  commit=""
done
[ -n "$commit" ] || die "no branch, tag or commit '$ref' in $source_dir (remote $remote)"

if [ -d "$dir" ]; then
  if [ -n "$(git -C "$dir" status --porcelain)" ]; then
    echo "itszulib-ref: $dir has local changes; building it as it is" >&2
  else
    git -C "$dir" checkout --quiet --detach "$commit"
  fi
else
  mkdir -p "$worktrees"
  git -C "$source_dir" worktree add --quiet --detach "$dir" "$commit"
fi

echo "itszulib-ref: ItszuLib $ref at $(git -C "$dir" log -1 --format='%h %s')" >&2
cd "$root"
exec ./gradlew -Pitszulib_dir="$dir" "$@"
