#!/bin/sh
# Enforces the branch/version policy from guidelines.md:
#   - master must never be a SNAPSHOT version
#   - develop must always be a SNAPSHOT version
#
# Used both as a local git hook (see .githooks/pre-commit) and as a CI
# check (see .github/workflows/java-build.yaml).
#
# usage: check-branch-version.sh <branch> [pom.xml]

set -e

branch="$1"
pom="${2:-pom.xml}"

if [ -z "$branch" ]; then
	echo "usage: $0 <branch> [pom.xml]" >&2
	exit 2
fi

version=$(grep -m1 -oE '<version>[^<]+</version>' "$pom" | sed -e 's/<version>//' -e 's/<\/version>//')

case "$branch" in
	master)
		case "$version" in
			*-SNAPSHOT)
				echo "ERROR: $pom version on branch 'master' must not be a SNAPSHOT (found $version)" >&2
				exit 1
				;;
		esac
		;;
	develop)
		case "$version" in
			*-SNAPSHOT) ;;
			*)
				echo "ERROR: $pom version on branch 'develop' must be a SNAPSHOT (found $version)" >&2
				exit 1
				;;
		esac
		;;
esac

exit 0
