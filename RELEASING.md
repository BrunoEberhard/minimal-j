This text file describes how release management is done in minimal-j

Branches

The master branch must contain a stable release. Some releases are deployed to maven repostory
with profile 'sonatype-release'.

The development branch must contain a SNAPSHOT release based on the last stable release in the master branch.

Builds

The versions in the master branch can be updated manually. When the master branch is pushed to github then
the version is build and the result is deployed to the github packages.

Official releases are build with profile 'sonatype-release' with release:prepare and release:perform

In the development branch there must be only SNAPSHOT version. When it's pushed to github it should be build
there and a package with SNAPSHOT datestamp should be created.

Manual local release to Sonatype / Maven Central

Releases are triggered manually, from the developer's machine, on the master branch.

Before starting:
- Everything intended for the release must already be merged from develop into master:
    git checkout master
    git pull origin master
    git merge develop
  Do not push this merge yet. Right after the merge, master's pom.xml is a SNAPSHOT (inherited
  from develop), which would fail the branch/version policy (see guidelines.md) if pushed as its
  own commit - CI rejects a SNAPSHOT on master, and so does the local opt-in pre-commit hook, if
  you have it enabled. Leave the merge commit local; it gets pushed together with the
  release-version commit in step 1 below, by which point the version is no longer a SNAPSHOT. If
  the local hook is enabled and rejects the merge commit itself, that one commit can be made with
  git commit --no-verify - it's a transient local state, immediately superseded by release:prepare.
- pom.xml on master must be on a SNAPSHOT version after the merge (the maven-release-plugin
  requires this to know what version to release) - the merge above satisfies this as a purely
  local, not-yet-pushed state.
- ~/.m2/settings.xml must contain a <server> entry with id 'central' holding the Sonatype Central token
  (used by the central-publishing-maven-plugin, publishingServerId 'central'), and a GPG key must be
  available locally, since the sonatype-release profile signs every artifact with maven-gpg-plugin.
- The working copy must be clean, no uncommitted changes.

Release steps, run from the master branch:

1. mvn release:prepare -Psonatype-release -DupdateWorkingCopyVersions=false
   Interactively asks for the release version and the tag name. To run it non-interactively pass them
   explicitly, for example:
     mvn release:prepare -Psonatype-release -DupdateWorkingCopyVersions=false -DreleaseVersion=2.38.0.0 -Dtag=minimalj-2.38.0.0
   By default release:prepare creates two commits on master: one setting the release version, and a
   second '[maven-release-plugin] prepare for next development iteration' commit that bumps the pom
   back to a SNAPSHOT. That second commit must be suppressed with -DupdateWorkingCopyVersions=false,
   otherwise master ends up on a SNAPSHOT version afterwards, which violates the branch policy above.
   With the flag set, only the release-version commit is made, the tag is created, and (by default)
   the commit and tag are pushed to origin.

2. mvn release:perform -Psonatype-release -Darguments=-Psonatype-release
   Checks out the tag into target/checkout and runs 'deploy' there, which builds, signs (gpg) and uploads
   the artifacts to the Sonatype Central staging area. The -Darguments=-Psonatype-release is required
   because release:perform runs the actual build in a separate, forked Maven process which does not
   inherit the -P flag given on the command line.

3. Open https://central.sonatype.com/publishing, find the new deployment and click 'Publish'. The
   sonatype-release profile does not auto-publish, this last step has to be done manually.

4. If release:prepare/perform did not push automatically (pushChanges disabled, or the push failed),
   push the commits and tag manually:
     git push origin master --follow-tags

After the release, merge master back into develop to pick up whatever code changes were merged into
master for the release (master no longer bumps its own version, so this merge only brings code, not
a version change):
    git checkout develop
    git merge master
    git push origin develop
  If pom.xml conflicts on the version line, keep develop's own SNAPSHOT version (develop tracks its
  own next version independently of whatever was just released from master).
