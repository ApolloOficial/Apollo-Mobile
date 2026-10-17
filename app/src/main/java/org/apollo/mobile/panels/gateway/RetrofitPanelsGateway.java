package org.apollo.mobile.panels.gateway;

import org.apollo.mobile.panels.api.PanelsApiService;
import org.apollo.mobile.panels.error.PanelsError;

import java.net.SocketTimeoutException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public final class RetrofitPanelsGateway implements PanelsGateway {
    private static final int PAGE_SIZE = 100;

    private final PanelsApiService service;

    public RetrofitPanelsGateway(PanelsApiService service) {
        this.service = service;
    }

    @Override
    public void strings(Callback<List<PanelStringItem>> callback) {
        enqueue(service.strings(PAGE_SIZE), callback, new Mapper<PageResponse<PanelStringItem>, List<PanelStringItem>>() {
            @Override
            public List<PanelStringItem> map(PageResponse<PanelStringItem> body) {
                return body.getContent();
            }
        });
    }

    @Override
    public void panels(String stringId, Callback<List<PanelItem>> callback) {
        enqueue(service.panels(stringId, PAGE_SIZE), callback, new Mapper<PageResponse<PanelItem>, List<PanelItem>>() {
            @Override
            public List<PanelItem> map(PageResponse<PanelItem> body) {
                return body.getContent();
            }
        });
    }

    @Override
    public void findByBarcode(String barcode, Callback<PanelItem> callback) {
        enqueue(service.byBarcode(barcode), callback, new Mapper<PanelItem, PanelItem>() {
            @Override
            public PanelItem map(PanelItem body) {
                return body;
            }
        });
    }

    @Override
    public void findBySerial(String serialNumber, Callback<PanelItem> callback) {
        enqueue(service.bySerial(serialNumber, PAGE_SIZE), callback, new Mapper<PageResponse<PanelItem>, PanelItem>() {
            @Override
            public PanelItem map(PageResponse<PanelItem> body) {
                for (PanelItem item : body.getContent()) {
                    if (item.getSerialNumber().equalsIgnoreCase(serialNumber)) {
                        return item;
                    }
                }
                return null;
            }
        });
    }

    @Override
    public void reportIssue(PanelIssueBody body, Callback<Void> callback) {
        enqueue(service.reportIssue(body), callback, new Mapper<okhttp3.ResponseBody, Void>() {
            @Override
            public Void map(okhttp3.ResponseBody ignored) {
                return null;
            }
        }, false);
    }

    private <B, R> void enqueue(Call<B> call, Callback<R> callback, Mapper<B, R> mapper) {
        enqueue(call, callback, mapper, true);
    }

    private <B, R> void enqueue(Call<B> call, Callback<R> callback, Mapper<B, R> mapper, boolean requireBody) {
        try {
            call.enqueue(new retrofit2.Callback<B>() {
                @Override
                public void onResponse(Call<B> c, Response<B> response) {
                    if (!response.isSuccessful()) {
                        callback.onFailure(PanelsError.fromStatus(response.code()));
                        return;
                    }
                    B body = response.body();
                    if (body == null && requireBody) {
                        callback.onFailure(new PanelsError(PanelsError.Type.INVALID_RESPONSE));
                        return;
                    }
                    callback.onSuccess(body == null ? null : mapper.map(body));
                }

                @Override
                public void onFailure(Call<B> c, Throwable throwable) {
                    PanelsError.Type type = throwable instanceof SocketTimeoutException
                            ? PanelsError.Type.TIMEOUT
                            : PanelsError.Type.NETWORK;
                    callback.onFailure(new PanelsError(type));
                }
            });
        } catch (RuntimeException exception) {
            callback.onFailure(new PanelsError(PanelsError.Type.NETWORK));
        }
    }

    private interface Mapper<B, R> {
        R map(B body);
    }
}
