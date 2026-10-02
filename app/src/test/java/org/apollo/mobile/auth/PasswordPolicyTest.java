package org.apollo.mobile.auth;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apollo.mobile.auth.navigation.AppDestination;
import org.apollo.mobile.auth.policy.PasswordPolicy;
import org.apollo.mobile.auth.policy.RoleAccessPolicy;
import org.apollo.mobile.auth.policy.UserRole;
import org.junit.Test;

public class PasswordPolicyTest {
    @Test
    public void acceptsPasswordWithAllRequirements() {
        assertTrue(PasswordPolicy.isValid("Apollo#2026"));
    }

    @Test
    public void rejectsShortOrMissingClasses() {
        assertFalse(PasswordPolicy.isValid(null));
        assertFalse(PasswordPolicy.isValid("Ab1#"));
        assertFalse(PasswordPolicy.isValid("apollo#2026"));
        assertFalse(PasswordPolicy.isValid("APOLLO#2026"));
        assertFalse(PasswordPolicy.isValid("Apollo#Apollo"));
        assertFalse(PasswordPolicy.isValid("Apollo2026"));
    }

    @Test
    public void rejectsPasswordLongerThan72() {
        StringBuilder value = new StringBuilder("Aa1#");
        for (int i = 0; i < 70; i++) {
            value.append('x');
        }
        assertFalse(PasswordPolicy.isValid(value.toString()));
    }

    @Test
    public void onlyTechnicianReachesTechnicianHome() {
        assertTrue(RoleAccessPolicy.canAccess(UserRole.fromApiValue("TECNICO"), AppDestination.TECHNICIAN_HOME));
        assertFalse(RoleAccessPolicy.canAccess(UserRole.fromApiValue("GERENTE"), AppDestination.TECHNICIAN_HOME));
    }
}
