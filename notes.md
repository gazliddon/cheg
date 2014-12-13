# Today
* Change the state change to happen once
* Write up some notes about tracking state at a higher order
    * Not x y xv yv
    * Events
        * Start time
        * Set of functions
        * Initial information needed by functions

An object is represented by fucntion f that I can apply time (t) to and receive a representation of it's state. 

```
s = f(t)
```

Function f is created by g, a function that takes the objects current state, some sort of event and returns a new function

```
f' = g(s)
```

If s has x,y,xv,yv







p = p + f(0)
f(t0 + t1) = f(t0) + f(t1) 


event = stick right went down

g(f,t, stick_right)


g(f,t) = fn(t') f(t) + t' * right_vel


gvel(p,vel) = fn(t') = p + t' * vel


ev = 

    * Objects are represesent by a function f
    * I can get the position by applying f to time
    ````p = f(t)
    * A new f is created by applying function g to the current f at a time with an event
    ````f' = g(f, t, event)


* Check out lein-pdo for the work environment
* What about boot for clj building
* Write follow up post on chestnut github options page
* Can I build a release version of this project and deploy it?
* What can I deploy it to? An docker instance?
    * So that would mean LTS 14.04 running on vagrant on this machine
    * With a chef or puppet script to do the installation of docker
    * And something that can make a docker instance on the host
    * And deploy it to the vagrant machine
    * And eventually to the AWS docker machine



# Todo
- Move cheg over to latest generated project.clj by chestnut
- Get cljs tests working

# Waiting
- Get cljx tests working

# Todone
- Update vimrc in github to be the same as on my mac
- Default highlight to be off
- Wrote up some notes about what tech I can use for serving



