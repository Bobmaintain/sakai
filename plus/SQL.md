

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

    https://dev1.sakaicloud.com/plus/sakai/canvas-config.json?guid=123456

    INSERT INTO PLUS_TENANT (TENNANT_GUID, TITLE) VALUES ('54321', 'Blackboard');

    https://dev1.sakaicloud.com/plus/sakai/canvas-config.json?guid=54321

    UPDATE PLUS_TENANT SET
        ISSUER = 'https://blackboard.com',
        CLIENT_ID = '4c43e5f0-9eef-425f-bf7c-c81689013cb7',
        DEPLOYMENT_ID = '14af10f1-04ed-4457-8e40-a581681458ce',
        OIDC_KEYSET = 'https://devportal-stage.saas.bbpd.io/api/v1/management/applications/4c43e5f0-9eef-425f-bf7c-c81689013cb7/jwks.json',
        OIDC_TOKEN = 'https://devportal-stage.saas.bbpd.io/api/v1/gateway/oauth2/jwttoken',
        OIDC_AUTH = 'https://devportal-stage.saas.bbpd.io/api/v1/gateway/oidcauth'
    WHERE TENNANT_GUID='54321';

    App Key (REST): 7cbbfd88-------REST-----ONLY----6ddc
    Secret(REST): ijWQ------REST----ONLY----fkn4U6
    ClientId: 4c43e5f0-9eef-425f-bf7c-c81689013cb7
    https://blackboard.com
    https://devportal-stage.saas.bbpd.io/api/v1/management/applications/4c43e5f0-9eef-425f-bf7c-c81689013cb7/jwks.json
    https://devportal-stage.saas.bbpd.io/api/v1/gateway/oauth2/jwttoken
    https://devportal-stage.saas.bbpd.io/api/v1/gateway/oidcauth


    mysql> describe PLUS_TENANT;
    +---------------+---------------+------+-----+---------+-------+
    | Field         | Type          | Null | Key | Default | Extra |
    +---------------+---------------+------+-----+---------+-------+
    | TENNANT_GUID  | varchar(36)   | NO   | PRI | NULL    |       |
    | CACHE_KEYSET  | varchar(4000) | YES  |     | NULL    |       |
    | CLIENT_ID     | varchar(200)  | YES  |     | NULL    |       |
    | DEPLOYMENT_ID | varchar(200)  | YES  |     | NULL    |       |
    | DESCRIPTION   | varchar(4000) | YES  |     | NULL    |       |
    | ISSUER        | varchar(200)  | YES  | MUL | NULL    |       |
    | OIDC_AUDIENCE | varchar(200)  | YES  |     | NULL    |       |
    | OIDC_AUTH     | varchar(500)  | YES  |     | NULL    |       |
    | OIDC_KEYSET   | varchar(500)  | YES  |     | NULL    |       |
    | OIDC_TOKEN    | varchar(500)  | YES  |     | NULL    |       |
    | TITLE         | varchar(500)  | NO   |     | NULL    |       |
    | TRUST_EMAIL   | bit(1)        | YES  |     | NULL    |       |
    +---------------+---------------+------+-----+---------+-------+
    12 rows in set (0.01 sec)

