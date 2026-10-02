package org.apollo.mobile.view.auth;

/** Chaves dos extras que levam o estado do fluxo de código entre as telas. */
public final class AuthExtras {

    /** Por que o código está sendo pedido: LOGIN (2FA) ou RECOVERY (esqueci a senha). */
    public static final String MODE = "mode";
    public static final String MODE_LOGIN = "LOGIN";
    public static final String MODE_RECOVERY = "RECOVERY";

    public static final String CHALLENGE_ID = "challenge_id";
    /** ArrayList&lt;OtpMethod&gt; (Serializable). */
    public static final String METHODS = "methods";
    public static final String METHOD = "method";
    public static final String DESTINATION = "destination";
    public static final String RESEND_SECONDS = "resend_seconds";
    public static final String RESET_TOKEN = "reset_token";

    private AuthExtras() {
    }
}
