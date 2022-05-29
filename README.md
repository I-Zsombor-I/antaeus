## My solution

###Working time 
I cannot say precisely how much work I did, but it was around 18-22 hours. I didn't really work with Kotlin before, 
so basically everything was new. A lot of time went on getting to know Kotlin, and also my computer being slow in general:)

###Notes on my solution

My first thoughts on the problem was that, it is not enough to do the payment, but I have to do something with the results as well.
If there is some kind of issue with the payment, someone at some point should do something about it. 
One solution would have been to add new invoice statuses reflecting the payment errors.
I took another route, I left the base models as I received them, and introduced a service, SummaryService, which returns the results of the last billing.
This is a simple service, the returned data is basically a list of strings, I didn't want to do anything sophisticated, because it reflects the idea in this state as well.

Also, I introduced another "external" service, and AlertingService which would notify another part of the system, that some error has occurred
and something should be done.  

I was thinking of introducing other Invoice statuses when payment is unsuccessful, but in the end I decided not to, because
the BillingService cannot really do anything about those errors, just retry execution. The SummaryService can provide the list of
problematic invoices, and "in theory" I put an alerting service in, so the errors are advertised. Of course, the payment summaries could be
saved to the db, but I didn't wanted to go that big:), but that could be a helpful addition to have a record of the payment procedures
(but maybe the PaymentProvider already does that in the other end)

Still, in this context, it could be useful to have new invoice statuses for the possibility of a more fine-grained re-billing attempts for problematic invoices,
for example retrying only the ones where there was a network error. So, it does not mean that new invoice statuses could not be added, and it wouldn't really be such a big change in the code.

####Scheduling

I put the scheduling part of the billing call into a separate service. This is not a totally general service, for example the monthly period could be
a configurable property as well, and even what function call is scheduled could be passed as argument. This is the first time I worked with coroutines, so
hopefully I did something usable. In general, I tried to solve stuff "the Kotlin way"

####Rest
I added a couple of new endpoints to be able to call these new services.

####Remaining work

There are still a variety of things which could be added. A lot more tests, for the data layer, integration tests and 
end-to-end tests as well, as well as making the services, results more sophisticated, but I decided to stop at this point in my work.
Some of the logging messages could be changed to debug, if we have a large number of invoices, we don't want to fill the logs.
Also, calling the payment provider could be done in parallel for multiple invoices, if we know how the external service behaves.

Anyways, I learned a lot about Kotlin during this assignment and thanks for looking at my code:)

## Antaeus

Antaeus (/ænˈtiːəs/), in Greek mythology, a giant of Libya, the son of the sea god Poseidon and the Earth goddess Gaia. He compelled all strangers who were passing through the country to wrestle with him. Whenever Antaeus touched the Earth (his mother), his strength was renewed, so that even if thrown to the ground, he was invincible. Heracles, in combat with him, discovered the source of his strength and, lifting him up from Earth, crushed him to death.

Welcome to our challenge.

## The challenge

As most "Software as a Service" (SaaS) companies, Pleo needs to charge a subscription fee every month. Our database contains a few invoices for the different markets in which we operate. Your task is to build the logic that will schedule payment of those invoices on the first of the month. While this may seem simple, there is space for some decisions to be taken and you will be expected to justify them.

## Instructions

Fork this repo with your solution. Ideally, we'd like to see your progression through commits, and don't forget to update the README.md to explain your thought process.

Please let us know how long the challenge takes you. We're not looking for how speedy or lengthy you are. It's just really to give us a clearer idea of what you've produced in the time you decided to take. Feel free to go as big or as small as you want.

## Developing

Requirements:
- \>= Java 11 environment

Open the project using your favorite text editor. If you are using IntelliJ, you can open the `build.gradle.kts` file and it is gonna setup the project in the IDE for you.

### Building

```
./gradlew build
```

### Running

There are 2 options for running Anteus. You either need libsqlite3 or docker. Docker is easier but requires some docker knowledge. We do recommend docker though.

*Running Natively*

Native java with sqlite (requires libsqlite3):

If you use homebrew on MacOS `brew install sqlite`.

```
./gradlew run
```

*Running through docker*

Install docker for your platform

```
docker build -t antaeus
docker run antaeus
```

### App Structure
The code given is structured as follows. Feel free however to modify the structure to fit your needs.
```
├── buildSrc
|  | gradle build scripts and project wide dependency declarations
|  └ src/main/kotlin/utils.kt 
|      Dependencies
|
├── pleo-antaeus-app
|       main() & initialization
|
├── pleo-antaeus-core
|       This is probably where you will introduce most of your new code.
|       Pay attention to the PaymentProvider and BillingService class.
|
├── pleo-antaeus-data
|       Module interfacing with the database. Contains the database 
|       models, mappings and access layer.
|
├── pleo-antaeus-models
|       Definition of the Internal and API models used throughout the
|       application.
|
└── pleo-antaeus-rest
        Entry point for HTTP REST API. This is where the routes are defined.
```

### Main Libraries and dependencies
* [Exposed](https://github.com/JetBrains/Exposed) - DSL for type-safe SQL
* [Javalin](https://javalin.io/) - Simple web framework (for REST)
* [kotlin-logging](https://github.com/MicroUtils/kotlin-logging) - Simple logging framework for Kotlin
* [JUnit 5](https://junit.org/junit5/) - Testing framework
* [Mockk](https://mockk.io/) - Mocking library
* [Sqlite3](https://sqlite.org/index.html) - Database storage engine

Happy hacking 😁!
