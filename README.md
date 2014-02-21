transaction-helper
==================

## Introduction

Transaciton Helper is a OSGi-DS component that makes it simple to use
transaction propagation in the code. The component registers the
TransactionHelper OSGi service that has the well known propagation
functions (requires, requiresNew, never, ...).


## Example Usage (Java 6 and Java 7)

    Integer result = transactionHelper.requiresNew(new Callback<Integer>() {

        @Override
        public Integer execute() {
            // Do some stuff in the new transaction
        }
    });

## Example Usage (Java 8)

    Integer result = transactionHelper.requiresNew(() -> {
        // Do some stuff in the new transaction
    });

## Why not annotations, interceptors or other magic?

By using this transaction helper, the trace of the code is always clear. We
used Spring interceptors, Spring annotations, Aries interceptors in the past,
but we decided not to use such "magical" solutions anymore.

Imagine the technology uses java proxies. In that case the programmer of the
class cannot be sure in which way the call stack came. Did the call come
from the same object but via a method that was not intercepted? Did it
come via a method that was intercepted? As a solution, many programmers
start intercepting all of the public methods of their class that is a very
bad behavior.

Ok. We found out that there is ASM, CGLIB, whatever... We could use them
to inherit from the class and with that, we can override all of the public
and protected functions. Problem solved. Interception is done during all
function call. What about the private functions?

Ok. We found out that there is Weaving. Private functions can be intercepted
as well. It does not matter if the system starts up really slow. It does not
matter that the hash of the class is not the same as it is at compile time.

What if I want to wrap a block of code inside an iteration with requiresNew
transaction propagation? Do we really have to create a new function for that?
And even if We do so, We cannot be sure that the new function is intercepted
if the technology uses simple Java Proxy classes.

What about the bytecode compatibility? Will version X of ASM work with
version Y of Java? Well, we saw it a couple of times that it did not. So we
had to wait for a new version of ASM to be able to use a bugfix version of
Java.

And especially in the OSGi world: What makes you sure that the bundle that
does the weaving is started before your bundle that should be weaved?

Is it so much to ask to use an anonymous class? There will be more code, but
still the code will be much more clear! It will be yours. With Java 8, the
code will be even more nice if you use lambda expressions.