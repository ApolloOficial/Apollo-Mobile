package org.apollo.mobile.view.common;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.apollo.mobile.R;
import org.apollo.mobile.home.error.HomeError;
import org.apollo.mobile.home.gateway.HomeGateway;
import org.apollo.mobile.home.gateway.HomeGatewayFactory;
import org.apollo.mobile.home.gateway.HomeResponse;
import org.apollo.mobile.settings.error.SettingsError;
import org.apollo.mobile.settings.gateway.SettingsGateway;
import org.apollo.mobile.settings.gateway.SettingsGatewayFactory;
import org.apollo.mobile.notifications.NotificationRepository;
import org.apollo.mobile.view.notifications.NotificationsActivity;
import org.apollo.mobile.view.settings.SettingsActivity;

public final class TopBarFragment extends Fragment {

    public interface Host {
        int topBarActionIcon();
        void onTopBarAction();
    }

    private TextView locationText;
    private View badge;
    private ImageView profileImage;
    private SettingsGateway settingsGateway;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_top_bar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        locationText = view.findViewById(R.id.tvTopBarLocation);
        profileImage = view.findViewById(R.id.ivTopBarProfile);
        settingsGateway = SettingsGatewayFactory.create(requireContext().getApplicationContext());

        view.findViewById(R.id.ivTopBarProfile).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));

        ImageView bell = view.findViewById(R.id.ivTopBarBell);
        if (requireActivity() instanceof Host) {
            Host host = (Host) requireActivity();
            bell.setImageResource(host.topBarActionIcon());
            view.findViewById(R.id.ibTopBarNotifications).setOnClickListener(v -> {
                Motion.bounce(bell);
                host.onTopBarAction();
            });
        } else {
            view.findViewById(R.id.ibTopBarNotifications).setOnClickListener(v -> {
                Motion.bounce(bell);
                v.postDelayed(() -> {
                    if (isAdded()) startActivity(new Intent(requireContext(), NotificationsActivity.class));
                }, 90);
            });
            badge = view.findViewById(R.id.vTopBarBadge);
        }

        loadHeader();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPhoto();
        loadBadge();
    }

    private void loadBadge() {
        if (badge == null) return;
        new NotificationRepository(requireContext()).unreadCount(new NotificationRepository.Result<Long>() {
            @Override
            public void onSuccess(Long value) {
                if (!isAdded() || badge == null) return;
                boolean show = value != null && value > 0;
                if (show && badge.getVisibility() != View.VISIBLE) {
                    badge.setVisibility(View.VISIBLE);
                    Motion.popIn(badge, 200);
                } else if (!show) {
                    badge.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure() { }
        });
    }

    private void loadPhoto() {
        if (profileImage == null || settingsGateway == null) return;
        profileImage.setImageResource(R.drawable.profile_picture);

        settingsGateway.photo(new SettingsGateway.Callback<byte[]>() {
            @Override
            public void onSuccess(byte[] data) {
                if (!isAdded() || profileImage == null || data == null || data.length == 0) return;
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (bitmap != null) profileImage.setImageBitmap(bitmap);
            }

            @Override
            public void onFailure(SettingsError error) {
                if (!isAdded() || profileImage == null) return;
                profileImage.setImageResource(R.drawable.profile_picture);
            }
        });
    }

    private void loadHeader() {
        HomeGateway gateway = HomeGatewayFactory.create(requireContext().getApplicationContext());
        gateway.getHome(new HomeGateway.Callback() {
            @Override
            public void onSuccess(HomeResponse response) {
                if (!isAdded() || locationText == null) return;
                String location = response.getFormattedLocation();
                locationText.setText(location.isEmpty()
                        ? getString(R.string.home_location_unavailable)
                        : location);
            }

            @Override
            public void onFailure(HomeError error) {
                if (!isAdded() || locationText == null) return;
                locationText.setText(R.string.home_location_unavailable);
            }
        });
    }

    @Override
    public void onDestroyView() {
        locationText = null;
        badge = null;
        profileImage = null;
        settingsGateway = null;
        super.onDestroyView();
    }
}
