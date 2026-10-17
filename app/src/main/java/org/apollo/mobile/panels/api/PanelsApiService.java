package org.apollo.mobile.panels.api;

import org.apollo.mobile.panels.gateway.PageResponse;
import org.apollo.mobile.panels.gateway.PanelIssueBody;
import org.apollo.mobile.panels.gateway.PanelItem;
import org.apollo.mobile.panels.gateway.PanelStringItem;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PanelsApiService {

    @GET("api/v1/strings")
    Call<PageResponse<PanelStringItem>> strings(@Query("size") int size);

    @GET("api/v1/panels")
    Call<PageResponse<PanelItem>> panels(@Query("stringId") String stringId, @Query("size") int size);

    @GET("api/v1/panels/barcode/{barcode}")
    Call<PanelItem> byBarcode(@Path("barcode") String barcode);

    @GET("api/v1/panels")
    Call<PageResponse<PanelItem>> bySerial(@Query("serialNumber") String serialNumber, @Query("size") int size);

    @GET("api/v1/warnings")
    Call<PageResponse<org.apollo.mobile.panels.gateway.WarningOrderItem>> warnings(
            @Query("stringId") String stringId, @Query("size") int size);

    @POST("api/v1/panels/activate")
    Call<PanelItem> activate(@Body org.apollo.mobile.panels.gateway.PanelActivateBody body);

    @POST("api/v1/panels/report-issue")
    Call<ResponseBody> reportIssue(@Body PanelIssueBody body);
}
