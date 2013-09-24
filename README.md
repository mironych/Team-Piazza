Team-Piazza (forked from https://code.google.com/p/team-piazza/ and updated to support Team City 8)
===========

Piazza is a build monitor plugin for the Team City continuous integration server. 
Piazza provides a high-visibility display of the current state of the build to alert the team as soon as the build breaks.

For any build, Piazza displays:

    The project and build name
    The current build number
    Whether the build is "red" or "green"
    Textual success/failure indication for the colour-blind
    Whether the Team City server is currently building
    The changes that caused the current build to be kicked off
    Pictures of the team members that made those changes
    The progress of the build
    The build step that the Team City agent is currently running
    The number of passed, failed and ignored tests 


    To install plugin:
    1. Shutdown TeamCity server
    2. Copy the piazza.zip archive with the plugin into <TeamCity Data Directory>/plugins directory.
    3. Start TeamCity server: the plugin files will be unpacked and processed automatically. 
