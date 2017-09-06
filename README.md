# java_lazy_init

Safely lazy initialize class members under multithreaded environment

Here we explore a way to initialize class member variables under concurrent execution scenarios. If the needed information for initialization is available at object creation time; then the obvious way would be to do the initialization in the constructor of the class with the member being “private final”. However; at times the needed information for initialization is not available at object construction time or we might want to delay initialization of costly/complex objects in a lazy fashion i.e. until the member variable is really required for the current operation to be performed on the object. The tricky part under such a situation is that if the application is multithreaded (which anyways is the de facto scenario now a days) then we must make sure that the member is initialized once and only once and it be made visible to all participating threads. Common way to ensure that is by putting the initialization code under a synchronized block with the downside that for every access of the lazily initialized member variable we’d need to incur the cost of acquiring the lock with regards to the synchronized block. One way to minimize the locking impact is to do a double-checked locking technique which as we know is best avoided for its inherent unsafe nature as it’s tricky to get it right!

In my current project; I had come across quite a few places where lazy initialization was needed. Initially I started off with “double checked locking” ensuring that it was implemented right! However; when same pattern started popping up at multiple places in the code base, I felt the need to encapsulate the initialization logic. At a high level I looked for following properties:

-	Initialization must happen once and only once. For some situations; this may not be critical (e.g. creating 2 instances and letting one go away/GC-ed and continue holding the other), but for others e.g. where initialization process makes some persistence changes, the “once and only once” semantic is essential.
-	No thread can perform an operation without the class member being initialized. In other words; if 2 or more threads are trying to initialize it concurrently, with one getting the “right” to initialize; the others must wait until the first one is done with initialization.
-	Safely publish the mutation of the lazily initialized member to all participating threads.

In this regard, I came up with an “One Time Executor” semantic. In other words 
-	A wrapper object which wraps a function F. In current context F is a function/lambda expression which holds the initialization code.
-	The wrapper provides an execute method which behaves as
o	Calls the function F the first time execute is called and caches the output of F.
o	If 2 or more threads call execute concurrently, only one “gets in” and the others block till the one which “got in” is done.
o	For all other/future invocations of execute, it does not call F rather simply returns the previously cached output.
-	The cached output can be safely accessed from outside of the initialization context.

[Note: From above context and usage, it’s clear that the lazily initialized object must itself be thread safe since multiple threads would be accessing it concurrently.]

The utility class is OneTimeExecutor and there are 4 sample classes demonstrating its different usage.
