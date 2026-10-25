package org.apollo.mobile.tracking;

public final class TrackingEvents {
    public static final String APP_OPENED = "app_opened";
    public static final String SCREEN_VIEWED = "screen_viewed";
    public static final String WORK_ORDER_VIEWED = "work_order_viewed";
    public static final String WORK_ORDER_START_CLICKED = "work_order_start_clicked";
    public static final String BARCODE_SCANNED = "barcode_scanned";
    public static final String REPORT_STARTED = "report_started";
    public static final String ACTION_FAILED = "action_failed";
    public static final String APP_FOREGROUNDED = "app_foregrounded";
    public static final String APP_BACKGROUNDED = "app_backgrounded";
    public static final String SCREEN_EXITED = "screen_exited";
    public static final String UI_ACTION = "ui_action";
    public static final String REPEATED_ACTION = "repeated_action";
    public static final String FORM_STARTED = "form_started";
    public static final String FORM_SUBMITTED = "form_submitted";
    public static final String FORM_SUCCEEDED = "form_succeeded";
    public static final String FORM_ABANDONED = "form_abandoned";
    public static final String VALIDATION_FAILED = "validation_failed";
    public static final String FILTER_CHANGED = "filter_changed";
    public static final String SEARCH_COMPLETED = "search_completed";
    public static final String EMPTY_STATE_VIEWED = "empty_state_viewed";
    public static final String RETRY_CLICKED = "retry_clicked";
    public static final String FEEDBACK_SHOWN = "feedback_shown";
    public static final String PERMISSION_RESULT = "permission_result";
    public static final String API_REQUEST_COMPLETED = "api_request_completed";
    public static final String API_REQUEST_FAILED = "api_request_failed";
    private TrackingEvents() { }
}
