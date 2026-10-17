package org.apollo.mobile.panels.gateway;

import org.apollo.mobile.panels.error.PanelsError;

import java.util.List;

public interface PanelsGateway {

    void strings(Callback<List<PanelStringItem>> callback);

    void panels(String stringId, Callback<List<PanelItem>> callback);

    void findByBarcode(String barcode, Callback<PanelItem> callback);

    void findBySerial(String serialNumber, Callback<PanelItem> callback);

    void reportIssue(PanelIssueBody body, Callback<Void> callback);

    void activate(String barcode, String installationDate, Callback<PanelItem> callback);

    void panelOrders(String stringId, Callback<java.util.Map<Long, Long>> callback);

    interface Callback<T> {
        void onSuccess(T result);

        void onFailure(PanelsError error);
    }
}
