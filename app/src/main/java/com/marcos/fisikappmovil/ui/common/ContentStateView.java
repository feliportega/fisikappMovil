package com.marcos.fisikappmovil.ui.common;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.marcos.fisikappmovil.R;

public class ContentStateView {

    private final View root;
    private final ProgressBar progressBar;
    private final TextView tvTitle;
    private final TextView tvMessage;
    private final Button btnRetry;

    public ContentStateView(View root) {
        this.root = root;
        this.progressBar = root.findViewById(R.id.progressContentState);
        this.tvTitle = root.findViewById(R.id.tvContentStateTitle);
        this.tvMessage = root.findViewById(R.id.tvContentStateMessage);
        this.btnRetry = root.findViewById(R.id.btnContentStateRetry);
    }

    public void showLoading(String title, String message) {
        root.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.GONE);

        tvTitle.setText(title);
        tvMessage.setText(message);
    }

    public void showEmpty(String title, String message) {
        root.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        tvTitle.setText(title);
        tvMessage.setText(message);
    }

    public void showError(String title, String message, View.OnClickListener retryListener) {
        root.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        btnRetry.setVisibility(View.VISIBLE);

        tvTitle.setText(title);
        tvMessage.setText(message);
        btnRetry.setOnClickListener(retryListener);
    }

    public void hide() {
        root.setVisibility(View.GONE);
    }
}