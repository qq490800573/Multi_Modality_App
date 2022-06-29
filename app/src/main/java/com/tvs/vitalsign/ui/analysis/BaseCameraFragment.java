package com.tvs.vitalsign.ui.analysis;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.tvs.vitalsign.R;
import com.tvs.vitalsign.ui.view.AutoFitTextureView;
import com.tvs.vitalsign.utils.ImageUtils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public abstract class BaseCameraFragment extends Fragment implements ImageReader.OnImageAvailableListener {

    private final static String TAG = BaseCameraFragment.class.getSimpleName();

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread backgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler backgroundHandler;

    private HandlerThread processBackgroundThread;
    private Handler processBackgroundHandler;

    protected void runInBackground(final Runnable r) {
        if (processBackgroundHandler != null) {
            processBackgroundHandler.post(r);
        }
    }

    private boolean cameraLock = false;

    public void setCameraLock(boolean cameraLock) {
        this.cameraLock = cameraLock;
        if (cameraLock) {
            textureView.setSurfaceTextureListener(null);
            closeCamera();
            stopBackgroundThread();
        }
    }

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private final Semaphore cameraOpenCloseLock = new Semaphore(1);

    /**
     * ID of the current {@link CameraDevice}.
     */
    private String cameraId;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice cameraDevice;

    /**
     * An {@link AutoFitTextureView} for camera preview.
     */
    private AutoFitTextureView textureView;

    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(final SurfaceTexture texture, final int width, final int height) {
                    openCamera(width, height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(final SurfaceTexture texture, final int width, final int height) {
                    configureTransform(width, height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(final SurfaceTexture texture) {
                    return true;
                }

                @Override
                public void onSurfaceTextureUpdated(final SurfaceTexture texture) {
                }
            };

    /**
     * {@link CameraDevice.StateCallback}
     * is called when {@link CameraDevice} changes its state.
     */
    private final CameraDevice.StateCallback stateCallback =
            new CameraDevice.StateCallback() {
                @Override
                public void onOpened(final CameraDevice cd) {
                    // This method is called when the camera is opened.  We start camera preview here.
                    cameraOpenCloseLock.release();
                    cameraDevice = cd;
                    createCameraPreviewSession();
                }

                @Override
                public void onDisconnected(final CameraDevice cd) {
                    cameraOpenCloseLock.release();
                    cd.close();
                    cameraDevice = null;
                }

                @Override
                public void onError(final CameraDevice cd, final int error) {
                    cameraOpenCloseLock.release();
                    cd.close();
                    cameraDevice = null;
                    final Activity activity = getActivity();
                    if (null != activity) {
                        activity.finish();
                    }
                }
            };

    private final CameraCaptureSession.CaptureCallback captureCallback =
            new CameraCaptureSession.CaptureCallback() {
            };

    /**
     * The {@link Size} of camera preview.
     */
    private Size previewSize;

    /**
     * {@link CaptureRequest.Builder} for the camera preview
     */
    private CaptureRequest.Builder previewRequestBuilder;

    /**
     * An {@link ImageReader} that handles preview frame capture.
     */
    private ImageReader previewReader;

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession captureSession;

    /**
     * {@link CaptureRequest} generated by {@link #previewRequestBuilder}
     */
    private CaptureRequest previewRequest;

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    private void createCameraPreviewSession() {
        try {
            final SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;

            // We configure the size of default buffer to be the size of camera preview we want.
            texture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());
            // This is the output Surface we need to start preview.
            final Surface surface = new Surface(texture);

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);

            Log.i(TAG, "Opening camera preview: " + previewSize.getWidth() + "x" + previewSize.getHeight());

            // Create the reader for the preview frames.
            previewReader =
                    ImageReader.newInstance(
                            previewSize.getWidth(), previewSize.getHeight(), ImageFormat.YUV_420_888, 2);

            previewReader.setOnImageAvailableListener(this, backgroundHandler);
            previewRequestBuilder.addTarget(previewReader.getSurface());

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice.createCaptureSession(
                    Arrays.asList(surface, previewReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(final CameraCaptureSession cameraCaptureSession) {
                            // The camera is already closed
                            if (null == cameraDevice) {
                                return;
                            }

                            // When the session is ready, we start displaying the preview.
                            captureSession = cameraCaptureSession;
                            try {
                                // Auto focus should be continuous for camera preview.
                                previewRequestBuilder.set(
                                        CaptureRequest.CONTROL_AF_MODE,
                                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                // Flash is automatically enabled when necessary.
                                previewRequestBuilder.set(
                                        CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                previewRequestBuilder.set(
                                        CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                                        CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON
                                );
                                previewRequestBuilder.set(
                                        CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                                        CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON
                                );

                                // Finally, we start displaying the camera preview.
                                previewRequest = previewRequestBuilder.build();
                                captureSession.setRepeatingRequest(
                                        previewRequest, captureCallback, backgroundHandler);
                            } catch (final CameraAccessException e) {
                                Log.e(TAG, "Exception!");
                            }
                        }

                        @Override
                        public void onConfigureFailed(final CameraCaptureSession cameraCaptureSession) {
                            showToast("Failed");
                        }
                    },
                    null);
        } catch (final CameraAccessException e) {
            Log.e(TAG, "Exception!");
        }
    }

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    private void showToast(final String text) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(
                    () -> Toast.makeText(activity, text, Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textureView = getTextureView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        cameraId = chooseCamera();
    }

    protected String chooseCamera() {
        final CameraManager manager = (CameraManager) requireActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            for (final String cameraId : manager.getCameraIdList()) {
                final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                final StreamConfigurationMap map =
                        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) continue;
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "Not allowed to access camera");
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cameraLock) return;
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        }
        textureView.setSurfaceTextureListener(surfaceTextureListener);
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("ImageListener");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        processBackgroundThread = new HandlerThread("ProcessImage");
        processBackgroundThread.start();
        processBackgroundHandler = new Handler(processBackgroundThread.getLooper());
    }

    /**
     * Opens the camera specified by {@link VitalAnalysisFragment#cameraId}.
     */
    private void openCamera(final int width, final int height) {
        setUpCameraOutputs();
        configureTransform(width, height);
        final CameraManager manager = (CameraManager) requireActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.openCamera(cameraId, stateCallback, backgroundHandler);
        } catch (final CameraAccessException e) {
            Log.e(TAG, "Exception!");
        } catch (final InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(final int viewWidth, final int viewHeight) {
        final Activity activity = getActivity();
        if (null == textureView || null == previewSize || null == activity) {
            return;
        }
        final int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        final Matrix matrix = new Matrix();
        final RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        final RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        final float centerX = viewRect.centerX();
        final float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            final float scale =
                    Math.max(
                            (float) viewHeight / previewSize.getHeight(),
                            (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    /**
     * The rotation in degrees of the camera sensor from the display.
     */
    private Integer sensorOrientation;

    protected Integer getSensorOrientation() {
        return sensorOrientation;
    }

    /**
     * The input size in pixels desired by TensorFlow (width and height of a square bitmap).
     */
    private final Size inputSize = new Size(640, 480);

    /**
     * Sets up member variables related to camera.
     */
    private void setUpCameraOutputs() {
        final CameraManager manager = (CameraManager) requireActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

            final StreamConfigurationMap map =
                    characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            // For still image captures, we use the largest available size.
            final Size largest =
                    Collections.max(
                            Arrays.asList(map.getOutputSizes(ImageFormat.YUV_420_888)),
                            new CompareSizesByArea());

            sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

            // Danger, W.R.! Attempting to use too large a preview size could  exceed the camera
            // bus' bandwidth limitation, resulting in gorgeous previews but the storage of
            // garbage capture data.
            previewSize =
                    chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                            inputSize.getWidth(),
                            inputSize.getHeight());

            // We fit the aspect ratio of TextureView to the size of preview we picked.
            final int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                textureView.setAspectRatio(previewSize.getWidth(), previewSize.getHeight());
            } else {
                textureView.setAspectRatio(previewSize.getHeight(), previewSize.getWidth());
            }
        } catch (final CameraAccessException e) {
            Log.e(TAG, "Exception!");
        } catch (final NullPointerException e) {
            throw new RuntimeException(getString(R.string.camera_error));
        }

        onPreviewSizeChosen(previewSize, sensorOrientation);
    }

    private static final int MINIMUM_PREVIEW_SIZE = 100;

    /**
     * Given {@code choices} of {@code Size}s supported by a camera, chooses the smallest one whose
     * width and height are at least as large as the minimum of both, or an exact match if possible.
     *
     * @param choices The list of sizes that the camera supports for the intended output class
     * @param width   The minimum desired width
     * @param height  The minimum desired height
     * @return The optimal {@code Size}, or an arbitrary one if none were big enough
     */
    protected static Size chooseOptimalSize(final Size[] choices, final int width, final int height) {
        final int minSize = Math.max(Math.min(width, height), MINIMUM_PREVIEW_SIZE);
        final Size desiredSize = new Size(width, height);

        // Collect the supported resolutions that are at least as big as the preview Surface
        boolean exactSizeFound = false;
        final List<Size> bigEnough = new ArrayList<Size>();
        final List<Size> tooSmall = new ArrayList<Size>();
        for (final Size option : choices) {
            if (option.equals(desiredSize)) {
                // Set the size but don't return yet so that remaining sizes will still be logged.
                exactSizeFound = true;
            }

            if (option.getHeight() >= minSize && option.getWidth() >= minSize) {
                bigEnough.add(option);
            } else {
                tooSmall.add(option);
            }
        }

        Log.i(TAG, "Desired size: " + desiredSize + ", min size: " + minSize + "x" + minSize);
        Log.i(TAG, "Valid preview sizes: [" + TextUtils.join(", ", bigEnough) + "]");
        Log.i(TAG, "Rejected preview sizes: [" + TextUtils.join(", ", tooSmall) + "]");

        if (exactSizeFound) {
            Log.i(TAG, "Exact size match found.");
            return desiredSize;
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            final Size chosenSize = Collections.min(bigEnough, new CompareSizesByArea());
            Log.i(TAG, "Chosen size: " + chosenSize.getWidth() + "x" + chosenSize.getHeight());
            return chosenSize;
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    private final int CROP_WIDTH = getCropWidth();
    private final int CROP_HEIGHT = getCropHeight();

    private int previewWidth = 0;
    private int previewHeight = 0;

    public int getPreviewWidth() {
        return previewWidth;
    }

    public int getPreviewHeight() {
        return previewHeight;
    }

    private Bitmap rgbFrameBitmap = null;

    public Bitmap getRgbFrameBitmap() {
        return rgbFrameBitmap;
    }

    private Bitmap croppedBitmap = null;

    public Bitmap getCroppedBitmap() {
        return croppedBitmap;
    }

    private Matrix frameToCropTransform;

    public Matrix getFrameToCropTransform() {
        return frameToCropTransform;
    }

    private Matrix cropToFrameTransform;

    public Matrix getCropToFrameTransform() {
        return cropToFrameTransform;
    }

    private void onPreviewSizeChosen(Size size, int rotation) {

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        sensorOrientation = rotation - getScreenOrientation();
        Log.i(TAG, "Camera orientation relative to screen canvas: " + sensorOrientation);

        Log.i(TAG, String.format("Initializing at size %dx%d", previewWidth, previewHeight));
        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(CROP_WIDTH, CROP_HEIGHT, Bitmap.Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        CROP_WIDTH, CROP_HEIGHT,
                        sensorOrientation, false);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

    }

    protected int getScreenOrientation() {
        switch (requireActivity().getWindowManager().getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_270:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_90:
                return 270;
            default:
                return 0;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (cameraLock) return;
        textureView.setSurfaceTextureListener(null);
        closeCamera();
        stopBackgroundThread();
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (null != captureSession) {
                captureSession.close();
                captureSession = null;
            }
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != previewReader) {
                previewReader.close();
                previewReader = null;
            }
        } catch (final InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    protected void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
                backgroundThread = null;
                backgroundHandler = null;
            } catch (final InterruptedException e) {
                Log.e(TAG, "Exception!");
            }
        }
        if (processBackgroundThread != null) {
            processBackgroundThread.quitSafely();
            try {
                processBackgroundThread.join();
                processBackgroundThread = null;
                processBackgroundHandler = null;
            } catch (final InterruptedException e) {
                Log.e(TAG, "Exception!");
            }
        }
    }

    private Runnable imageConverter;
    private int[] rgbBytes = null;
    private byte[][] yuvBytes = new byte[3][];
    private int yRowStride;
    Bitmap c_bitmap;
    int[] intValue = new int[300 * 300];

    protected int getLuminanceStride() {
        return yRowStride;
    }

    protected byte[] getLuminance() {
        return yuvBytes[0];
    }

    @Override
    public void onImageAvailable(final ImageReader reader) {
        //We need wait until we have some size from onPreviewSizeChosen
        if (previewWidth == 0 || previewHeight == 0) {
            return;
        }
        if (rgbBytes == null) {
            rgbBytes = new int[previewWidth * previewHeight];
        }
        try {
            final Image image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }
            Trace.beginSection("imageAvailable");
            final Image.Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);
            yRowStride = planes[0].getRowStride();
            final int uvRowStride = planes[1].getRowStride();
            final int uvPixelStride = planes[1].getPixelStride();
            imageConverter =
                    () -> ImageUtils.convertYUV420ToARGB8888(
                            yuvBytes[0],
                            yuvBytes[1],
                            yuvBytes[2],
                            previewWidth,
                            previewHeight,
                            yRowStride,
                            uvRowStride,
                            uvPixelStride,
                            rgbBytes);

            rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
            c_bitmap = Bitmap.createScaledBitmap(rgbFrameBitmap, 300, 300, true);
            c_bitmap.getPixels(intValue, 0, 300, 0, 0, 300, 300);

            rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);
            processImage(image);
        } catch (final Exception e) {
            Log.e(TAG, "Exception!");
            Trace.endSection();
            return;
        }
        Trace.endSection();
    }

    protected void fillBytes(final Image.Plane[] planes, final byte[][] yuvBytes) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (int i = 0; i < planes.length; ++i) {
            final ByteBuffer buffer = planes[i].getBuffer();
            if (yuvBytes[i] == null) {
                Log.d(TAG, String.format("Initializing buffer %d at size %d", i, buffer.capacity()));
                yuvBytes[i] = new byte[buffer.capacity()];
            }
            buffer.get(yuvBytes[i]);
        }
    }

    protected int[] getRgbBytes() {
        imageConverter.run();
        return rgbBytes;
    }

    protected abstract AutoFitTextureView getTextureView();

    protected abstract int getCropWidth();

    protected abstract int getCropHeight();

    protected abstract void processImage(Image image);

    /**
     * Compares two {@code Size}s based on their areas.
     */
    private static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(final Size lhs, final Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum(
                    (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }

}