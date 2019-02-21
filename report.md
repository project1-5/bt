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

```
 !!!! Warnings (cyclomatic_complexity > 15 or length > 1000 or parameter_count > 100) !!!!
================================================
  NLOC    CCN   token  PARAM  length  location
------------------------------------------------
     181     46   1325      0     285 PullMetaDataConnection::processInput@329-613@./bt-dht/the8472/mldht/src/the8472/bt/PullMetaDataConnection.java
     113     33    787      1     132 PrettyPrinter::prettyPrintInternal@83-214@./bt-dht/the8472/mldht/src/the8472/bencode/PrettyPrinter.java
     113     32   1214      3     148 MessageDecoder::parseResponse@162-309@./bt-dht/the8472/mldht/src/lbms/plugins/mldht/kad/messages/MessageDecoder.java
     107     27   1187      3     137 MessageDecoder::parseRequest@325-461@./bt-dht/the8472/mldht/src/lbms/plugins/mldht/kad/messages/MessageDecoder.java
     103     21   1079      2     150 RPCServer::handlePacket@375-524@./bt-dht/the8472/mldht/src/lbms/plugins/mldht/kad/RPCServer.java
      76     21    596      0     106 TorrentFetcher::FetchTask::connections@501-606@./bt-dht/the8472/mldht/src/the8472/mldht/TorrentFetcher.java
     136     21   1122      4     196 MSEHandshakeProcessor::negotiateIncoming@291-486@./bt-core/src/main/java/bt/net/crypto/MSEHandshakeProcessor.java
      75     20    478      2      81 ConnectionSource::getConnectionAsync@85-165@./bt-core/src/main/java/bt/net/ConnectionSource.java
     120     19   1066      1     158 MetadataService::buildTorrent@109-266@./bt-core/src/main/java/bt/metainfo/MetadataService.java
     108     19    668      0     143 Server::accept@96-238@./bt-dht/the8472/mldht/src/the8472/mldht/cli/Server.java
```

We only used Lizard and the results are clear.



2. Are the functions just complex, or also long?
Answered below.
3. What is the purpose of the functions?
Answered below.
5. Is the documentation clear w.r.t. all the possible outcomes?
Answered below.

4. Are exceptions taken into account in the given measurements?
No.


### (2,3,5) ConnectionSource#getConnectionAsync (Johan):

The CCN is partially inflated because of branches which are solely there for logging.
The code is fairly long, clocking in at 75 lines for one method and the method is not documented. There are
logging but it does not particularly help in understanding the code.

The purpose of the method is to either return an existing connection to a peer with a certain torrent
or to create a new one to that same peer and torrent.
This new connection is established and run asynchronously through the CompletableFuture class.
To create a new CompletableFuture a lambda is provided in the code. The branches in the lambda should
technically be counted separately, however lizard does not do so.

Code coverage before:

According to JaCoCo:
0%
According to manual instrumentation:
0%

Code coverage after:

According to JaCoCo:
16% branches, 33% code
According to manual instrumentation:
3/10 branches taken

### (2,3,5) MetadataService#buildTorrent (Johan):

Many branches here are because of null checks. They are also lonely if-branches without an else-part.
The code creates a torrent from the metadata from some parser (the parser holds the content of some torrent file).
There is no real documentation to establish the entirety of possible outcomes and warnings of rawtypes and unchecked exceptions are suppressed.

Code coverage:

According to JaCoCo:
86% branches 85% code
According to manual instrumentation:
13/17 branches taken


### (2,3,5) MessagingAgentCompiler#compileType (Jagan Moorthy):
The function is pretty long(60 lines). 
The function goes through a given class' functions and check if they have either(and not both) of the two given annotations, and check if such annotated functions are public, and places them into corresponding collections passed as parameters. It returns the count of such functions.
Not much documentation.


Branch coverage:
Before: 9/11 (Assuming Loggers are covered in debug mode)
After: 11/11



### (2,3,5) RarestFirstSelectionStrategy#getNextPieces (Jagan Moorthy):
50 line function, which can be broken down.
The function gets statistics per torrent piece and a piece selection criteria as the input and returns a said number of pieces. In addition to the passed criteria, it selects the least frequent peices in a random way.
No documentation available in the overridden function, but the base class has some info.

Branch coverage:
Before: 6/9 (Assuming Loggers are covered in debug mode)
After: 9/9




### (2,3,5) HttpTracker#urlEncode
Purpose:

This function returns a string that is url-friendly. Given a string to encode into a url, the function will convert the
string into a url-friendly format, taking out characters that cannot be in a url. This function is used to ultimately build
a url with a string builder in the HttpTracker class.

This is a fairly short function, at 28 lines of code.

Manual cyclomatic complexity (M = pi - s + 2):
M = 12 - 1 + 2 = 13



### (2,3,5) ByteChannelReader#sync
Purpose:

This function is crucial in the process of UDP communications. The purpose of this function is to make sure that the 
number of bytes actually read into a buffer is equal to the number of bytes expected in the communication process. This 
is done by continually reading into a buffer until you no longer can. Then you continuously check to see if you exceeded the 
number of bytes expected to sync the expected with actual. Exceptions are thrown if it is not synced properly. 

This is a fairly long function, at 74 lines of code. Again, no documentation is given so logging wasn't super helpful by itself.

Manual cyclomatic complexity (M = pi - s + 2):
M = 16 - 1 + 2 = 17



## Coverage

### Tools

