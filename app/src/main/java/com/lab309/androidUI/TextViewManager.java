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

	/*RUNNABLES*/
	private class Updater implements Runnable {
		private String text;

		private Updater (String text) {
			this.text = text;
		}

		@Override
		public void run () {
			TextViewManager.this.textView.setText(this.text);
		}
	}

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
	public void setText (String text) {
		this.handler.post(new Updater(text));
	}

}
