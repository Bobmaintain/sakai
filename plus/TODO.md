
TODO LIST
=========

Figure out UI/UX best practice for pretty CRUD UIs - talk to Adrian / Earle

Put context memberships into a thread and then into a batch job

Put lineitem creation and score sending into database for redo on failure and add batch job

Build pretty UI for errors - add forward to knowledgebase feature

Make the mini / ultra portal for tool launches

Make a single tool content item request and Canvas config

Build IMS auto-configuration

THINGS TO RESEARCH
------------------

LIMIT 1 im Repository lookup's when there is a unique constraint
https://www.baeldung.com/jpa-limit-query-results

Embedded ID
https://stackoverflow.com/questions/2611619/onetomany-and-composite-primary-keys


LESSONS LEARNED
---------------

NaturalId Is Hibernate-Only so forget about it.

https://stackoverflow.com/questions/14254083/jpa-equivalent-to-hibernates-naturalid

No, there is not. You will have to use composite keys, so either EmbeddedId or IdClass depending what you prefer.
https://docs.oracle.com/javaee/6/api/javax/persistence/EmbeddedId.html
https://docs.oracle.com/javaee/5/api/javax/persistence/IdClass.html