Johan: The project uses JaCoCo for coverage and already had it integrated with its build environment.
How to use the tool in this project was completely undocumented so at first I had to look around for
instructions on how to use the tool through Google. I did this for about 2 hours without any real gains being made.
Finally I found a Travis script in the scripts folder called "travis-run-tests.sh" which ran the tests with code coverage on.
So yes, the experience in using this tool was terrible because of a lack of in-project documentation.
The JaCoCo project's documentation however was fine.


Jagan:
I used Intellij's inbuilt code coverage plug-in to avoid hassles. It was very well documented. I got to use Mockito for the first time and it was a good exposure.

Nikhil: Upvote to what Johan said about the poor documentation of Jacoco on the bt README. I had a bit more trouble
trying to get it to work on my Ubuntu machine. Neither the bash script nor the mvn install command inside the script
worked for me initially. Errors pointing to other maven plugins would prevent any reports from being created.
After hours and hours of troubleshooting I finally found out that simply updating the Jacoco plugin version from 
0.7.8 to 0.8.2 would work, and it did.



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

Report of old coverage:

The old coverage by JaCoCo can be found in the oldcoverage/ directory.
ConnectionSource#getConnectionAsync:
MetadataService#buildTorrent:

Jagan:
I used IntelliJ's inbuilt code coverage tool, as it seemed to avoid a lot of hassles and was documented pretty clearly. The results of my coverage(both before and afer) are available as images under the oldcoverage/methodName directory

Report of new coverage: [link]
The new coverage by JaCoCo can be found in the respective target/ directories.
ConnectionSource#getConnectionAsync:
MetadataService#buildTorrent: Not applicable

Test cases added:
ConnectionSource#getConnectionAsync: Two: Check for unreachable peers and already existing connections
MetadataService#buildTorrent: None

MessagingAgentCompiler#compileType: 2 test case for non-public function and a function with both annotations assigned
RarestFirstSelectionStrategy#getNextPieces: One test case to handle the randomized selection branch.


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

ByteChannelReader#sync:

The main issue with this function with respect to cyclomatic complexity is the fact that there are so many conditions
to check for to figure out whether the function should throw an exception or continue reading bytes with the do while loop.
Many times, I find it checking to see if the read buffer exceeds or is limited by some static counter in conjunction with 
some secondary condition. Many of these can be extracted out to another helper function that lowers the complexity number
due to the removal of the && and ||.


HttpTracker#urlEncode:

The main reason why this function is so high in cyclomatic complexity is because for every character in the given string,
we check to see if the ascii value represents a digit OR a lowercase letter OR a uppercase letter OR another valid character.
All these extra conditionals in the if statement result in a high complexity. The main way we can reduce this is by creating
a map that maps the range of ascii values to itself if it's a valid url character or some other value if it's an invalid character.
This cost is trivial as insert in a hash map is O(1) and it reduces complexity as we only need a simple map lookup instead of an if statement.


RarestFirstSelectionStrategy#getNextPieces:
Note: The line numbers mentioned are with the manual code coverage logging lines included.
The first level of breaking up would be to take off lines 114 to 129 as a separate function for randomization.
The next level can be would be to isolate the logic in lines from 89 to 96 as it iterates through the piece stats to "pack" certain pieces.

MessagingAgentCompiler#compileType:
Note: The line numbers mentioned are with the manual code coverage logging lines included.

1. We can move the validation of function annotations and access modifiers 127-134 to a validateMethod() function
2. The remaining part is primarily clogged with logging and is a simple enough if-else condition, but given the similarity in the content, it has a minor scope for code re-use by having another method that can be called within the if and else(But the called function will in-turn have an if-else and may take us back to square one).





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
- Nikhil:  around 20 minutes asking about Jacoco

3. reading documentation;
- Johan: 1 hour
- Nikhil: around 30 min on bt, 1 hour on Jacoco
- Jagan: 30 mins

4. configuration;
- Nikhil: 4 hours (Spent 2 days trying to configure Jacoco)

5. analyzing code/output;
- Johan: 4 hours
- Jagan: 6 hours. Was struggling with test cases before using Mockito, as I tried to build all dependency objects myself.
- Nikhil: 3 hours

6. writing documentation (writing report?));
- Johan: 6 hours
- Nikhil: 2.5 hours
- Jagan: 1 hour


7. writing code;
- Johan: 10 hours
- Nikhil: 4 hours
- Jagan: 3 hours

8. running code?
- Johan: Many hours?
- Nikhil: Not sure what this refers to but see above for more accurate division

## Overall experience

What are your main take-aways from this project? What did you learn?


CCN seems to be a good metric to check for possible potentially buggy code. It's good practice to ensure functions with high CCNs are 
  1. Refactored into possible chunks, if possible
  2. Else, adequately unit tested to ensure future code doesn't break stuff

Though code coverage is a reasonably good method, it's not 100% fool-proof and can still be worked around due to short-circuits in conditions(especially weak if the focus is to only write test cases for high CCN functions ).

Is there something special you want to mention here?


## P Plus Johan Sjölén

The code is available here:

https://github.com/project1-5/bt/tree/feat_35_pplus

The tests are in bt/bt-tests/src/test/java/bt/net/ConnectionSourceTest.java
The refactorings are in: bt/bt-core/src/main/java/bt/net/ConnectionSource.java

and in: bt/bt-core/src/main/java/bt/metainfo/MetadataService.java

Refactorings:

MetadataService#buildTorrent
Old CCN: 19
New CCN: 11
Diff: 57% of original

ConnectionSource#getConnectionAsync
Old CCN: 20
New CCN: 14
Diff: 70% of original

Diff together: 64% of original (36% reduction)

Unit tests:

4 tests in ConnectionSource#getConnectionAsync (MetadataService#buildTorrent had very high coverage)


