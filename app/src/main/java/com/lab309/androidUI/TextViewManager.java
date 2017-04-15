package com.lab309.androidUI;

import android.os.Handler;
import android.widget.TextView;

/**
 * Class for textview management designed to edit textview elements while keeping the application UI responsive
 *
 * Created by Vitor Andrade dos Santos on 3/22/17.
 */

public class TextViewManager {

	/*ATTRIBUTES*/
	private TextView textView;
	private Handler handler;

	/*CONSTRUCTORS*/
	public TextViewManager (TextView textView, Handler handler) {
		this.textView = textView;
		this.handler = handler;
	}

	/*GETTERS*/
	public String getText () {
		return this.textView.getText().toString();
	}

	/*METHODS*/
	public void setText (final String text) {
		this.handler.post(new Runnable() {
			@Override
			public void run () {
				TextViewManager.this.textView.setText(text);
			}
		});
	}

	public void setVisibility (final int visibility) {
		this.handler.post(new Runnable() {
			@Override
			public void run () {
				TextViewManager.this.textView.setVisibility(visibility);
			}
		});
	}

}
