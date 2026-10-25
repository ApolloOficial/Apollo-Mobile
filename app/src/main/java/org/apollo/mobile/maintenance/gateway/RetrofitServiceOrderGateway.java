package org.apollo.mobile.maintenance.gateway;

import org.apollo.mobile.maintenance.api.ServiceOrderApiService;
import org.apollo.mobile.tracking.TrackingEvents;
import org.apollo.mobile.tracking.TrackingManager;
import org.apollo.mobile.settings.api.SettingsApiService;
import org.apollo.mobile.settings.gateway.SettingsSummary;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.CompleteBody;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Details;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.MaintenanceType;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Order;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Page;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Panel;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Scope;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.SolarString;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.StringGroup;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Warning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public final class RetrofitServiceOrderGateway implements ServiceOrderGateway {
    private final ServiceOrderApiService api;
    private final SettingsApiService settingsApi;

    public RetrofitServiceOrderGateway(ServiceOrderApiService api, SettingsApiService settingsApi) {
        this.api = api;
        this.settingsApi = settingsApi;
    }

    @Override
    public void list(String status, Callback<List<Order>> callback) {
        listAssigned(emptyToNull(status), null, callback);
    }

    @Override
    public void listByType(Long maintenanceTypeId, Callback<List<Order>> callback) {
        listAssigned(null, maintenanceTypeId, callback);
    }

    private void listAssigned(String status, Long maintenanceTypeId,
                              Callback<List<Order>> callback) {
        settingsApi.summary().enqueue(new retrofit2.Callback<SettingsSummary>() {
            @Override
            public void onResponse(Call<SettingsSummary> call, Response<SettingsSummary> response) {
                if (!response.isSuccessful() || response.body() == null
                        || !notBlank(response.body().getUserId())) {
                    callback.onFailure(error(response.code()));
                    return;
                }
                SettingsSummary summary = response.body();
                String technicianId = "TECHNICIAN".equalsIgnoreCase(summary.getRole())
                        ? summary.getUserId() : null;
                requestOrders(status, maintenanceTypeId, technicianId, callback);
            }

            @Override
            public void onFailure(Call<SettingsSummary> call, Throwable throwable) {
                callback.onFailure(new ServiceOrderError(ServiceOrderError.Type.NETWORK));
            }
        });
    }

    private void requestOrders(String status, Long maintenanceTypeId, String technicianId,
                               Callback<List<Order>> callback) {
        api.getOrders(status, maintenanceTypeId, technicianId, 0, 100, "openingDt", "DESC")
                .enqueue(new retrofit2.Callback<Page<Order>>() {
                    @Override
                    public void onResponse(Call<Page<Order>> call, Response<Page<Order>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            callback.onFailure(error(response.code()));
                            return;
                        }
                        callback.onSuccess(response.body().getContent());
                    }

                    @Override
                    public void onFailure(Call<Page<Order>> call, Throwable throwable) {
                        callback.onFailure(new ServiceOrderError(ServiceOrderError.Type.NETWORK));
                    }
                });
    }

    @Override
    public void maintenanceTypes(Callback<List<MaintenanceType>> callback) {
        api.getMaintenanceTypes().enqueue(new retrofit2.Callback<List<MaintenanceType>>() {
            @Override
            public void onResponse(Call<List<MaintenanceType>> call,
                                   Response<List<MaintenanceType>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onFailure(error(response.code()));
                    return;
                }
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<List<MaintenanceType>> call, Throwable throwable) {
                callback.onFailure(new ServiceOrderError(ServiceOrderError.Type.NETWORK));
            }
        });
    }

    @Override
    public void details(long id, Callback<Details> callback) {
        api.getOrder(id).enqueue(new retrofit2.Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onFailure(error(response.code()));
                    return;
                }

                Order order = response.body();
                if (order.getWarningId() == null) {
                    callback.onFailure(new ServiceOrderError(ServiceOrderError.Type.INVALID_RESPONSE));
                    return;
                }
                loadWarning(order, callback);
            }

            @Override
            public void onFailure(Call<Order> call, Throwable throwable) {
                callback.onFailure(new ServiceOrderError(ServiceOrderError.Type.NETWORK));
            }
        });
    }

    @Override
    public void targetLabel(long warningId, Callback<String> callback) {
        api.getWarning(warningId).enqueue(new retrofit2.Callback<Warning>() {
            @Override
            public void onResponse(Call<Warning> call, Response<Warning> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onFailure(error(response.code()));
                    return;
                }

                Warning warning = response.body();
                if (warning.getPanelId() != null) {
                    callback.onSuccess("Placa " + safe(warning.getPanelSerial()));
                    return;
                }
                if (notBlank(warning.getStringId())) {
                    callback.onSuccess(safe(warning.getInverterCode())
                            + ", String " + safe(warning.getStringCode()));
                    return;
                }
                callback.onSuccess("Inversor " + safe(warning.getInverterCode())
                        + ", Todas as strings");
            }

            @Override
            public void onFailure(Call<Warning> call, Throwable throwable) {
                callback.onFailure(new ServiceOrderError(ServiceOrderError.Type.NETWORK));
            }
        });
    }

    private void loadWarning(Order order, Callback<Details> callback) {
        api.getWarning(order.getWarningId()).enqueue(new retrofit2.Callback<Warning>() {
            @Override
            public void onResponse(Call<Warning> call, Response<Warning> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onFailure(error(response.code()));
                    return;
                }

                Warning warning = response.body();
                if (warning.getPanelId() != null) {
                    loadSingleString(order, warning, Scope.PANEL, callback);
                } else if (notBlank(warning.getStringId())) {
                    loadSingleString(order, warning, Scope.STRING, callback);
                } else if (notBlank(warning.getInverterCode())) {
                    loadInverter(order, warning, callback);
                } else {
                    callback.onFailure(new ServiceOrderError(ServiceOrderError.Type.INVALID_RESPONSE));
                }
            }

            @Override
            public void onFailure(Call<Warning> call, Throwable throwable) {
                callback.onFailure(new ServiceOrderError(ServiceOrderError.Type.NETWORK));
            }
        });
    }

    private void loadSingleString(Order order, Warning warning, Scope scope, Callback<Details> callback) {
        if (!notBlank(warning.getStringId())) {
            callback.onSuccess(new Details(order, warning, scope, Collections.emptyList()));
            return;
        }

        api.getString(warning.getStringId()).enqueue(new retrofit2.Callback<SolarString>() {
            @Override
            public void onResponse(Call<SolarString> call, Response<SolarString> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onFailure(error(response.code()));
                    return;
                }
                SolarString solarString = response.body();
                loadPanels(solarString, panels -> callback.onSuccess(
                        new Details(order, warning, scope,
                                Collections.singletonList(new StringGroup(solarString, panels)))
                ), callback);
            }

            @Override
            public void onFailure(Call<SolarString> call, Throwable throwable) {
                callback.onFailure(new ServiceOrderError(ServiceOrderError.Type.NETWORK));
            }
        });
    }

    private void loadInverter(Order order, Warning warning, Callback<Details> callback) {
        api.getStrings(warning.getInverterCode(), 0, 100)
                .enqueue(new retrofit2.Callback<Page<SolarString>>() {
                    @Override
                    public void onResponse(Call<Page<SolarString>> call, Response<Page<SolarString>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            callback.onFailure(error(response.code()));
                            return;
                        }
                        loadGroupsSequentially(order, warning, response.body().getContent(), 0,
                                new ArrayList<>(), callback);
                    }

                    @Override
                    public void onFailure(Call<Page<SolarString>> call, Throwable throwable) {
                        callback.onFailure(new ServiceOrderError(ServiceOrderError.Type.NETWORK));
                    }
                });
    }

    private void loadGroupsSequentially(Order order, Warning warning, List<SolarString> strings,
                                        int index, List<StringGroup> groups, Callback<Details> callback) {
        if (index >= strings.size()) {
            callback.onSuccess(new Details(order, warning, Scope.INVERTER, groups));
            return;
        }

        SolarString solarString = strings.get(index);
        loadPanels(solarString, panels -> {
            groups.add(new StringGroup(solarString, panels));
            loadGroupsSequentially(order, warning, strings, index + 1, groups, callback);
        }, callback);
    }

    private interface PanelsSuccess {
        void accept(List<Panel> panels);
    }

    private void loadPanels(SolarString solarString, PanelsSuccess success, Callback<?> callback) {
        api.getPanels(solarString.getId(), 0, 200)
                .enqueue(new retrofit2.Callback<Page<Panel>>() {
                    @Override
                    public void onResponse(Call<Page<Panel>> call, Response<Page<Panel>> response) {
                        if (!response.isSuccessful() || response.body() == null) {
                            callback.onFailure(error(response.code()));
                            return;
                        }
                        success.accept(response.body().getContent());
                    }

                    @Override
                    public void onFailure(Call<Page<Panel>> call, Throwable throwable) {
                        callback.onFailure(new ServiceOrderError(ServiceOrderError.Type.NETWORK));
                    }
                });
    }

    @Override
    public void start(long id, Callback<Order> callback) {
        TrackingManager.order(TrackingEvents.WORK_ORDER_START_CLICKED, "WorkOrderStart", id);
        enqueueOrder(api.start(id), tracked(callback, "start_work_order", id));
    }

    @Override
    public void complete(long id, String report, double laborCost, Callback<Order> callback) {
        enqueueOrder(api.complete(id, new CompleteBody(report.trim(), Math.max(0.0, laborCost))),
                tracked(callback, "complete_work_order", id));
    }

    private Callback<Order> tracked(Callback<Order> callback, String action, long id) {
        return new Callback<Order>() {
            @Override
            public void onSuccess(Order order) {
                callback.onSuccess(order);
            }

            @Override
            public void onFailure(ServiceOrderError error) {
                TrackingManager.failure("WorkOrderOperation", action, error.getType().name(), id);
                callback.onFailure(error);
            }
        };
    }

    private void enqueueOrder(Call<Order> call, Callback<Order> callback) {
        call.enqueue(new retrofit2.Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onFailure(error(response.code()));
                    return;
                }
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<Order> call, Throwable throwable) {
                callback.onFailure(new ServiceOrderError(ServiceOrderError.Type.NETWORK));
            }
        });
    }

    private ServiceOrderError error(int code) {
        if (code == 401 || code == 403) return new ServiceOrderError(ServiceOrderError.Type.UNAUTHORIZED);
        if (code == 404) return new ServiceOrderError(ServiceOrderError.Type.NOT_FOUND);
        if (code >= 500) return new ServiceOrderError(ServiceOrderError.Type.SERVER);
        return new ServiceOrderError(ServiceOrderError.Type.INVALID_RESPONSE);
    }

    private String emptyToNull(String value) {
        return notBlank(value) ? value : null;
    }

    private boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String safe(String value) {
        return notBlank(value) ? value.trim() : "-";
    }
}
