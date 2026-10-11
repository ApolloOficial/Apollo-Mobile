package org.apollo.mobile.settings;

import static org.junit.Assert.assertEquals;

import org.apollo.mobile.settings.error.SettingsError;
import org.apollo.mobile.settings.error.SettingsErrorMapper;
import org.junit.Test;

public class SettingsErrorMapperTest {
    @Test
    public void mapsAuthStatuses() {
        assertEquals(SettingsError.Type.SESSION_EXPIRED, SettingsErrorMapper.fromStatus(401));
        assertEquals(SettingsError.Type.FORBIDDEN, SettingsErrorMapper.fromStatus(403));
        assertEquals(SettingsError.Type.NOT_FOUND, SettingsErrorMapper.fromStatus(404));
    }

    @Test
    public void mapsRequestErrors() {
        assertEquals(SettingsError.Type.INVALID_REQUEST, SettingsErrorMapper.fromStatus(400));
        assertEquals(SettingsError.Type.INVALID_REQUEST, SettingsErrorMapper.fromStatus(409));
        assertEquals(SettingsError.Type.INVALID_REQUEST, SettingsErrorMapper.fromStatus(413));
        assertEquals(SettingsError.Type.INVALID_REQUEST, SettingsErrorMapper.fromStatus(422));
    }

    @Test
    public void mapsServerErrors() {
        assertEquals(SettingsError.Type.UNAVAILABLE, SettingsErrorMapper.fromStatus(503));
        assertEquals(SettingsError.Type.SERVER, SettingsErrorMapper.fromStatus(500));
        assertEquals(SettingsError.Type.INVALID_RESPONSE, SettingsErrorMapper.fromStatus(302));
    }
}
