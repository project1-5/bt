# Report for assignment 3

This is a template for your report. You are free to modify it as needed.
It is not required to use markdown for your report either, but the report
has to be delivered in a standard, cross-platform format.

## Project

Name: bt

URL: https://github.com/atomashpolskiy/bt

bt is a BitTorrent library for the Java programming language. It supports custom storage backends and many modern BitTorrent features such as magnet links out of the box.

## Onboarding experience

Building was easy and described in the README. Built fine on my Linux system using a modern JDK (OpenJDK 11).

Nikhil: Building was very easy, trying to run the Jacoco coverage suite was another question. Ended up spending multiple hours trying
to troubleshoot why Jacoco would not create coverage reports. Running the test cases with the coverage configuration pulled up
many errors for me, pointing to other maven plugins (maven-surefire-plugin). Solution was finally found by updating Jacoco version
in the pom.xml file. Very frustrating experience trying to get Jacoco to work.

## Complexity

1. What are your results for the ten most complex functions? (If ranking
is not easily possible: ten complex functions)?
   * Did all tools/methods get the same result?
   * Are the results clear?
2. Are the functions just complex, or also long?
3. What is the purpose of the functions?
4. Are exceptions taken into account in the given measurements?
5. Is the documentation clear w.r.t. all the possible outcomes?


### (2,3) ConnectionSource#getConnectionAsync:

TODO: Do a manual count of the CCN.
Purpose, CCN:

The CCN is partially inflated because of branches which are solely there for logging.
The code is fairly long, clocking in at 75 lines for one method and the method is not documented. There are
logging but it does not particularly help in understanding the code.

The purpose of the method is to either return an existing connection to a peer with a certain torrent
or to create a new one to that same peer and torrent.
This new connection is established and run asynchronously through the CompletableFuture class.
To create a new CompletableFuture a lambda is provided in the code. The branches in the lambda should
technically be counted separately, however lizard does not do so.

Code coverage:

According to JaCoCo:
0%
According to manual instrumentation:
0%

### (2,3) MetadataService#buildTorrent
Purpose, CCN:

Many branches here are because of null checks. They are also lonely if-branches without an else-part.
The code creates a torrent from the metadata from some parser (the parser holds the content of some torrent file).
There is no real documentation to establish the entirety of possible outcomes and warnings of rawtypes and unchecked exceptions are suppressed.

Code coverage:

According to JaCoCo:
0%
According to manual instrumentation:

### (4) Assignments#update
Purpose:

