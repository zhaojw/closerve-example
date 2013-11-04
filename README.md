closerve-example
================

Example about using closerve, a view first web framework in Clojure inspired by
[Lift web framework](http://liftweb.net)

Just like the Django mark up language is used by many non-python web frameworks, 
since Lift is the de Facto standard for view first framework, Closerve try to mark
HTML using the same “lift:” identifier. This will provide some reusablility of HTML files. 

This example included how to use lift:surround, lift:embed, lift:LazyLoad, lift:form.ajax, lift.comet,
which are currently implemented in CloServe. The other features of Lift like screen, wizard, wiring, 
proto user, etc are not implemented yet.

Though the names are similar to Lift, there are differences here and there. For the form.ajax
case, one parameter "callback" need to be given, which corresponds to a piece of code that defind
how to process form data. It is demonstrated in the examples.

Contrubution by:

- [Jiawei Zhao](https://github.com/zhaojw)
- [MKRRF IT Limited](http://www.mkrrf-it.com), provided some of the code used in [CloEDIT](http://www.mkrrf-it.com/cloedit/)

###Usage

First, you need the [closerve snapshot](https://github.com/zhaojw/closerve) from github,

```
git clone https://github.com/zhaojw/closerve.git
```

Then compile and install it to your local mvn repo:

```lein cljsbuild clean```

```lein compile```

```lein install```

Checkout the clojure-example to another dir:


```
git clone https://github.com/zhaojw/closerve-example.git
```

Then you can try it as:

```
lein run ./webroot port 8888
```

This will start a jetty server listening to 127.0.0.1 on port 8888. You can make it listen
to all interfaces with additional:

```
host 0.0.0.0
```

parameter.

## License

Copyright © 2013 Jiawei Zhao, MKRRF IT Limited

Distributed under the Eclipse Public License, the same as Clojure.
