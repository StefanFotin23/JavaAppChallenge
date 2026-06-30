### Deploy the app:
    docker-compose up --build -d

#### Unit tests:
    mvn clean test

#### Formatting and checkstyle:
    mvn spotless:apply

### Personal Opinion
&nbsp;&nbsp;&nbsp;&nbsp;First of all, this project was done in an extensive way, I tried to have everything well done,
in detail, even if the initial requirements were probably lighter. It took me a few hours, mainly on some decision-making,
implementation but also some time spent on testing end to end and debugging. I used this extensive approach because at work I use
mainly traditional Java with Spring, focusing on database queries, liquibase, a lot of debugging, solving defects, and I
used this application as a chance to experiment and get more comfortable with these approaches.
&nbsp;&nbsp;&nbsp;&nbsp;I did my research using LLMs mostly, asking for approaches, why and how does every solution works
and did choose the one I thought it suits best, regarding the parts where I was less-experienced. Mainly inside the
shared package, where I needed to provide configs for the general usage of libraries. I also experimented more with Mappers, records.

## Architectural and Implementation Details:
#### Architecture
&nbsp;&nbsp;&nbsp;&nbsp;Most of the people tend to use a simple Architecture with Controller, Service, Repository and
DTOs layer, but I wanted to experiment more on Domain Driven Design. Afterward, I did run curl on the Swapi API url in
order to get information about the data model.\
&nbsp;&nbsp;&nbsp;&nbsp;Then I focused on the SwapiClient and defined the DTOs needed. I've also used records, which I
didn't have much experience, but have found them very useful. Then I did the Controller layer and the DTO that needs to
be sent to the client (Swagger). So basically I saw that the data model from SwapiClient can be directly stored and
cached using Redis in my desired format. Didn't want to have all fields as string in CharacterDTO and also parsed the
mass and height as Integers, because most of the data was of this format and actually none used height for example as
179.8cm, everyone says 180cm or 1.80m. It was my personal choice, because the "business" requirements were not that
explicit.\
&nbsp;&nbsp;&nbsp;&nbsp;The SwapiClient is encapsulated and the SwapiRepository just makes calls using the client and in
this layer caches data using Redis.\
&nbsp;&nbsp;&nbsp;&nbsp;The Service layer acts as the middleman between controller and repository, where I just used
simple calls of repository and mapper, to have better segregation of concerns and readability. Also, here I do the Redis
manual interaction for favourites section, which can be further segregated into its own service, of course if the
project gets bigger, it's probably the next step that I would do in future work.\
The authentication part was kind of tricky, because I had the chance to implement low level JWT and OAuth2 in faculty
projects, but not in an actual modern Spring Application. The idea is that I wanted to comply with the "write less, use
already established solutions for security" ideology and just fine-tune, config the solutions that I brought up.\
&nbsp;&nbsp;&nbsp;&nbsp;The shared package is very interesting, because I didn't have before the chance to fully implement
this into a Java app, so I did there most of the general libraries configs, settings etc. For example Redis time-to-leave,
cause of course, I had the common concern of "Alright, @Cacheable caches everything, but I want to actually not overflow
the memory, don't waste it etc.".\
&nbsp;&nbsp;&nbsp;&nbsp;Also, my concerns of implementation on most of the aspects were thinking of the app to run in a
cluster on multiple pods, that's why I needed to have a common Secret for JWT generation, to have limits for Redis
(probably can be done from infrastructure side too...but it's better to have them done directly in Java or container
level, not on the node or pod, mainly because I really don't have clusters, pods, nodes here...so yeah, it's plug and play now)
The deployment part it's easy but 100% automated, using docker-compose. I created a standard Dockerfile for a small 
java app and a kind of simple docker-compose.yml, adding the memory constraints and eviction policy for Redis and java
app environment variables. Didn't use Secrets, of course, it's not the point of the project, but used the info in clear,
inside a .env. Also have application.yml, which did decouple some configs, so it's not everything hardcoded and the app
can be run let's say on multiple deployments with different configs and some of them were also pointed outside, to the
Docker container, but again, wasn't the point to do it for all of them, like in a production setup.\
&nbsp;&nbsp;&nbsp;&nbsp;Also used LLMs for general feedback on some aspects and also for generating UnitTests, providing
the expected template that I wanted, with Mocks, InjectMocks, focusing on test coverage, which is also kind of good,
but not really production grade. Would be a point in future work.\
&nbsp;&nbsp;&nbsp;&nbsp;From my point of view, my work and decisions aimed to have the least trade-offs or problems, I
think I used the best practices in terms of architecture, solutions, maintainability, readability, separation of
concerns etc. Can't really find problems, more like "future work" options to implement if project extends.

#### Future Work
Would increase the Test Coverage
Would extend even more the separation of concerns, for example segregate RedisService, that handles all abstract manual
Redis work.