This function returns a set of other peers that contain pieces that are interesting to the current peer and that can be
given assignments. The CCN is high for a few reasons. There are multiple null checks, and checks to see if certain 
logging traces are enabled or not. Given a certain peer (which itself requires some branches to identify if it's a valid peer),
checking to see if it has important/interesting pieces results in multiple loop iterations and if statements.

This is a fairly long function, at 63 lines of code. Again, no documentation is given so logging wasn't super helpful by itself.

Manual cyclomatic complexity (M = pi - s + 2):
M = 17 - 1 + 2 = 18


### (8) Assignments#assign
Purpose:

This function selects a piece the current peer has and assigns it to another peer that has been given to the function.
There are multiple reasons why many branches exist in this funciton. A few have to do with the nature of the assigning
which requires constant checking of the bit iterator to see if more pieces need to be assigned. Another has to do with
seeing if certain log traces are enabled our not. No exceptions are made here, so they are not taken into account in the CCN.

This is a fairly long function, at 65 lines of code. Again, no documentation is given so logging wasn't super helpful by itself.

Manual cyclomatic complexity (M = pi - s + 2):
M = 16 - 2 + 2 = 16




## Coverage

### Tools

The project uses JaCoCo for coverage and already had it integrated with its build environment.
How to use the tool in this project was completely undocumented so at first I had to look around for
instructions on how to use the tool through Google. I did this for about 2 hours without any real gains being made.
Finally I found a Travis script in the scripts folder called "travis-run-tests.sh" which ran the tests with code coverage on.
So yes, the experience in using this tool was terrible because of a lack of in-project documentation.
The JaCoCo project's documentation however was fine.

Nikhil: Upvote to what Johan said about the poor documentation of Jacoco on the bt README. I had a bit more trouble
trying to get it to work on my Ubuntu machine. Neither the bash script nor the mvn install command inside the script
worked for me initially. Errors pointing to other maven plugins would prevent any reports from being created.
After hours and hours of troubleshooting I finally found out that simply updating the Jacoco plugin version from 
0.7.8 to 0.8.2 would work, and it did.


//Document your experience in using a "new"/different coverage tool.

//How well was the tool documented? Was it possible/easy/difficult to integrate it with your build environment?

### DYI

ConnectionSource#getConnectionAsync:
See: https://gist.github.com/jsjolen/0e98536c5817fc0502eeb954b003a7fc
MetadataService#buildTorrent:
See: https://gist.github.com/jsjolen/fb3bc47617056cac7d605e7da5f53842

You can generate the patch file for these two methods with: git format-patch -1 899854761449388bf0829fbc272e17c8276e854e


//Show a patch that show the instrumented code in main (or the unit
//test setup), and the ten methods where branch coverage is measured.

//The patch is probably too long to be copied here, so please add
//the git command that is used to obtain the patch instead

>What kinds of constructs does your tool support, and how accurate is its output?

Our manual tool consists of one single method which writes some numerical id to a file with some name.
The numerical id represents the branch taken. Assume a strong adversary, could such an adversary avoid having our method called or its output recorded in the program when a branch occurs?
As far as we can discern the answer is no. Since our method is statically called there's no way to override the method using reflection for example. Therefore the tool is 100% accurate.
However it's still a tool of limited usage because of the large amount of manual work needed to discern which branches were taken and which weren't.


### Evaluation

Report of old coverage: [link]

Report of new coverage: [link]

Test cases added:

git diff ...

## Refactoring

The goal of CCN as a metric is to measure the complexity of the codebase. However the goal should never be to reduce the CCN explicitly, because that would eliminate its usefulness as a metric.

Plan for refactoring complex code:

Many of the branches are separated into "chunks" in which each chunk has a separate concern that it's dealing with.
To reduce the complexity number the easiest way to do so would be to move out these chunks into separate methods.
However, if you have a small snippet of code which does something fairly trivial and only has a single use-case then splitting it into a method could be a bad idea. This is because it's trivial for the reader to skip over the chunk while reading the code and also because having it in a method signals to the reader that this is something that could be used in other places too.


ConnectionSource#getConnectionAsync:

The following is an evaluation of each chunk of such branches to see if they are viable for refactoring.

L88-95: Checks if there is a pending connection for the peer and torrent and if so returns it.

L98-117: Check if the peer has been deemed unreachable. If so, check if it's time to re-try connecting to the peer or if it's still considered unreachable. This is a potential refactoring, since there is a clear separation of concerns here and the unreachablePeers list is allocated per ConnectionSource instance, meaning that it makes sense to have methods related to that line of code. Here we can separate out the logic of a peer still being considered unreachable and if so remove it from the unreachablePeers list.

L119-127: Check if there's a maximum number of peer connections. Not really refactorable.

L130-137: Does the same as L88-95. Therefore refactoring into a lambda function (Consumer\<T\> perhaps?) at the top of the method (exposing it as a purely local method) might be useful.

The rest of the method is composed of a CompleteableFuture being supplied with a lambda.

ConnectionSource#getConnectionAsync#lambda:

L145-L157: Establish connection, if not succesful return its failure.
L147-152: Add the connection to the pool if absent. If not, return the currently opened connection and close new one silently.
L158-162: Remove new connection from the pending connections when created.
None of these are really refactorable.

L164-180: This chunk is concerned with what happened if the connections somehow failed. It adds to the unreachable ppers map.

On the whole the method is actually quite simple, regardless of the CCN.
Some of the branches are returning branches - they decide that the method ought to return pre-maturely for one reason or another.
What could be done for these is to implement the checks as methods, however the branches would still be there, so the CCN would not be reduced.


MetadataService#buildTorrent:

L111-L115: Ensure that the parser is of the expected read type.
L119-L127: Ensure that the metadata follows the expected model.

L134-L165: This slice of code is perfect for moving out into a method. It does one well-defined thing and it is a pure function (no side-effects).

After this a large try-block does a set of parsing and null-checking.

L175-179: Set the name of the torrent if available. Pure null-checking.
L187-190: Read the size of the torrent and fill in. Pure null-checking.
L192-215: Calculate the size of the torrent manually. This can be refactored into a separate method.
L220-240: Same null-checking logic for filling out the fields when they exist.


All the null-checking can be replace with the new Java Optional class. The issue with the branches aren't the branches themselves, but the need to remember to branch on the possibility of a null. The best way to solve this is to use an API that utilizes Optionals, this would mean that all consumers of this method's return value would get compile errors instead of runtime errors (type-error compared to NPE).

L244-269: Gather out the tracker URLs. This can easily be refactored into a method.

Assignments#update:

Like with the assign function (see below), I would abstract out all mentions and checks of LOGGER.isTraceEnabled. There are only 2 in this function,
but 1 of them is within a loop. Additionally, there are 2 separate if statements that check to see if a specific object is null. One checks to see if
the object (queue) is null, and the other checks to see if it's not null. Creating this null check only once and when it is immediately initialized can 
reduce the need to check it again later. There is also a check for the size of the object (queue) that is done twice. Abstracting that out as well 
(assuming the object is not null) can prevent the function from needing to check it twice.

Assignments#assign:

The first thing I would do with this function, is abstract out all mentions and checks of LOGGER.isTraceEnabled. This check is made at least 6 times
in the function, 4 of which are in a do-while loop. Removing that if statement alone and calling it once in a helper logging function can simplify the code.
I would also create a separate function for the iteration of pieces (peers) as it is already nested in the else block of the endgame check. In fact, a return could be
made after identifying a peer as endgame to avoid ever having to walk into the else branch.

Carried out refactoring (optional)

## Effort spent

For each team member, how much time was spent in

1. plenary discussions/meetings;
- Johan: 2 hours
- Nikhil: 2 hours
- Jagan: 2 hours
- Tom: 0 hours (was ill)

2. discussions within parts of the group;
- Johan: 10 minutes or so
- Nikhil: ~20 minutes asking about Jacoco

3. reading documentation;
- Johan: 1 hour
- Nikhil: ~30 min on bt, 1 hour on Jacoco

4. configuration;
- Nikhil: 4 hours (Spent 2 days trying to configure Jacoco)

5. analyzing code/output;
- Johan: 4 hours
- Nikhil: 1 hour

6. writing documentation (writing report?));
- Johan: 5 hours
- Nikhil: 2.5 hours

7. writing code;
- Johan: 6 hours
- Nikhil:

8. running code?
- Johan: Many hours?
- Nikhil: Not sure what this refers to but see above for more accurate division

## Overall experience

What are your main take-aways from this project? What did you learn?

Is there something special you want to mention here?
