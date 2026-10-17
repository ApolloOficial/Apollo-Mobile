package org.apollo.mobile.maintenance.gateway;

import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Details;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Order;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.MaintenanceType;

import java.util.List;

public interface ServiceOrderGateway {
    void list(String status, Callback<List<Order>> callback);
    void listByType(Long maintenanceTypeId, Callback<List<Order>> callback);
    void maintenanceTypes(Callback<List<MaintenanceType>> callback);
    void targetLabel(long warningId, Callback<String> callback);
    void details(long id, Callback<Details> callback);
    void start(long id, Callback<Order> callback);
    void complete(long id, String report, double laborCost, Callback<Order> callback);

    interface Callback<T> {
        void onSuccess(T value);
        void onFailure(ServiceOrderError error);
    }
}
