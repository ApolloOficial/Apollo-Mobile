package org.apollo.mobile.session;

public interface SessionStorage {

    UserSession read();

    void save(UserSession session);

    void clear();
}
