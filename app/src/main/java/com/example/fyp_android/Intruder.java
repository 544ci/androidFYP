package com.example.fyp_android;

import android.Manifest;
import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeMap;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class Intruder extends DeviceAdminReceiver {
    private static final String TAG = "GGGGGGGGGGGGGGGGG";
    Context context;
    CameraManager manager;
    byte[] intruderImage;
    private CameraDevice cameraDevice;
    private ImageReader imageReader;
    /***
     * camera ids queue.
     */
    private Queue<String> cameraIds;
    private String currentCameraId;
    private boolean cameraClosed;
    /**
     * stores a sorted map of (pictureUrlOnDisk, PictureData).
     */
    private TreeMap<String, byte[]> picturesTaken;
    private PictureCapturingListener capturingListener;
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        private final ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader imageReader) {
                final Image image = imageReader.acquireLatestImage();
                final ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                final byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                intruderImage = bytes;
                saveImageToDisk(bytes);
                Auth auth = new Auth(context, new Login() {
                    @Override
                    public void onSuccessfulLogin(String token) {
                        uploadImage(bytes, "Bearer " + token);
                    }

                    @Override
                    public void onFailedLogin(int code) {
                        Log.e("com.example.abc1", code + "");
                    }
                });
                auth.login();
                image.close();

            }
        };
        private final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request,
                                           @NonNull TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                if (picturesTaken.lastEntry() != null) {
                    capturingListener.onCaptureDone(picturesTaken.lastEntry().getKey(), picturesTaken.lastEntry().getValue());
                    Log.i(TAG, "done taking picture from camera " + cameraDevice.getId());
                }
                closeCamera();
            }
        };

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraClosed = false;
            Log.d(TAG, "camera " + camera.getId() + " opened");
            cameraDevice = camera;
            Log.i(TAG, "Taking picture from camera " + camera.getId());
            //Take the picture after some delay. It may resolve getting a black dark photos.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        takePicture();
                    } catch (final CameraAccessException e) {
                        Log.e(TAG, " exception occurred while taking picture from " + currentCameraId, e);
                    }
                }
            }, 500);
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {

        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {

        }

        private void takePicture() throws CameraAccessException {
            if (null == cameraDevice) {
                Log.e(TAG, "cameraDevice is null");
                return;
            }
            final CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            StreamConfigurationMap streamConfigurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (streamConfigurationMap != null) {
                jpegSizes = streamConfigurationMap.getOutputSizes(ImageFormat.JPEG);
            }
            final boolean jpegSizesNotEmpty = jpegSizes != null && 0 < jpegSizes.length;
            int width = jpegSizesNotEmpty ? jpegSizes[0].getWidth() : 640;
            int height = jpegSizesNotEmpty ? jpegSizes[0].getHeight() : 480;
            final ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            final List<Surface> outputSurfaces = new ArrayList<>();
            outputSurfaces.add(reader.getSurface());
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, 0);
            reader.setOnImageAvailableListener(onImageAvailableListener, null);
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            try {
                                session.capture(captureBuilder.build(), captureListener, null);
                            } catch (final CameraAccessException e) {
                                Log.e(TAG, " exception occurred while accessing " + currentCameraId, e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        }
                    }
                    , null);
        }

        private void saveImageToDisk(final byte[] bytes) {
            File folder = new File(Environment.getExternalStorageDirectory() + "/intruders");
            if (!folder.exists()) {
                folder.mkdir();
            }
            String randomUUIDString = UUID.randomUUID().toString();
            final String cameraId = cameraDevice == null ? UUID.randomUUID().toString() : cameraDevice.getId();
            final File file = new File(Environment.getExternalStorageDirectory() + "/intruders/" + randomUUIDString + ".jpg");

            try (final OutputStream output = new FileOutputStream(file)) {
                output.write(bytes);
                picturesTaken.put(file.getPath(), bytes);
                Log.i(TAG, "Image Saved on Disk ");

            } catch (final IOException e) {
                Log.e(TAG, "Exception occurred while saving picture to external storage ", e);
            }


        }

        private void uploadImage(final byte[] bytes, String token) {
            String fileName = "image.jpg";
            final String phoneId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            Retrofit retrofit = getRetrofitClient(context, App.UPLOAD_IMAGE_URL);
            UploadAPIs uploadAPIs = retrofit.create(UploadAPIs.class);
            RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/jpg"), bytes);
            MultipartBody.Part part = MultipartBody.Part.createFormData("files", fileName, fileReqBody);
            RequestBody description = RequestBody.create(MediaType.parse("text/plain"), phoneId);

            Call call = uploadAPIs.uploadImage(part, description, token);

            call.enqueue(new Callback() {
                @Override
                public void onResponse(Call call, retrofit2.Response response) {
                    Log.i(TAG, "Image Uploaded to server");

                }

                @Override
                public void onFailure(Call call, Throwable t) {

                }
            });

        }

        public Retrofit getRetrofitClient(Context context, String URL) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            return retrofit;
        }

        private void closeCamera() {
            Log.d(TAG, "closing camera " + cameraDevice.getId());
            if (null != cameraDevice && !cameraClosed) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != imageReader) {
                imageReader.close();
                imageReader = null;
            }
        }


    };

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        Log.w("com.example", "heeeeelllloooo");
        this.context = context;
        manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        this.picturesTaken = new TreeMap<>();
        this.capturingListener = new PictureCapturingListener() {
            @Override
            public void onCaptureDone(String pictureUrl, byte[] pictureData) {

            }

            @Override
            public void onDoneCapturingAllPhotos(TreeMap<String, byte[]> picturesTaken) {

            }
        };


        this.cameraIds = new LinkedList<>();
        try {
            final String[] cameraIds = manager.getCameraIdList();
            if (cameraIds.length > 0) {
                this.cameraIds.addAll(Arrays.asList(cameraIds));
//                    this.currentCameraId = this.cameraIds.poll();
                this.currentCameraId = "1";
                openCamera();

            } else {
                //No camera detected!
                capturingListener.onDoneCapturingAllPhotos(picturesTaken);
            }
        } catch (final CameraAccessException e) {
            Log.e("GGGGGGGGGGG", "Exception occurred while accessing the list of cameras", e);
        }

    }

    private void openCamera() {
        Log.d(TAG, "opening camera " + currentCameraId);
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                manager.openCamera(currentCameraId, stateCallback, null);
            }
        } catch (final CameraAccessException e) {
            Log.e(TAG, " exception occurred while opening camera " + currentCameraId, e);
        }
    }

    public interface UploadAPIs {
        @Multipart
        @POST(App.UPLOAD_IMAGE_URL)
        Call<ResponseBody> uploadImage(@Part MultipartBody.Part file, @Part("phoneId") RequestBody requestBody, @Header("Authorization") String token);
    }

}
