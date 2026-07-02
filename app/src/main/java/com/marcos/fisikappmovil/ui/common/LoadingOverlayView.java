package com.marcos.fisikappmovil.ui.common;

import android.view.View;
import android.widget.TextView;

import com.marcos.fisikappmovil.R;

public class LoadingOverlayView {

    private final View root;
    private final TextView tvTitle;
    private final TextView tvMessage;

    public LoadingOverlayView(View root) {
        this.root = root;
        this.tvTitle = root.findViewById(R.id.tvLoadingOverlayTitle);
        this.tvMessage = root.findViewById(R.id.tvLoadingOverlayMessage);
    }

    public void show(String title, String message) {
        if (title != null) {
            tvTitle.setText(title);
        }

        if (message != null) {
            tvMessage.setText(message);
        }

        root.setVisibility(View.VISIBLE);
        root.bringToFront();
    }

    public void hide() {
        root.setVisibility(View.GONE);
    }

    public boolean isShowing() {
        return root.getVisibility() == View.VISIBLE;
    }
}