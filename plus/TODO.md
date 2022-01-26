
TODO LIST
=========

Figure out UI/UX best practice for pretty CRUD UIs - talk to Adrian / Earle

Put context memberships into a thread and then into a batch job

Put lineitem creation and score sending into database for redo on failure and add batch job

Build pretty UI for errors

Add the validation errors to debug log

THINGS TO RESEARCH
------------------

LIMIT 1
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




