
TODO LIST
=========

Make smarter use of cookies in the iframe Content Security Policy - Matt 

Make a better template site to start with - Wilma?

Move CSS from headscripts to someplace else - Shawn?

Figure out UI/UX best practice for pretty CRUD UIs - talk to Adrian / Earle

Put context memberships into a batch job - Sam / Matt?

Put lineitem creation and score sending into database for redo on failure and add batch job

Build pretty UI for errors - add forward to knowledgebase feature

Figure out inheritance in PlusPortal 

Make a single tool content item request and Canvas config flow work

Understand the ramifications of SameSite None - Perhaps add a CSP header to lock things down

Ask Adrian about the use of window.parent in webcomponents/tool/src/main/frontend/js/sakai-button.js

Be unhappy about the fact that resource link placements don't allow resizing
https://community.canvaslms.com/t5/Canvas-Question-Forum/Dynamically-resizing-iframes-in-Canvas/td-p/131341

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




