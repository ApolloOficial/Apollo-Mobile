package org.apollo.mobile.view.auth;

import androidx.annotation.StringRes;

import org.apollo.mobile.R;
import org.apollo.mobile.auth.error.AuthError;

final class AuthMessages {
    private AuthMessages() {
    }

    @StringRes
    static int generic(AuthError error) {
        switch (error.getType()) {
            case TIMEOUT:
                return R.string.login_timeout;
            case NETWORK:
                return R.string.login_network_error;
            case OTP_INVALID:
                return R.string.otp_invalid;
            case OTP_EXPIRED:
                return R.string.otp_expired;
            case OTP_TOO_MANY_ATTEMPTS:
                return R.string.otp_too_many_attempts;
            case OTP_RESEND_TOO_SOON:
                return R.string.otp_resend_too_soon;
            case RESET_REJECTED:
                return R.string.recovery_reset_rejected;
            case LOCKED:
                return R.string.login_locked;
            default:
                return R.string.login_unavailable;
        }
    }
}
