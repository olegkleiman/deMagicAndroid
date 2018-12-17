package com.okey.demagicandroid;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.android.gms.vision.face.Face;
import com.okey.demagicandroid.common.GraphicOverlay;

public class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float FACE_POSITION_RADIUS = 10.0f;
    private static final float ID_TEXT_SIZE = 40.0f;
    private static final float ID_Y_OFFSET = 50.0f;
    private static final float ID_X_OFFSET = -50.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN,
            Color.MAGENTA,
            Color.RED,
            Color.WHITE,
            Color.YELLOW
    };
    private static int mCurrentColorIndex = 0;

    private Paint mFacePositionPaint;
    private Paint mIdPaint;
    private Paint mBoxPaint;

    private volatile Face mFace;
    private int mFaceId;
    private float mFaceHappiness;

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mFacePositionPaint = new Paint();
        mFacePositionPaint.setColor(selectedColor);

        mIdPaint = new Paint();
        mIdPaint.setColor(selectedColor);
        mIdPaint.setTextSize(ID_TEXT_SIZE);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(selectedColor);
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mBoxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    void setId(int id) {
        mFaceId = id;
    }


    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        // Draws a rectangle at the position of the detected face, with the face's track id below.
        float centerX = translateX(face.getPosition().x + face.getWidth() / 2);
        float centerY = translateY(face.getPosition().y + face.getHeight() / 2);
        canvas.drawCircle(centerX, centerY, FACE_POSITION_RADIUS, mFacePositionPaint);
//        canvas.drawRect(face.getPosition().x,face.getPosition().y,
//                    face.getPosition().x + face.getWidth(),
//                    face.getHeight() + face.getHeight(), mFacePositionPaint);
        int _x = Math.round(face.getPosition().x);
        int _y = Math.round(face.getPosition().y);
        canvas.drawText("(x,y): " + String.format("%d %d", _x, _y), centerX - ID_X_OFFSET, centerY - ID_Y_OFFSET, mIdPaint);
        canvas.drawText("width: " + String.format("%.2f", face.getWidth()), centerX - ID_X_OFFSET, centerY + ID_Y_OFFSET * 2, mIdPaint);
        canvas.drawText("id: " + mFaceId, centerX + ID_X_OFFSET, centerY + ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("happiness: " + String.format("%.2f", face.getIsSmilingProbability()), x - ID_X_OFFSET, y - ID_Y_OFFSET, mIdPaint);
//        canvas.drawText("right eye: " + String.format("%.2f", face.getIsRightEyeOpenProbability()), x + ID_X_OFFSET * 2, y + ID_Y_OFFSET * 2, mIdPaint);
//        canvas.drawText("left eye: " + String.format("%.2f", face.getIsLeftEyeOpenProbability()), x - ID_X_OFFSET*2, y - ID_Y_OFFSET*2, mIdPaint);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getWidth() / 2.0f);
        float yOffset = scaleY(face.getHeight() / 2.0f);
        float left = centerX - xOffset;
        float top = centerY - yOffset;
        float right = centerX + xOffset;
        float bottom = centerY + yOffset;
        canvas.drawRect(left, top, right, bottom, mBoxPaint);
    }
}
