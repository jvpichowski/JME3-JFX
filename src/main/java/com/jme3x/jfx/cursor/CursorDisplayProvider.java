package com.jme3x.jfx.cursor;

import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.cursor.CursorType;

public interface CursorDisplayProvider {

	/**
	 * called by the JFxManager during startup, should be used to renderToFullscreen the necessary cursors
	 * 
	 * @param normal
	 */
	void setup(CursorType normal);

	void showCursor(CursorFrame cursorFrame);

}
