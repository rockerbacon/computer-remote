package com.lab309.androidUI;

import android.os.Handler;
import android.widget.ProgressBar;

/**
 * Created by Vitor Andrade dos Santos on 4/15/17.
 */

public class ProgressBarManager {
	/*ATTRIBUTES*/
	private ProgressBar progressBar;
	private Handler handler;

	/*CONSTRUCTORS*/
	public ProgressBarManager (ProgressBar progressBar, Handler handler) {
		this.progressBar = progressBar;
		this.handler = handler;
	}

	/*METHODS*/
	public void setVisibility (final int visibility) {
		this.handler.post(new Runnable() {
			@Override
			public void run () {
				ProgressBarManager.this.progressBar.setVisibility(visibility);
			}
		});
	}

}
