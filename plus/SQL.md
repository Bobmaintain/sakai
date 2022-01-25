

    Nope: alter table SAKAI_SITE add column CONTEXT_GUID varchar(99);

    alter table GB_GRADABLE_OBJECT_T add column PLUS_LINEITEM longtext;

    mysql -u sakaiuser -p

    select TENNANT_GUID, TITLE, TRUST_EMAIL from PLUS_TENANT;


    select TENNANT_GUID, TITLE, TRUST_EMAIL, ISSUER, CLIENT_ID, DEPLOYMENT_ID, OIDC_KEYSET, OIDC_TOKEN from PLUS_TENANT;

    +--------------+------------+-------------+--------------------------------+-------------------+----------------------------------------------+------------------------------------------------------+---------------------------------------------------+
    | TENNANT_GUID | TITLE      | TRUST_EMAIL | ISSUER                         | CLIENT_ID         | DEPLOYMENT_ID                                | OIDC_KEYSET                                          | OIDC_TOKEN                                        |
    +--------------+------------+-------------+--------------------------------+-------------------+----------------------------------------------+------------------------------------------------------+---------------------------------------------------+
    | 123456       | Sakai Plus | NULL        | https://canvas.instructure.com | 85530000000000147 | 326:a16deed8f169b120bdd14743e67ca7916eaea622 | https://canvas.instructure.com/api/lti/security/jwks | https://canvas.instructure.com/login/oauth2/token |
    +--------------+------------+-------------+--------------------------------+-------------------+----------------------------------------------+------------------------------------------------------+---------------------------------------------------+

