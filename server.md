# Serving Game

Some notes on the tech I can try out to serve the game.

The server will do two things:
- Serve HTML for the game to start
- Run the server to mediate the game
- Aim is to write all of this stuff in clojure

## Server
- HTML / Page hosting
    - Static stuff served bt nginx]
    - Simple services proxied to a clojure server
        - REST to database for login etc
        - Auth with twitter / google / facebook?
- Game Serving
    - Direct connection
    - Websockets on client?
    - Does that map to direct TCP?
    - Not sure what best platform is
        - Clojurescript on node
        - Clojure on jvm
        - Suspect the latter

# What I want
I want to be able to:
- To be able to run and develop on the same server type that I would deploy to
- To be able to have a production and development server
- Servers
    - Stable
    - Beta
    - Live (staging or testing?)
- For my local environment and content to be moevable to the AWS server without any content change
- Or risk that there may need to be any content change

# Potential solutions
- Vagrant to replicate the OS and machine I would use
    - Need an OS that I can use on vagrant
    - And will install to AWS
- Scripting on ````vagrant up```` that would set up the env needed
    - Puppet
    - Chef
    - Vagrant's shell script
    - Different levels of complexity
    - Not sure what I need / can get away with
- Maybe Docker
    - Sits on top of vagrant
    - Should remove any variation between AWS and Vagrant OS
        - Even though they're the __same__ they're probably not the same
        - Docker env only has what's needed for deployment
        - And that's described in a deployable container
- So
    - Ubuntu LTS 14.04 server
    - Docker
    - A cheg server container
    - On my dev machine
        - Simplest vagrant up ever (no deployment)
        - Server running a container with everything I need installed for dev
        - Edit my code (which is in a shared dir)
        - Dev env on dockerised machine restart / runs

## Vagrant
- LTS 14.04 Ubuntu
- Docker
    - Packaged up app with lein, clojure, jdk, web server, etc
    - Shared dir from my machine?
    - Dev docker running lein and own server
    - Or Nginx but certain routes that provide services proxied to clj server?
    - Only configuration differences are my source
