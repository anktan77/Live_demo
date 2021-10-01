package com.example.capture.video.camera;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.os.Build;

@SuppressWarnings("deprecation")
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class VideoCaptureFactory {
    private static boolean isLReleaseOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    private static boolean isLegacyDevice(Context context) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics
                        = manager.getCameraCharacteristics(cameraId);
                Integer level = characteristics.get(
                        CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                return (level != null && level ==
                        CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY);
            }

            return false;
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static VideoCapture createVideoCapture(Context context) {
        if (isLReleaseOrLater() && !isLegacyDevice(context)) {
            return new VideoCaptureCamera2(context);
        } else {
            return new VideoCaptureCamera(context);
        }
    }
}
