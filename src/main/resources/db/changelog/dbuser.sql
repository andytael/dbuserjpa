-- liquibase formatted sql

-- changeset nisse:inherit_system endDelimiter:/ runAlways:true
DECLARE
    l_conn_user VARCHAR2(255);
    l_user      VARCHAR2(255);
    l_tblspace  VARCHAR2(255);
BEGIN
    BEGIN
        SELECT user INTO l_conn_user FROM DUAL;
        SELECT username INTO l_user FROM DBA_USERS WHERE USERNAME='USER_REPO';
    EXCEPTION WHEN no_data_found THEN
        EXECUTE IMMEDIATE 'CREATE USER "USER_REPO" NO AUTHENTICATION';
    END;
    
    EXECUTE IMMEDIATE 'ALTER USER "USER_REPO" GRANT CONNECT THROUGH ' || l_conn_user;

    SELECT default_tablespace INTO l_tblspace FROM dba_users WHERE username = 'USER_REPO';

    EXECUTE IMMEDIATE 'ALTER USER "USER_REPO" QUOTA UNLIMITED ON ' || l_tblspace;
    EXECUTE IMMEDIATE 'GRANT CONNECT TO "USER_REPO"';
    EXECUTE IMMEDIATE 'GRANT RESOURCE TO "USER_REPO"';
    EXECUTE IMMEDIATE 'ALTER USER "USER_REPO" DEFAULT ROLE CONNECT,RESOURCE';
END;
/