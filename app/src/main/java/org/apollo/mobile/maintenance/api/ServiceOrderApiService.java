package org.apollo.mobile.maintenance.api;

import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.CompleteBody;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.MaintenanceType;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Order;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Page;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Panel;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.SolarString;
import org.apollo.mobile.maintenance.gateway.ServiceOrderModels.Warning;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ServiceOrderApiService {
    @GET("api/v1/maintenance-registers")
    Call<Page<Order>> getOrders(
            @Query("status") String status,
            @Query("maintenanceTypeId") Long maintenanceTypeId,
            @Query("technicianId") String technicianId,
            @Query("page") int page,
            @Query("size") int size,
            @Query("sortBy") String sortBy,
            @Query("direction") String direction
    );

    @GET("api/v1/maintenance-types")
    Call<List<MaintenanceType>> getMaintenanceTypes();

    @GET("api/v1/maintenance-registers/{id}")
    Call<Order> getOrder(@Path("id") long id);

    @PATCH("api/v1/maintenance-registers/{id}/start")
    Call<Order> start(@Path("id") long id);

    @PATCH("api/v1/maintenance-registers/{id}/complete")
    Call<Order> complete(@Path("id") long id, @Body CompleteBody body);

    @GET("api/v1/warnings/{id}")
    Call<Warning> getWarning(@Path("id") long id);

    @GET("api/v1/strings")
    Call<Page<SolarString>> getStrings(
            @Query("inverterCode") String inverterCode,
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("api/v1/strings/{id}")
    Call<SolarString> getString(@Path("id") String id);

    @GET("api/v1/panels")
    Call<Page<Panel>> getPanels(
            @Query("stringId") String stringId,
            @Query("page") int page,
            @Query("size") int size
    );
}
