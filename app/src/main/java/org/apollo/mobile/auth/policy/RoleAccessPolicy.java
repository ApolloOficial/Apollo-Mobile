package org.apollo.mobile.auth.policy;

import org.apollo.mobile.auth.navigation.AppDestination;

public final class RoleAccessPolicy {

    private RoleAccessPolicy() {
    }

    public static boolean canAccess(UserRole role, AppDestination destination) {
        if (role == null || destination == null) {
            return false;
        }

        switch (destination) {
            case TECHNICIAN_HOME:
                return role == UserRole.TECHNICIAN || role == UserRole.ADMINISTRATOR;
            default:
                return false;
        }
    }
}
